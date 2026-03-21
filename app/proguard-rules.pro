# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /sdk/tools/proguard/proguard-android.txt

# ═══════════════════════════════════════════
# 공통 (Room / Retrofit / kotlinx.serialization)
# ═══════════════════════════════════════════

-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes Exceptions
-keepattributes RuntimeVisibleAnnotations

# kotlinx.serialization (컴파일 생성 직렬화기 유지)
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}

# ── data 모델 (JSON 직렬화) ──
-keep class com.example.engtest.data.model.** { *; }
-keepclassmembers class com.example.engtest.data.model.** { *; }

-keep class com.example.engtest.data.remote.model.** { *; }
-keepclassmembers class com.example.engtest.data.remote.model.** { *; }

-keep interface com.example.engtest.data.remote.DictionaryApiService { *; }
-keep class com.example.engtest.data.remote.DictionaryEntryDto { *; }
-keep class com.example.engtest.data.remote.PhoneticDto { *; }

# WordAssetLoader 내부 직렬화 DTO
-keep class com.example.engtest.data.loader.** { *; }

# ── Room Entity ──
-keep class com.example.engtest.data.entity.** { *; }
-keepclassmembers class com.example.engtest.data.entity.** { *; }

# 도메인 모델
-keep class com.example.engtest.domain.model.** { *; }

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp / Okio
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
