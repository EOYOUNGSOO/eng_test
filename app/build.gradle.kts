import java.io.FileOutputStream
import java.net.URL
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
}

// 빌드 출력은 기본 `app/build` 사용.
// (이전 `.build-tmp/app` 커스텀 경로는 Windows에서 lint 캐시 JAR 잠금으로 `:clean` 실패가 잦음.)

// Pretendard 폰트 자동 다운로드 (res/font, 무료 오픈소스)
val fontDir = file("src/main/res/font")
val pretendardMedium = file("src/main/res/font/pretendard_medium.otf")
val pretendardBold = file("src/main/res/font/pretendard_bold.otf")
tasks.register("downloadPretendard") {
    doLast {
        if (!pretendardMedium.exists() || !pretendardBold.exists()) {
            fontDir.mkdirs()
            if (!pretendardMedium.exists()) {
                URL("https://cdn.jsdelivr.net/gh/fonts-archive/Pretendard@main/Pretendard-Medium.otf").openStream().use { input ->
                    FileOutputStream(pretendardMedium).use { it.write(input.readBytes()) }
                }
            }
            if (!pretendardBold.exists()) {
                URL("https://cdn.jsdelivr.net/gh/fonts-archive/Pretendard@main/Pretendard-Bold.otf").openStream().use { input ->
                    FileOutputStream(pretendardBold).use { it.write(input.readBytes()) }
                }
            }
        }
    }
}
tasks.named("preBuild").configure { dependsOn("downloadPretendard") }

android {
    namespace = "com.euysoo.engtest"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.euysoo.engtest"
        minSdk = 26
        targetSdk = 35
        versionCode = 7
        versionName = "1.2.4"

        val localPropsFile = rootProject.file("local.properties")
        val geminiApiKey =
            if (localPropsFile.exists()) {
                Properties().apply { localPropsFile.inputStream().use { load(it) } }
                    .getProperty("gemini.api.key", "")
                    .orEmpty()
            } else {
                ""
            }
        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${geminiApiKey.replace("\\", "\\\\").replace("\"", "\\\"")}\"",
        )
        // 16KB 메모리 페이지 기기: 64비트 ABI만 포함(ML Kit/CameraX 등 .so 정렬·용량 정리)
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }
    androidResources {
        localeFilters += listOf("ko", "en")
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }
    // APK/AAB 내 JNI를 비압축·ZIP 16KB 정렬로 넣어 16KB 페이지 크기 기기 호환성 향상
    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

// Windows: dexBuilder 증분 단계에서 기존 .dex deleteIfExists 가 AccessDenied 로 실패하는 경우 완화.
// 해당 variant 의 project_dex_archive .../out 을 task 시작 시 통째로 제거(증분 DEX는 느려질 수 있음).
tasks.matching {
    it.name.startsWith("dexBuilder") && it.name.length > "dexBuilder".length
}.configureEach {
    doFirst {
        val suffix = name.removePrefix("dexBuilder")
        if (suffix.isEmpty()) return@doFirst
        val variantDir = suffix.replaceFirstChar { it.lowercaseChar() }
        val outDir =
            layout.buildDirectory
                .get()
                .asFile
                .resolve("intermediates/project_dex_archive/$variantDir/dexBuilder$suffix/out")
        runCatching {
            if (outDir.exists()) {
                outDir.deleteRecursively()
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.androidx.navigation.compose)

    // JSON (컴파일 타임 직렬화, 수동 파싱·Retrofit ResponseBody와 함께 사용)
    implementation(libs.kotlinx.serialization.json)

    // Free Dictionary API (발음 기호 조회)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Room DB
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // WorkManager (발음기호 백그라운드 조회)
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // ML Kit OCR (온디바이스, 번들) — Latin + Korean 이중 인식
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("com.google.mlkit:text-recognition-korean:16.0.1")
    val cameraxVersion = "1.3.4"
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")

    // Gemini (Google AI SDK) — 온라인 고정밀 OCR
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
