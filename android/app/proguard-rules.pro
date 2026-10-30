# Gson reads these models through reflection, including in release builds.
-keep class com.runtrack.data.models.** { *; }
-keep class com.runtrack.data.api.WeatherAdvice { *; }
-keep class com.runtrack.data.api.WeatherHistory { *; }
-keep class com.runtrack.data.api.Location { *; }
-keep class com.runtrack.data.api.WeatherData { *; }
