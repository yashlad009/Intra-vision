# AI Interview Coach — Project Conventions

## Stack
- Android, Kotlin, minSdk 24+
- CameraX for recording (not Camera2 directly)
- Firebase (Firestore, Auth, Storage) for backend — add later, not in first spec
- ML Kit for face detection, Android SpeechRecognizer for speech-to-text — add later
- MPAndroidChart for progress graphs — add later

## Package structure
app/
  ui/ (recording, questionbank, report, history)
  speech/
  vision/
  scoring/
  data/ (local, remote)
  model/

## Team context
- College mini-project (UAMIL0571), 3-4 person team, ~15 week timeline
- Building incrementally: recording UI first, then question bank, then speech,
  then vision, then scoring, then report — do NOT jump ahead to later modules
  unless explicitly asked

## Style
- Keep each module decoupled so team members can work in parallel
- Prefer simple, explainable logic over black-box ML where the course allows it
- Add TODO comments for anything deferred to a later spec, don't silently skip it