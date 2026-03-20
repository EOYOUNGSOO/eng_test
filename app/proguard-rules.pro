# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /sdk/tools/proguard/proguard-android.txt

# Room entities
-keep class com.example.engtest.data.entity.** { *; }

# Retrofit/Gson models
-keep class com.example.engtest.data.remote.model.** { *; }

# 교육부 어휘 JSON (WordSyncManager, WordAssetLoader) — R8 난독화 시 Gson 실패 방지
-keep class com.example.engtest.data.model.** { *; }

# WordAssetLoader 내부 Gson용 DTO (private WordJson 등)
-keep class com.example.engtest.data.loader.** { *; }

-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Gson: 제네릭/리플렉션용 시그니처 유지 (List<EducationVocabItem> 등)
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Gson
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**
-dontwarn sun.misc.**

# OkHttp/Okio
-dontwarn okhttp3.**
-dontwarn okio.**
