# NanoHTTPD
-keep class fi.iki.elonen.** { *; }

# JSON
-keep class org.json.** { *; }

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep service and receiver classes
-keep class com.powerbx.astro.ledservice.** { *; }
