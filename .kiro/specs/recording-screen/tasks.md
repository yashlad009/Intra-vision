# Implementation Plan: Recording Screen

## Overview

Implements the first vertical slice of the AI Interview Coach Android app in Kotlin.
The plan follows the MVVM architecture defined in the design: a Room data layer, a ViewModel layer (RecorderViewModel + HistoryViewModel), a single-activity Navigation Component host, and two Fragments (RecordingFragment + HistoryFragment). Hilt wires everything together. Kotest property-based tests and Espresso instrumented tests provide correctness coverage.

Tasks are ordered so each step compiles and runs on top of the previous one, keeping merge conflicts minimal for a 3–4 person team. Tasks marked `*` are optional (tests / instrumentation) and can be deferred for a faster MVP.

---

## Tasks

- [x] 1. Project setup — Gradle dependencies and package scaffolding
  - [x] 1.1 Add required dependencies to `app/build.gradle.kts`
    - Add Room (`room-runtime`, `room-ktx`, `room-compiler` kapt/ksp), CameraX (`camera-camera2`, `camera-lifecycle`, `camera-video`, `camera-view`), Hilt (`hilt-android`, `hilt-android-compiler`), Jetpack Navigation (`navigation-fragment-ktx`, `navigation-ui-ktx`), Kotlin Coroutines, and Lifecycle (`lifecycle-viewmodel-ktx`, `lifecycle-runtime-ktx`)
    - Add test dependencies: `kotest-runner-junit5`, `kotest-property`, `mockk`, `kotlinx-coroutines-test`, `room-testing`, `hilt-android-testing`, `espresso-core`, `fragment-testing`
    - Enable kapt/ksp plugin for Room and Hilt annotation processing
    - _Requirements: 3.2, 4.3, 7.1_

  - [x] 1.2 Create package directory skeleton
    - Create empty Kotlin files (or `package` stubs) for all packages: `app/ui/recording`, `app/data/local`, `app/model`, `app/di`
    - Create `AndroidManifest.xml` entries for `CAMERA` and `RECORD_AUDIO` permissions and declare `RecordingActivity`
    - _Requirements: 2.1, 2.2_

- [x] 2. Data layer — Room entity, DAO, database, and Hilt module
  - [x] 2.1 Implement `RecordingEntry` Room entity
    - Create `app/model/RecordingEntry.kt` with `@Entity(tableName = "recording_entries")`, `@PrimaryKey(autoGenerate = true) val id: Long`, `val filename: String`, `val createdAt: Long`
    - _Requirements: 4.6, 6.1, 7.1_

  - [x] 2.2 Implement `RecordingDao`
    - Create `app/data/local/RecordingDao.kt` with `@Dao` interface containing `getAllOrderedByDate()` returning `Flow<List<RecordingEntry>>` (ORDER BY createdAt DESC) and `insert(entry: RecordingEntry)` as a suspend function with `OnConflictStrategy.REPLACE`
    - _Requirements: 5.2, 7.1_

  - [x] 2.3 Implement `AppDatabase`
    - Create `app/data/local/AppDatabase.kt` as `@Database(entities = [RecordingEntry::class], version = 1, exportSchema = false)` abstract class extending `RoomDatabase` with `abstract fun recordingDao(): RecordingDao` and a `companion object` factory using `Room.databaseBuilder(...).fallbackToDestructiveMigration()`
    - _Requirements: 7.1, 7.3, 7.4_

  - [x] 2.4 Implement `DatabaseModule` Hilt module
    - Create `app/di/DatabaseModule.kt` as `@Module @InstallIn(SingletonComponent::class) object DatabaseModule` providing `@Singleton AppDatabase` and `@Singleton RecordingDao`
    - _Requirements: 7.1_

  - [ ]* 2.5 Write unit tests for `RecordingDao` and `AppDatabase` (in-memory Room)
    - Use `Room.inMemoryDatabaseBuilder` in `src/test/` (JVM) or `src/androidTest/` as `AppDatabaseTest`
    - Test: single insert then query returns that entry; two inserts return both entries ordered descending by `createdAt`
    - _Requirements: 7.1, 7.2_

- [x] 3. Data layer — repository and local storage helper
  - [x] 3.1 Implement `LocalStorageHelper`
    - Create `app/data/local/LocalStorageHelper.kt` with `@Inject constructor(@ApplicationContext context: Context)`
    - Implement `createOutputFile(timestampMs: Long = System.currentTimeMillis()): File` — creates `<filesDir>/recordings/` directory if absent and returns `File(dir, "interview_${timestampMs}.mp4")`
    - Add `// TODO: Replace hardcoded path with MediaStore internal volume when targeting API 29+ media APIs`
    - _Requirements: 4.3_

  - [x] 3.2 Implement `RecordingRepository`
    - Create `app/data/local/RecordingRepository.kt` with `@Inject constructor(private val dao: RecordingDao)`
    - Expose `fun getAllEntries(): Flow<List<RecordingEntry>> = dao.getAllOrderedByDate()` and `suspend fun insert(entry: RecordingEntry) = dao.insert(entry)`
    - _Requirements: 4.6, 5.1, 5.2, 7.1_

  - [x] 3.3 Implement `RepositoryModule` Hilt module
    - Create `app/di/RepositoryModule.kt` as `@Module @InstallIn(SingletonComponent::class) object RepositoryModule` providing `@Singleton RecordingRepository` and `@Singleton LocalStorageHelper`
    - _Requirements: 4.6, 7.1_

  - [ ]* 3.4 Write property test — Property 3: filename pattern (`LocalStorageHelperPropertyTest`)
    - `// Feature: recording-screen, Property 3: Output filename matches required pattern for any timestamp`
    - Use Kotest `forAll` with an `Arb.long()` generator (min 100 iterations); assert `createOutputFile(ts).name == "interview_${ts}.mp4"` and matches `^interview_\d+\.mp4$`
    - _Requirements: 4.3_

  - [ ]* 3.5 Write property test — Property 4: round-trip persistence (`RecordingRepositoryPropertyTest`)
    - `// Feature: recording-screen, Property 4: Recording entry persistence round-trip`
    - Use an in-memory Room database; generate lists of `RecordingEntry` objects with `Arb.list(Arb.bind(...))` (min 100 iterations); insert all, query, assert no entries lost/duplicated and result is descending by `createdAt`
    - _Requirements: 4.6, 7.2_

  - [ ]* 3.6 Write example-based unit tests for `LocalStorageHelper` (`LocalStorageHelperTest`)
    - Test: directory is created when absent; two calls with different timestamps return distinct `File` objects; file extension is `.mp4`
    - _Requirements: 4.3_

  - [ ]* 3.7 Write example-based unit tests for `RecordingRepository` (`RecordingRepositoryTest`)
    - Use in-memory Room; insert a single `RecordingEntry`, collect `getAllEntries()` flow, verify exact round-trip match
    - _Requirements: 4.6, 7.1_

- [x] 4. Checkpoint — data layer complete
  - Ensure all data-layer unit tests pass with `./gradlew testDebugUnitTest`. Ask the team if any Room setup questions arise before moving to ViewModels.

- [x] 5. ViewModel layer — RecorderViewModel and UI state
  - [x] 5.1 Define `RecordingUiState` data class
    - Create `app/ui/recording/RecordingUiState.kt` with fields: `permissionsGranted: Boolean = false`, `isRecording: Boolean = false`, `isSaving: Boolean = false`, `savedFilename: String? = null`, `errorMessage: String? = null`, `sampleQuestion: String = SAMPLE_QUESTION`
    - Add `companion object { const val SAMPLE_QUESTION = "Tell me about yourself." }` and a `// TODO` comment for future question-bank lookup
    - _Requirements: 1.1, 1.2, 1.3, 3.4, 3.5, 4.1, 4.4, 4.5_

  - [x] 5.2 Implement `RecorderViewModel`
    - Create `app/ui/recording/RecorderViewModel.kt` as `@HiltViewModel` with `@Inject constructor(val repository: RecordingRepository, val storageHelper: LocalStorageHelper)`
    - Implement `MutableStateFlow<RecordingUiState>` exposed as `uiState: StateFlow<RecordingUiState>`
    - Implement `onPermissionsResult(grants: Map<String, Boolean>)`: sets `permissionsGranted` based on both CAMERA and RECORD_AUDIO being true
    - Implement `startRecording(outputFile: File)`: calls CameraX `prepareRecording(...).withAudioEnabled().start(...)`, sets `isRecording = true`; on `VideoRecordEvent.Start` noop; on `VideoRecordEvent.Finalize` delegates to `onRecordingFinalized` or `onRecordingError`
    - Implement `stopRecording()`: calls `activeRecording?.stop()`, sets `isSaving = true`, disables Stop button immediately
    - Implement private `onRecordingFinalized(result)`: calls `repository.insert(RecordingEntry(filename=file.name, createdAt=ts))` in `viewModelScope`, sets `savedFilename`, resets `isRecording`/`isSaving` to false
    - Implement private `onRecordingError(event)`: sets `errorMessage`, resets recording flags
    - Add all six `// TODO` comments from the design's Deferred Features section
    - State machine transitions: IDLE → RECORDING → SAVING → IDLE (success) / ERROR; ERROR → IDLE on retry
    - _Requirements: 2.3, 2.4, 3.1, 3.2, 3.3, 3.4, 3.5, 3.7, 4.1, 4.2, 4.4, 4.5, 4.6, 4.7_

  - [ ]* 5.3 Write property test — Property 1: UI state control invariant (`RecordingUiStatePropertyTest`)
    - `// Feature: recording-screen, Property 1: UI state invariant — recording controls always reflect isRecording`
    - Use Kotest `forAll` with `Arb.bind` over all `RecordingUiState` boolean fields (min 100 iterations); assert: indicator visible ↔ `isRecording`, Stop visible ↔ `isRecording`, Record enabled ↔ `!isRecording`, question label always visible
    - _Requirements: 1.2, 3.4, 3.5, 4.1, 4.7_

  - [ ]* 5.4 Write property test — Property 2: sample question no truncation (`SampleQuestionDisplayPropertyTest`)
    - `// Feature: recording-screen, Property 2: Sample question displayed without truncation for any valid length`
    - Use `Arb.string(minSize = 1, maxSize = 300)` (min 100 iterations); pass each string through the display formatting function; assert output equals input exactly
    - _Requirements: 1.3_

  - [ ]* 5.5 Write example-based unit tests for `RecorderViewModel` (`RecorderViewModelTest`)
    - Use MockK to create a fake `RecordingRepository` and `LocalStorageHelper`
    - Test: initial state is IDLE with `permissionsGranted = false`; `onPermissionsResult` with both granted → `permissionsGranted = true`; `onPermissionsResult` with one denied → `permissionsGranted = false`; state goes to `isRecording = true` after `startRecording`; state goes to `isSaving = true` after `stopRecording`; `onRecordingFinalized` sets `savedFilename` and clears flags; `onRecordingError` sets `errorMessage` and clears flags
    - _Requirements: 2.3, 3.2, 3.3, 3.4, 3.5, 4.1, 4.2, 4.5, 4.7_

- [x] 6. ViewModel layer — HistoryViewModel
  - [x] 6.1 Define `HistoryUiState` sealed interface
    - In `app/ui/recording/HistoryViewModel.kt` (or a separate file), declare `sealed interface HistoryUiState` with `object Loading`, `data class Success(val entries: List<RecordingEntry>)`, and `data class Error(val message: String)`
    - _Requirements: 5.2, 5.3, 5.4_

  - [x] 6.2 Implement `HistoryViewModel`
    - Create `app/ui/recording/HistoryViewModel.kt` as `@HiltViewModel` with `@Inject constructor(val repository: RecordingRepository)`
    - Expose `val entries: StateFlow<HistoryUiState>` built from `repository.getAllEntries().map { HistoryUiState.Success(it) }.catch { emit(HistoryUiState.Error("Failed to load recordings")) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState.Loading)`
    - _Requirements: 5.2, 5.3, 5.4_

  - [x] 6.3 Implement `formatDate` utility function
    - Add a top-level or companion `fun formatDate(createdAt: Long): String` that returns `"Unknown date"` for `createdAt ≤ 0` and `SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(createdAt))` otherwise
    - _Requirements: 6.1, 6.3_

  - [ ]* 6.4 Write property test — Property 5: date formatting (`DateFormatterPropertyTest`)
    - `// Feature: recording-screen, Property 5: Date formatting is correct for all valid timestamps`
    - Use `Arb.long()` (min 1000 iterations); assert: `createdAt ≤ 0` → `"Unknown date"`; `createdAt > 0` → result matches regex `\d{2} [A-Z][a-z]{2} \d{4}, \d{2}:\d{2}`
    - _Requirements: 6.1, 6.3_

  - [ ]* 6.5 Write property test — Property 6: history list ordering (`HistoryViewModelPropertyTest`)
    - `// Feature: recording-screen, Property 6: History list ordering for any set of entries`
    - Use `Arb.list(Arb.bind(...), range = 1..50)` (min 100 iterations); feed list through `RecordingRepository` (in-memory Room) or a fake; collect `HistoryViewModel.entries` `Success` state; assert `entries == entries.sortedByDescending { it.createdAt }`
    - _Requirements: 5.2_

  - [ ]* 6.6 Write example-based unit tests for `HistoryViewModel` (`HistoryViewModelTest`)
    - Use MockK / fake repository; test: empty list emits `HistoryUiState.Loading` then `HistoryUiState.Success(emptyList())`; non-empty list emits `Success` with correct entries; Flow error emits `HistoryUiState.Error`
    - _Requirements: 5.2, 5.3, 5.4_

- [x] 7. Checkpoint — ViewModel layer complete
  - Run `./gradlew testDebugUnitTest` and verify all property tests reach their minimum iteration counts. Confirm state machine coverage with the team before wiring the UI.

- [x] 8. UI layer — navigation graph and RecordingActivity
  - [x] 8.1 Create Navigation graph XML
    - Add `res/navigation/nav_recording.xml` with `RecordingFragment` as `startDestination` and a `<action>` navigating to `HistoryFragment`; set `enterAnim` / `exitAnim` transitions
    - _Requirements: 5.1_

  - [x] 8.2 Create `activity_recording.xml` layout
    - Add a `FragmentContainerView` as a `NavHostFragment` referencing `nav_recording`; configure edge-to-edge window insets
    - _Requirements: 5.1_

  - [x] 8.3 Implement `RecordingActivity`
    - Create `app/ui/recording/RecordingActivity.kt` as `@AndroidEntryPoint class RecordingActivity : AppCompatActivity()` that calls `setContentView(R.layout.activity_recording)` and applies edge-to-edge setup
    - _Requirements: 5.1_

- [x] 9. UI layer — RecordingFragment layout and fragment
  - [x] 9.1 Create `fragment_recording.xml` layout
    - Add `PreviewView` (CameraX) filling the upper portion of the screen; a `TextView` for the sample question pinned below the preview; a recording indicator (`ImageView` or `TextView` for "REC" red dot) with `visibility="gone"` by default; a Record `Button` and a Stop `Button` (initially `visibility="gone"`)
    - _Requirements: 1.1, 1.2, 2.4, 3.1, 3.4, 4.1_

  - [x] 9.2 Implement `RecordingFragment` — permission gating and CameraX binding
    - Create `app/ui/recording/RecordingFragment.kt` as `@AndroidEntryPoint class RecordingFragment : Fragment(R.layout.fragment_recording)` with `private val viewModel: RecorderViewModel by viewModels()`
    - Register `permissionLauncher` via `registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())` forwarding result to `viewModel.onPermissionsResult`
    - Implement `checkAndRequestPermissions()` called from `onViewCreated`; implement `startCamera()` that binds `Preview` to `PreviewView` and `VideoCapture<Recorder>` to the lifecycle via `ProcessCameraProvider`
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

  - [x] 9.3 Implement `RecordingFragment` — state rendering and button wiring
    - Implement `renderState(state: RecordingUiState)` that updates: question `TextView` text; recording indicator visibility (`isRecording`); Record button enabled state (`!isRecording && !isSaving && permissionsGranted`); Stop button visibility (`isRecording`); Snackbar shown when `savedFilename != null` (≥ 3 s); error message shown when `errorMessage != null`; permission rationale message when `!permissionsGranted`
    - Wire Record button → `viewModel.startRecording(storageHelper.createOutputFile())`; Stop button → `viewModel.stopRecording()`; History nav icon → `findNavController().navigate(R.id.action_recordingFragment_to_historyFragment)`
    - Collect `viewModel.uiState` in `viewLifecycleOwner.lifecycleScope`
    - _Requirements: 1.1, 1.2, 1.3, 2.3, 2.4, 3.1, 3.4, 3.5, 4.1, 4.2, 4.4, 4.5, 4.7, 5.1_

- [ ] 10. UI layer — HistoryFragment, adapter, and layouts
  - [x] 10.1 Create `item_recording.xml` list item layout
    - Add two `TextViews`: `tvFilename` (max 100 chars display, `ellipsize="end"`) and `tvDate` showing the formatted `createdAt`
    - _Requirements: 6.1, 6.2_

  - [ ] 10.2 Implement `RecordingAdapter`
    - Create `app/ui/recording/RecordingAdapter.kt` as a `ListAdapter<RecordingEntry, RecordingAdapter.ViewHolder>` using `DiffUtil`
    - Bind `filename` (truncate to 100 chars in bind) and `formatDate(createdAt)` in each `ViewHolder`
    - Add `// TODO: Enable item click for playback when playback spec is implemented`
    - _Requirements: 6.1, 6.2, 6.3_

  - [x] 10.3 Create `fragment_history.xml` layout
    - Add a `RecyclerView` with `LinearLayoutManager`; a `TextView` for the empty-state "No recordings yet" message; a `TextView` for the error state
    - _Requirements: 5.2, 5.3, 5.4, 6.2_

  - [x] 10.4 Implement `HistoryFragment`
    - Create `app/ui/recording/HistoryFragment.kt` as `@AndroidEntryPoint class HistoryFragment : Fragment(R.layout.fragment_history)` with `private val viewModel: HistoryViewModel by viewModels()`
    - Collect `viewModel.entries` in `viewLifecycleOwner.lifecycleScope`; on `Loading` show progress indicator; on `Success` submit list to adapter (show empty-state when empty); on `Error` show error `TextView`
    - _Requirements: 5.2, 5.3, 5.4, 6.1, 6.2, 6.3_

- [ ] 11. Checkpoint — UI wiring complete
  - Build the project with `./gradlew assembleDebug`; verify no compilation errors. Run the app on an emulator or device and manually confirm: question displays, permissions prompt appears, Record/Stop toggle works, Snackbar appears after stop, History screen loads. Ask the team if any CameraX integration questions arise.

- [ ] 12. Instrumented tests — Hilt + Espresso
  - [ ]* 12.1 Write `RecordingFragmentPermissionTest`
    - Use `GrantPermissionRule` to test both the "permissions granted" path (preview shown, Record button enabled) and the "permissions denied" path (rationale message shown, Record button not shown)
    - _Requirements: 2.2, 2.3, 2.4, 3.1_

  - [ ]* 12.2 Write `HistoryFragmentTest`
    - Pre-populate an in-memory Room DB via a `@TestInstallIn` Hilt override; launch `HistoryFragment` via `FragmentScenario`; verify RecyclerView item count, filename text, and date text match inserted entries
    - _Requirements: 5.2, 6.1, 6.2, 6.3_

  - [ ]* 12.3 Write `AppDatabaseTest`
    - Use `Room.inMemoryDatabaseBuilder` with `allowMainThreadQueries()`; insert multiple entries; collect `getAllOrderedByDate()` flow; assert descending order and correct field values
    - _Requirements: 7.1, 7.2_

- [~] 13. Final checkpoint — all tests pass
  - Run `./gradlew testDebugUnitTest connectedDebugAndroidTest`. Ensure all 6 Kotest property tests reach their minimum iteration counts and all Espresso tests pass. Resolve any failures before marking the feature complete.

---

## Notes

- Tasks marked `*` are optional test/instrumentation tasks and can be skipped for a faster MVP build, but must be completed before the feature is considered fully verified.
- Each task references specific acceptance criteria for traceability; reviewers should use these to cross-check coverage.
- Checkpoints at tasks 4, 7, 11, and 13 provide natural team synchronisation points across the ~15-week timeline.
- Property tests use Kotest `forAll` with a minimum of 100 iterations (1 000 for `DateFormatterPropertyTest`); each must carry the `// Feature: recording-screen, Property N: …` comment.
- The six `// TODO` comments for deferred features (question bank, ML Kit, Firebase, playback, scoring, vision) must appear in code exactly as specified in the design document.
- `fallbackToDestructiveMigration()` is intentional for the development phase (Requirement 7.3); revisit before any public release.

---

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2"] },
    { "id": 1, "tasks": ["2.1"] },
    { "id": 2, "tasks": ["2.2", "2.3"] },
    { "id": 3, "tasks": ["2.4", "3.1"] },
    { "id": 4, "tasks": ["2.5", "3.2"] },
    { "id": 5, "tasks": ["3.3", "3.4", "3.6"] },
    { "id": 6, "tasks": ["3.5", "3.7", "5.1"] },
    { "id": 7, "tasks": ["5.2", "6.1", "6.3"] },
    { "id": 8, "tasks": ["5.3", "5.4", "5.5", "6.2", "6.4"] },
    { "id": 9, "tasks": ["6.5", "6.6", "8.1"] },
    { "id": 10, "tasks": ["8.2"] },
    { "id": 11, "tasks": ["8.3", "9.1"] },
    { "id": 12, "tasks": ["9.2"] },
    { "id": 13, "tasks": ["9.3", "10.1"] },
    { "id": 14, "tasks": ["10.2", "10.3"] },
    { "id": 15, "tasks": ["10.4"] },
    { "id": 16, "tasks": ["12.1", "12.2", "12.3"] }
  ]
}
```
