# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /sdk/tools/proguard/proguard-android.txt

# ═══════════════════════════════════════════
# Gson 파싱 관련 (릴리즈 R8 + Gson 런타임 오류 방지)
# ═══════════════════════════════════════════

-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes Exceptions

# Gson 내부
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**
-dontwarn sun.misc.**

-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * extends com.google.gson.reflect.TypeToken
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# @SerializedName 필드 (필드명 난독화 시에도 JSON 매핑 유지)
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ── 초기화 JSON: 교육부 어휘 (EducationVocabRoot / EducationVocabItem) ──
-keep class com.example.engtest.data.model.** { *; }
-keepclassmembers class com.example.engtest.data.model.** { *; }

# WordAssetLoader 내부 Gson DTO
-keep class com.example.engtest.data.loader.** { *; }

# ── Free Dictionary API 응답 모델 ──
-keep class com.example.engtest.data.remote.model.** { *; }
-keepclassmembers class com.example.engtest.data.remote.model.** { *; }

# data.remote 패키지 DTO (DictionaryEntryDto 등 — model 하위가 아님)
-keep class com.example.engtest.data.remote.DictionaryEntryDto { *; }
-keep class com.example.engtest.data.remote.PhoneticDto { *; }

# ── Room Entity ──
-keep class com.example.engtest.data.entity.** { *; }
-keepclassmembers class com.example.engtest.data.entity.** { *; }

# 도메인 모델 중 Gson/직렬화 연동 (WordDetail 캐시 JSON 등)
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
