# ─── Kotlin / Compose ─────────────────────────────────────
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# ─── Hilt ──────────────────────────────────────────────────
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel

# ─── Retrofit + Gson ──────────────────────────────────────
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.scansafe.data.remote.dto.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ─── OkHttp ───────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# ─── Room ─────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# ─── ML Kit ───────────────────────────────────────────────
-keep class com.google.mlkit.** { *; }

# ─── Coil ─────────────────────────────────────────────────
-keep class coil.** { *; }

# ─── Firebase ─────────────────────────────────────────────
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ─── Domain Models ────────────────────────────────────────
-keep class com.scansafe.domain.model.** { *; }

# ─── Timber ───────────────────────────────────────────────
-dontwarn org.jetbrains.annotations.**