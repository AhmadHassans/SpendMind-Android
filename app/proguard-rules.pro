# SpendMind ProGuard Rules

# Keep Hilt generated components
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }

# Keep Room entities and DAOs
-keep class com.spendmind.app.core.data.database.entities.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Database class * { *; }

# Keep Retrofit and OkHttp
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**

# Keep Gson serialization
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.spendmind.app.core.data.network.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep domain models
-keep class com.spendmind.app.core.domain.model.** { *; }

# Keep repositories
-keep class com.spendmind.app.core.data.repository.** { *; }

# iText7
-keep class com.itextpdf.** { *; }
-dontwarn com.itextpdf.**

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }

# Kotlin Coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# Mixpanel
-keep class com.mixpanel.android.** { *; }
-dontwarn com.mixpanel.**

# Suppress warnings for unused classes in dependencies
-dontwarn javax.annotation.**
-dontwarn sun.misc.**
