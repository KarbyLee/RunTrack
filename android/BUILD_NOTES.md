# Android project repair

Open this `android` directory in Android Studio, then choose **File > Sync Project with Gradle Files**.
Use the bundled Android Studio JDK (17 or 21). The Gradle wrapper pins Gradle 8.7;
AGP is 8.5.2, Kotlin is 1.9.25, Compose Compiler is 1.5.15, and Hilt is 2.51.1.
Install Android SDK Platform 34 and Build Tools 34.0.0 if Android Studio prompts.

From a terminal with JAVA_HOME set to a compatible JDK:

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
```

The app currently displays a starter screen. Running history, shoe management screens,
authentication, GPS tracking, and AI features still need implementation. The backend
route handlers are also placeholders. A successful Android build does not imply that
these features are implemented or that backend integration has been verified.

The manifest no longer declares the missing LocationTrackingService. Add its declaration
when the service and runtime location/foreground-service permission handling are implemented.
The JSON DTOs follow the backend schemas and Retrofit uses Gson snake_case mapping.
The repository takes ShoeService through Hilt constructor injection.

For backend integration, configure API_BASE_URL in app/build.gradle.kts. An Android
emulator reaches the host computer at 10.0.2.2; localhost refers to the emulator itself.
Use HTTPS for deployment and add a scoped debug network policy if local HTTP is required.
Maps and Gemini API keys are still placeholders and must be configured before using those features.

Compatibility references:
- https://developer.android.com/build/releases/agp-8-5-0-release-notes
- https://developer.android.com/jetpack/androidx/releases/compose-kotlin
