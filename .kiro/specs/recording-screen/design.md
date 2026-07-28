# Design Document — Recording Screen

## Overview

The Recording Screen is the first vertical slice of the AI Interview Coach Android app. It is a self-contained feature that lets a student view a sample interview question, record a video/audio response using the device camera and microphone, and browse a history list of past recordings. No network activity, scoring, or analysis is involved.

The implementation follows the **MVVM** pattern with **CameraX** for camera/recording operations, a **Room** database for persistence, and **Hilt** for dependency injection. All modules are kept decoupled so team members can work in parallel without merge conflicts.

### Key Design Decisions

| Decision | Choice | Rationale |
|---|---|---|
| DI framework | Hilt | Hilt is the officially recommended Android DI library. For a college mini-project it adds very little boilerplate and its annotations are easy to understand for the whole team. Manual DI would require hand-wiring every ViewModel and repository. |
| Camera API | CameraX `VideoCapture<Recorder>` | CameraX abstracts Camera2 complexity, handles lifecycle automatically, and its `PendingRecording` API is straightforward. Camera2 directly is unnecessary complexity. |
| Storage | `Context.getFilesDir()` (app-internal) | Satisfies requirement of private, local-only storage. No `WRITE_EXTERNAL_STORAGE` permission needed on API 29+. |
| Navigation | Jetpack Navigation Component | Single-activity pattern keeps the back-stack clean and matches team conventions. |
| Async | Kotlin Coroutines + Flow | Standard Kotlin async, works natively with Room and LiveData/StateFlow. |

---

## Architecture

The feature is structured in three layers:

```
┌─────────────────────────────────────────────────┐
│                   UI Layer                       │
│  RecordingFragment        HistoryFragment        │
│  RecordingActivity (host)                        │
└─────────────┬───────────────────┬───────────────┘
              │ observes StateFlow │ observes StateFlow
┌─────────────▼───────────────────▼───────────────┐
│               ViewModel Layer                    │
│  RecorderViewModel        HistoryViewModel       │
└─────────────┬───────────────────┬───────────────┘
              │ suspends/collects  │ collects Flow
┌─────────────▼───────────────────▼───────────────┐
│               Data Layer                         │
│  RecordingRepository  ←→  RecordingDao (Room)    │
│  LocalStorageHelper (file I/O)                   │
└─────────────────────────────────────────────────┘
```

### Package Layout

```
app/
  ui/
    recording/
      RecordingActivity.kt          ← single-activity host
      RecordingFragment.kt          ← camera preview + controls
      RecorderViewModel.kt          ← CameraX lifecycle, UI state
      HistoryFragment.kt            ← list of past recordings
      HistoryViewModel.kt           ← loads & sorts entries
      RecordingAdapter.kt           ← RecyclerView adapter
  data/
    local/
      AppDatabase.kt                ← Room database singleton
      RecordingDao.kt               ← DAO interface
      RecordingRepository.kt        ← repository abstraction
      LocalStorageHelper.kt         ← file-path utilities
  model/
    RecordingEntry.kt               ← Room @Entity + domain model
  di/
    DatabaseModule.kt               ← Hilt module for DB + DAO
    RepositoryModule.kt             ← Hilt module for repository
```

---

## Components and Interfaces

### RecordingActivity

Single-activity host. Hosts the Jetpack Navigation graph and nothing else. Responsible for edge-to-edge setup.

```kotlin
@AndroidEntryPoint
class RecordingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recording)
        // NavHostFragment is declared in XML layout
    }
}
```

---

### RecordingFragment

Responsibility: permission gating, CameraX preview binding, button wiring, UI state observation.

Key methods:

| Method | Responsibility |
|---|---|
| `onViewCreated` | Checks permissions, observes `uiState`, sets click listeners |
| `checkAndRequestPermissions()` | Calls `ActivityResultLauncher` for camera + mic |
| `startCamera()` | Binds `Preview` + `VideoCapture<Recorder>` to lifecycle |
| `onRecordClicked()` | Delegates to `viewModel.startRecording(outputFile)` |
| `onStopClicked()` | Delegates to `viewModel.stopRecording()` |
| `renderState(state: RecordingUiState)` | Updates all views in response to state |

```kotlin
@AndroidEntryPoint
class RecordingFragment : Fragment(R.layout.fragment_recording) {

    private val viewModel: RecorderViewModel by viewModels()

    // Permission launcher (both permissions requested together)
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants -> viewModel.onPermissionsResult(grants) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkAndRequestPermissions()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { renderState(it) }
        }
        // wire buttons …
    }
}
```

---

### RecorderViewModel

Responsibility: orchestrates CameraX lifecycle, owns `RecordingUiState`, delegates persistence to `RecordingRepository`.

```kotlin
@HiltViewModel
class RecorderViewModel @Inject constructor(
    private val repository: RecordingRepository,
    private val storageHelper: LocalStorageHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordingUiState())
    val uiState: StateFlow<RecordingUiState> = _uiState.asStateFlow()

    private var activeRecording: Recording? = null   // CameraX handle

    fun onPermissionsResult(grants: Map<String, Boolean>)
    fun startRecording(outputFile: File)
    fun stopRecording()
    private fun onRecordingFinalized(result: VideoRecordEvent.Finalize)
    private fun onRecordingError(error: VideoRecordEvent)
}
```

State transitions:

```
IDLE ──[startRecording]──► RECORDING ──[stopRecording]──► SAVING
                                   └──[error]──────────► ERROR
SAVING ──[success]──────► IDLE (+ Snackbar shown)
SAVING ──[error]────────► ERROR
ERROR ──[retry tap]─────► IDLE
```

---

### RecordingUiState

Single sealed value class that the Fragment renders. Keeping all UI state in one object eliminates inconsistency between individual view properties.

```kotlin
data class RecordingUiState(
    val permissionsGranted: Boolean = false,
    val isRecording: Boolean = false,
    val isSaving: Boolean = false,
    val savedFilename: String? = null,      // non-null triggers Snackbar
    val errorMessage: String? = null,       // non-null triggers error UI
    val sampleQuestion: String = SAMPLE_QUESTION
) {
    companion object {
        const val SAMPLE_QUESTION = "Tell me about yourself."
        // TODO: replace with question bank lookup when Requirement 'Question bank' is implemented
    }
}
```

---

### HistoryViewModel

Responsibility: loads `RecordingEntry` list from `RecordingRepository`, exposes it as a sorted `StateFlow`.

```kotlin
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: RecordingRepository
) : ViewModel() {

    val entries: StateFlow<HistoryUiState> = repository
        .getAllEntries()                       // Flow<List<RecordingEntry>>
        .map { list -> HistoryUiState.Success(list) }
        .catch { emit(HistoryUiState.Error("Failed to load recordings")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState.Loading)
}

sealed interface HistoryUiState {
    object Loading : HistoryUiState
    data class Success(val entries: List<RecordingEntry>) : HistoryUiState
    data class Error(val message: String) : HistoryUiState
}
```

---

### RecordingRepository

Responsibility: single source of truth; wraps the DAO and exposes suspend functions / Flows. Keeps the ViewModel ignorant of Room.

```kotlin
class RecordingRepository @Inject constructor(
    private val dao: RecordingDao
) {
    /** Ordered by createdAt DESC — guaranteed by DAO query */
    fun getAllEntries(): Flow<List<RecordingEntry>> = dao.getAllOrderedByDate()

    suspend fun insert(entry: RecordingEntry) = dao.insert(entry)
}
```

---

### RecordingDao

```kotlin
@Dao
interface RecordingDao {
    @Query("SELECT * FROM recording_entries ORDER BY createdAt DESC")
    fun getAllOrderedByDate(): Flow<List<RecordingEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: RecordingEntry)
}
```

---

### LocalStorageHelper

Responsibility: produces deterministic `File` objects for output paths; keeps path logic out of the ViewModel.

```kotlin
class LocalStorageHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Returns a File in app-internal storage with name interview_<timestamp>.mp4.
     * Creates parent directories if absent.
     */
    fun createOutputFile(timestampMs: Long = System.currentTimeMillis()): File {
        val dir = File(context.filesDir, "recordings").also { it.mkdirs() }
        return File(dir, "interview_${timestampMs}.mp4")
    }
}
```

---

## Data Models

### RecordingEntry (Room @Entity + domain model)

```kotlin
// app/model/RecordingEntry.kt
@Entity(tableName = "recording_entries")
data class RecordingEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filename: String,
    val createdAt: Long      // epoch-milliseconds from System.currentTimeMillis()
)
```

### AppDatabase

```kotlin
// app/data/local/AppDatabase.kt
@Database(entities = [RecordingEntry::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recordingDao(): RecordingDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "interview_coach.db")
                .fallbackToDestructiveMigration()   // safe for dev; revisit before release
                .build()
    }
}
```

---

## Data Flow Diagrams

### A. Permission Check → Preview

```
RecordingFragment.onViewCreated()
        │
        ▼
checkAndRequestPermissions()
        │
  ┌─────┴─────┐
  │ Granted?  │
  └─────┬─────┘
     Yes│                   No
        ▼                    ▼
  startCamera()       Show rationale message
        │              (permissionsGranted=false in UiState)
        ▼
  CameraX bindToLifecycle(
      Preview → PreviewView,
      VideoCapture<Recorder>
  )
        │
        ▼
  Preview stream rendered in PreviewView
  Record button enabled
```

### B. Record → Stop → Save → Persist

```
User taps Record
        │
        ▼
RecordingFragment.onRecordClicked()
        │
        ▼
RecorderViewModel.startRecording(outputFile)
        │
        ▼
VideoCapture.output.prepareRecording(context, FileOutputOptions(outputFile))
    .withAudioEnabled()
    .start(executor, listener)          ← returns Recording handle
        │
        ▼
uiState: isRecording=true
(RecordingFragment renders: indicator visible, Stop shown, Record disabled)
        │
        ▼
User taps Stop
        │
        ▼
RecordingFragment.onStopClicked()
        │
        ▼
RecorderViewModel.stopRecording()
    activeRecording.stop()
    uiState: isSaving=true, Stop button immediately disabled
        │
        ▼
CameraX listener receives VideoRecordEvent.Finalize
        │
  ┌─────┴──────┐
  │  Success?  │
  └─────┬──────┘
     Yes│                    No
        ▼                     ▼
LocalStorageHelper          uiState: errorMessage="..."
  (file already written)
        │
        ▼
RecordingRepository.insert(
    RecordingEntry(filename=file.name, createdAt=timestampMs)
)
        │
        ▼
uiState: savedFilename=file.name
(Fragment shows Snackbar for ≥ 3 s)
uiState: isRecording=false, isSaving=false
```

### C. History Screen Load

```
User taps History nav control
        │
        ▼
Navigation.navigate(R.id.historyFragment)
        │
        ▼
HistoryFragment.onViewCreated()
    HistoryViewModel.entries (StateFlow) collected
        │
        ▼
RecordingRepository.getAllEntries()
    RecordingDao.getAllOrderedByDate()   ← Room emits Flow
        │
  ┌─────┴──────────┐
  │  Entries exist?│
  └─────┬──────────┘
     Yes│                      No (empty)
        ▼                        ▼
  RecyclerView renders       Show "No recordings yet"
  list (filename + date)
        │
  Error path: catch {} → HistoryUiState.Error → show error message
```

---

## Dependency Wiring (Hilt)

Hilt is chosen over manual DI. Two Hilt modules cover the data layer; everything else is injected via constructor injection.

```kotlin
// app/di/DatabaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        AppDatabase.create(ctx)

    @Provides @Singleton
    fun provideRecordingDao(db: AppDatabase): RecordingDao = db.recordingDao()
}

// app/di/RepositoryModule.kt
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides @Singleton
    fun provideRecordingRepository(dao: RecordingDao): RecordingRepository =
        RecordingRepository(dao)

    @Provides @Singleton
    fun provideLocalStorageHelper(@ApplicationContext ctx: Context): LocalStorageHelper =
        LocalStorageHelper(ctx)
}
```

`RecorderViewModel` and `HistoryViewModel` are both annotated `@HiltViewModel` and injected via `by viewModels()`. No manual factory classes needed.

---

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

The feature contains pure-function logic (filename generation, date formatting, list ordering, UI-state derivation) that is well-suited for property-based testing using **[Kotest](https://kotest.io/docs/proptest/property-based-testing.html)** (the standard PBT library for Kotlin/JVM). Each property below targets a specific acceptance criterion and can be executed in the JVM unit-test tier with no Android framework dependencies.

---

### Property 1: UI state invariant — recording controls always reflect isRecording

*For any* `RecordingUiState`, the following must hold simultaneously:
- The recording indicator is visible if and only if `isRecording = true`
- The Stop button is visible if and only if `isRecording = true`
- The Record button is enabled if and only if `isRecording = false`
- The sample question label is always visible regardless of `isRecording`

**Validates: Requirements 1.2, 3.4, 3.5, 4.1, 4.7**

---

### Property 2: Sample question displayed without truncation for any valid length

*For any* string of length 1 to 300 characters used as the sample question, the formatted question string produced by the display function should equal the input exactly (no ellipsis appended, no characters removed).

**Validates: Requirements 1.3**

---

### Property 3: Output filename matches required pattern for any timestamp

*For any* `Long` value representing a timestamp in milliseconds, `LocalStorageHelper.createOutputFile(timestampMs).name` should equal `"interview_${timestampMs}.mp4"` and match the regex `^interview_\d+\.mp4$`.

**Validates: Requirements 4.3**

---

### Property 4: Recording entry persistence round-trip

*For any* list of `RecordingEntry` objects (with distinct filenames and varied `createdAt` values), inserting all entries into the Room database and then querying `getAllOrderedByDate()` should return:
- All inserted entries (no entries lost or duplicated)
- Entries ordered by `createdAt` descending

This consolidates the save-entry round-trip (4.6) and the app-restart persistence guarantee (7.2).

**Validates: Requirements 4.6, 7.2**

---

### Property 5: Date formatting is correct for all valid timestamps

*For any* `Long` value `createdAt`:
- If `createdAt ≤ 0`, `formatDate(createdAt)` should return `"Unknown date"`
- If `createdAt > 0`, `formatDate(createdAt)` should return a string matching the pattern `dd MMM yyyy, HH:mm` (verified via `SimpleDateFormat` round-trip or regex `\d{2} [A-Z][a-z]{2} \d{4}, \d{2}:\d{2}`)

**Validates: Requirements 6.1, 6.3**

---

### Property 6: History list ordering for any set of entries

*For any* non-empty list of `RecordingEntry` objects with arbitrary `createdAt` values, the list returned by `HistoryViewModel` (sourced from `RecordingRepository.getAllEntries()`) should satisfy:
- `entries[i].createdAt >= entries[i+1].createdAt` for all consecutive pairs

This is equivalent to verifying that `entries == entries.sortedByDescending { it.createdAt }`.

**Validates: Requirements 5.2**

---

## Error Handling

| Failure Scenario | Detection Point | Handling |
|---|---|---|
| Camera/mic permission denied | `onPermissionsResult` callback | Show rationale string in Fragment; keep Record button hidden |
| CameraX bind failure | `ProcessCameraProvider.bindToLifecycle` throws | `uiState.errorMessage = "Camera unavailable"` |
| CameraX init error (resource busy) | `VideoRecordEvent.Finalize.ERROR_*` before first frame | `uiState.errorMessage = "Microphone in use"` |
| Unrecoverable recording error | `VideoRecordEvent.Finalize` with non-OK error code | Stop recording, `uiState.errorMessage`, re-enable controls |
| File write failure | `VideoRecordEvent.Finalize` error code / IOException | `uiState.errorMessage`, do not show Snackbar |
| Room DB open failure | `Flow.catch {}` in HistoryViewModel | `HistoryUiState.Error`, show error message, app does not crash |
| Room DB read failure | Same Flow error path | Same as above |

All errors are surfaced through `RecordingUiState.errorMessage` or `HistoryUiState.Error`. No bare exceptions are exposed to the UI layer. The user can always retry by tapping Record again (controls are re-enabled after every error).

---

## Testing Strategy

### Dual Testing Approach

Both unit/property tests and instrumented tests are used. Unit tests cover pure logic in isolation; property tests verify universal invariants; instrumented tests verify CameraX and Room wiring.

### Unit & Property Tests (JVM, `src/test/`)

Use **Kotest** for property-based tests and **JUnit 4/5** + **MockK** for example-based unit tests.

```kotlin
// build.gradle.kts (app module, testImplementation)
testImplementation("io.kotest:kotest-runner-junit5:5.x.x")
testImplementation("io.kotest:kotest-property:5.x.x")
testImplementation("io.mockk:mockk:1.x.x")
```

Each property test must run a **minimum of 100 iterations** (Kotest default). Tag each test with a comment in the format:

```
// Feature: recording-screen, Property N: <property text>
```

**Property tests to implement:**

| Test class | Property | Min iterations |
|---|---|---|
| `RecordingUiStatePropertyTest` | Property 1 — control invariant | 100 |
| `SampleQuestionDisplayPropertyTest` | Property 2 — no truncation ≤ 300 chars | 100 |
| `LocalStorageHelperPropertyTest` | Property 3 — filename pattern | 100 |
| `RecordingRepositoryPropertyTest` | Property 4 — round-trip persistence (in-memory Room) | 100 |
| `DateFormatterPropertyTest` | Property 5 — date/unknown-date format | 1000 |
| `HistoryViewModelPropertyTest` | Property 6 — descending order | 100 |

**Example-based unit tests to implement:**

- `RecorderViewModelTest`: verify state transitions for start/stop/error using a fake CameraX recorder
- `LocalStorageHelperTest`: verify directory creation, unique filenames per millisecond
- `RecordingRepositoryTest`: insert single entry, verify retrieval (in-memory Room DB)
- `HistoryViewModelTest`: empty state → "No recordings yet", error state → error message, non-empty → list shown

### Instrumented Tests (`src/androidTest/`)

Use **Espresso** + **Hilt testing support**.

- `RecordingFragmentPermissionTest`: grant/deny permissions via `GrantPermissionRule`, verify correct UI states
- `HistoryFragmentTest`: pre-populate in-memory Room DB, open HistoryFragment, verify RecyclerView items
- `AppDatabaseTest`: end-to-end Room DAO test using `allowMainThreadQueries()`

### Deferred Testing

The following are explicitly out of scope for this spec and should NOT be stubbed:
- CameraX end-to-end video capture (requires physical device or emulator with camera)
- Firebase integration tests
- ML Kit inference tests

---

## Deferred Features (TODO)

The following integration points exist in the code but are not implemented. Each must have a `// TODO` comment at the call site.

```kotlin
// TODO: Replace hardcoded SAMPLE_QUESTION with QuestionBankRepository lookup
//       when the question-bank feature spec is implemented.

// TODO: After saving RecordingEntry, enqueue ML Kit speech-to-text analysis
//       (filler detection, pace analysis) via WorkManager when that spec is implemented.

// TODO: After saving RecordingEntry, enqueue Firebase Storage upload
//       when the Firebase backend spec is implemented.

// TODO: Enable video playback from HistoryFragment item click
//       when the playback spec is implemented.

// TODO: Pass RecordingEntry.filename to scoring engine after transcription
//       when the scoring spec is implemented.

// TODO: Integrate face/eye-contact detection via ML Kit CameraX pipeline
//       when the vision spec is implemented.
```
