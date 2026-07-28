# Requirements Document

## Introduction

The Recording Screen is the first vertical slice of the AI Interview Coach Android app. It gives users a single, self-contained screen where they can practise answering a sample interview question by recording a video/audio response. Completed recordings are saved to app-internal storage and are visible in a history list screen. No network connectivity, scoring, or analysis is involved at this stage.

## Glossary

- **Recording_Screen**: The primary UI screen (`app/ui/recording`) that presents a question and recording controls to the user.
- **History_Screen**: The secondary UI screen (`app/ui/recording`) that lists past recordings by filename and date.
- **Recorder**: The CameraX-backed component (`app/ui/recording/RecorderViewModel`) responsible for starting and stopping video+audio capture.
- **Local_Storage**: App-internal private storage (`app/data/local`), equivalent to `Context.getFilesDir()` / `MediaStore` internal volume — no external or cloud storage.
- **Recording_File**: An `.mp4` file produced by the Recorder and persisted to Local_Storage.
- **Recording_Entry**: A data object (`filename: String`, `createdAt: Long`) representing a saved Recording_File.
- **Camera_Permission**: Android `android.permission.CAMERA` runtime permission.
- **Microphone_Permission**: Android `android.permission.RECORD_AUDIO` runtime permission.
- **Sample_Question**: A single hardcoded interview question string displayed on the Recording_Screen (e.g. "Tell me about yourself.").

---

## Requirements

### Requirement 1: Display Sample Question

**User Story:** As a student preparing for interviews, I want to see an interview question on screen while recording, so that I know what I am responding to.

#### Acceptance Criteria

1. THE Recording_Screen SHALL display the Sample_Question as a non-editable text label in the uppermost content region of the screen, above all recording controls.
2. WHILE a recording session is active, THE Recording_Screen SHALL continue to display the Sample_Question so the user can refer to it throughout their response.
3. THE Recording_Screen SHALL display a Sample_Question of up to 300 characters without truncation or layout overflow.

---

### Requirement 2: Request Required Permissions

**User Story:** As a student, I want the app to ask for camera and microphone access before recording, so that the recording works correctly without manual settings changes.

#### Acceptance Criteria

1. WHEN the Recording_Screen is first displayed, THE Recording_Screen SHALL check whether Camera_Permission and Microphone_Permission have been granted.
2. IF Camera_Permission or Microphone_Permission has not been granted (regardless of whether the other permission is already granted), THEN THE Recording_Screen SHALL present the Android system permission dialog requesting the missing permission(s) before enabling recording controls.
3. IF the user denies Camera_Permission or Microphone_Permission, THEN THE Recording_Screen SHALL display an explanatory message stating that camera and microphone access are required to record an answer.
4. WHILE Camera_Permission and Microphone_Permission are both granted, THE Recording_Screen SHALL display a live camera preview.

---

### Requirement 3: Record Video and Audio

**User Story:** As a student, I want to tap a Record button to start capturing my video and audio response, so that my practice session is preserved.

#### Acceptance Criteria

1. WHILE Camera_Permission and Microphone_Permission are both granted, THE Recording_Screen SHALL display a Record button.
2. WHEN the user taps the Record button, THE Recorder SHALL begin capturing video and audio simultaneously using CameraX `VideoCapture` with `AudioConfig.AUDIO_ENABLED`, with capture starting within 1 second of the tap.
3. IF the Recorder fails to initialise due to hardware issues or resource constraints, THEN THE Recording_Screen SHALL display an error message indicating the cause category (e.g. "Camera unavailable" or "Microphone in use") and keep the Record button enabled so the user can retry.
4. WHILE recording is in progress, THE Recording_Screen SHALL continuously display a recording indicator (e.g. a red dot or "REC" label) that is visually distinct from the idle state, so the user knows capture is active.
5. WHILE recording is in progress, THE Recording_Screen SHALL disable the Record button so it cannot be tapped again until recording stops.
6. THE Recorder SHALL produce video at a minimum resolution of 480p (720 × 480) encoded as H.264/AAC in an `.mp4` container.
7. IF the Recorder encounters an unrecoverable error while recording is in progress, THEN THE Recorder SHALL stop the active capture, THE Recording_Screen SHALL display an error message indicating the failure, and recording controls SHALL be re-enabled so the user can retry.

---

### Requirement 4: Stop Recording and Save File

**User Story:** As a student, I want to tap a Stop button to finish my recording and have it automatically saved, so that I do not have to manage files manually.

#### Acceptance Criteria

1. WHILE recording is in progress, THE Recording_Screen SHALL display a Stop button; WHILE recording is not in progress, THE Recording_Screen SHALL hide the Stop button.
2. WHEN the user taps the Stop button, THE Stop button SHALL be disabled immediately to prevent double-tap during finalisation, THE Recorder SHALL stop the active recording and finalise the `.mp4` file, and recording controls SHALL be re-enabled upon completion.
3. WHEN the Recorder finalises an `.mp4` file, THE Local_Storage SHALL persist the Recording_File to the app-internal files directory with a filename in the format `interview_<timestamp_ms>.mp4`.
4. WHEN a Recording_File is successfully saved, THE Recording_Screen SHALL display a confirmation message (e.g. a Snackbar) showing the saved filename for at least 3 seconds.
5. IF the Recorder encounters an error during recording or saving, THEN THE Recording_Screen SHALL display an error message describing the failure and SHALL NOT display a success confirmation message.
6. WHEN a Recording_File is successfully saved, THE Recorder SHALL create a corresponding Recording_Entry and persist it via the `RecordingRepository` in `app/data/local`.
7. WHEN the Recorder stops, THE Recording_Screen SHALL hide the recording indicator that was shown during capture.

---

### Requirement 5: Navigate to History Screen

**User Story:** As a student, I want to view a list of my past recordings, so that I can track how many practice sessions I have completed.

#### Acceptance Criteria

1. THE Recording_Screen SHALL provide a persistently visible navigation control (e.g. a top-bar icon or dedicated button) that opens the History_Screen.
2. WHEN the History_Screen is opened, THE History_Screen SHALL display a list of all Recording_Entries from the `RecordingRepository` reflecting the current state of the repository at the time of display, ordered by `createdAt` descending (newest first).
3. IF the `RecordingRepository` fails to load Recording_Entries, THEN THE History_Screen SHALL display an error message and SHALL NOT show a partial list.
4. WHILE the `RecordingRepository` contains no Recording_Entries, THE History_Screen SHALL display the empty-state message "No recordings yet".

---

### Requirement 6: Display Recording Details in History

**User Story:** As a student, I want each past recording to show its filename and date, so that I can identify sessions at a glance.

#### Acceptance Criteria

1. THE History_Screen SHALL display each Recording_Entry as a list item showing the `filename` (truncated to 100 characters if longer) and the `createdAt` timestamp formatted as `dd MMM yyyy, HH:mm`.
2. THE History_Screen SHALL display the list in a scrollable container so all entries are reachable when many are present.
3. IF a Recording_Entry has a `createdAt` value of 0 or less, THE History_Screen SHALL display "Unknown date" in place of the formatted timestamp for that entry.

---

### Requirement 7: Data Persistence

**User Story:** As a student, I want my recording history to survive app restarts, so that I can review past sessions after closing the app.

#### Acceptance Criteria

1. THE `RecordingRepository` SHALL store Recording_Entries in a Room database located in `app/data/local`.
2. WHEN the app is restarted, THE History_Screen SHALL reload Recording_Entries from the Room database and display the same entries (matching `filename` and `createdAt` values) in the same `createdAt` descending order as before the restart.
3. WHEN the app is launched after a Room database schema change, THE app SHALL launch successfully and THE History_Screen SHALL remain accessible (using destructive migration during development to avoid crash on schema mismatch).
4. IF the Room database fails to open or read on app launch, THEN THE History_Screen SHALL display an error message and SHALL NOT crash the app.

---

## Out of Scope (Future Work)

The following items are explicitly deferred and MUST NOT be implemented in this spec. Each deferred area should have a `// TODO` comment at the relevant integration point in code.

- **Question bank / multiple questions** — hardcoded single question only.
- **Speech-to-text, filler detection, pace analysis** — no audio processing.
- **Face / eye-contact detection** — no ML Kit vision in this slice.
- **Firebase or any cloud backend** — Local_Storage only.
- **Scoring or report screens** — no analytics or feedback UI.
- **Video playback from the History Screen** — list view only.
