# Keep model classes for Gson serialization
-keep class com.tollfuelpro.app.models.** { *; }

# Keep OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Keep Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep Mapbox service
-keep class com.tollfuelpro.app.services.MapboxService { *; }
