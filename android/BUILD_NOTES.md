# Android project setup

Open this `android` directory in Android Studio, then choose **File > Sync Project with Gradle Files**.
Use the bundled Android Studio JDK (17 or 21). The wrapper pins Gradle 8.7;
AGP is 8.5.2, Kotlin is 1.9.25, Compose Compiler is 1.5.15, and Hilt is 2.51.1.
Install Android SDK Platform 34 and Build Tools 34.0.0 if prompted.

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
```

The app includes the home summary, shoe and running lists, entry/edit forms,
local persistence, and a separate AI design example screen. See UI_CHANGES.md
for input rules, behavior, validation, and remaining integration work.

Login, server sync, GPS tracking, and live AI recommendations are not connected.
The backend route handlers and AI implementation are still placeholders.
API_BASE_URL and Maps/Gemini API keys must be configured before network integration.
An emulator reaches the host computer at 10.0.2.2, while localhost is the emulator itself.

The manifest does not declare the missing LocationTrackingService. Add it when
the service and runtime permission handling have been implemented.

Compatibility references:
- https://developer.android.com/build/releases/agp-8-5-0-release-notes
- https://developer.android.com/jetpack/androidx/releases/compose-kotlin
- https://developer.android.com/studio/write/java8-support
