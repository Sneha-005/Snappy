# Repo Information

- Name: Snappy
- Platform: Android (Jetpack Compose)
- Language: Kotlin
- Min SDK: 24
- Target/Compile SDK: 35
- Key modules: Single `app` module

## Key Files
- app/src/main/java/com/devsneha/snappy/ui/SnappyCanvas.kt — existing canvas and tools implementation
- app/src/main/java/com/devsneha/snappy/ui/SnappyCanvasViewModel.kt — new ViewModel (state + gestures + undo/redo)
- app/src/main/java/com/devsneha/snappy/model/CanvasModels.kt — render and interaction models
- app/src/main/java/com/devsneha/snappy/data/CanvasRepository.kt — persistence abstraction

## Build
- Compose enabled
- Lifecycle ViewModel + ViewModel Compose available
- M3 Material

## Notes
- VM-backed screen `SnappyCanvasScreen2` added (non-breaking). Switch MainActivity to it when ready.