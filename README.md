# One Click Release Install on iOS

An Android Studio plugin that adds a dedicated toolbar button for installing the current Flutter
project on a physical iPhone or iPad in release mode.

The plugin runs:

```text
flutter run --release --no-resident -d <selected-device-id>
```

Flutter therefore builds, installs, and launches the app, then exits without leaving a resident
terminal process.

## Features

- Adds an independent toolbar action without changing the normal Run configuration.
- Detects supported physical iOS devices through `flutter devices --machine`.
- Uses the only connected iOS device automatically or opens a chooser when several are connected.
- Can remember a selected device per project and use it automatically whenever it is available.
- Finds Flutter from `android/local.properties`, `FLUTTER_ROOT`, `PATH`, or common SDK locations.
- Shows Flutter output in Android Studio's Run tool window.
- Reports success and failure through IDE notifications.
- Prevents accidentally starting two release installations for the same project.

## Compatibility

- Android Studio based on IntelliJ Platform 2026.1 (`261`) or newer.
- macOS with Flutter and Xcode configured for physical-device iOS development.
- A Flutter application project containing `pubspec.yaml`.

## Install the built plugin

1. Open Android Studio settings.
2. Select **Plugins**.
3. Open the gear menu and choose **Install Plugin from Disk…**.
4. Select the ZIP file.
5. Restart Android Studio if requested.

The **Install Flutter Release on iOS** action is added to the right side of the main toolbar, before
Search Everywhere. It is also available under **Tools** as a fallback. If the toolbar is too narrow,
Android Studio may place it in the toolbar overflow menu.

## Build

The project uses Java 21, Gradle 9.1, and IntelliJ Platform Gradle Plugin 2.18.1.

```bash
./gradlew clean test buildPlugin
```

The installable ZIP is written to `build/distributions/`.

## Notes

- The app is signed using the Flutter/Xcode signing configuration already present in the project.
- The plugin deliberately does not modify existing Android Studio Run configurations.
