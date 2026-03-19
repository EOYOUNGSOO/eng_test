import java.io.FileOutputStream
import java.net.URL

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
}

// OneDrive 등에서 build 폴더 잠금 시 clean 실패 방지: 빌드 출력을 %TEMP%로 이동
val customBuildDir = file("${System.getenv("TEMP") ?: System.getProperty("java.io.tmpdir")}/eng_test_build/app").also { it.mkdirs() }
layout.buildDirectory.set(customBuildDir)

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
    namespace = "com.example.engtest"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.engtest"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

    // JSON 파싱 (assets 단어 데이터 로드용)
    implementation(libs.gson)

    // Free Dictionary API (발음 기호 조회)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)

    // Room DB
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // WorkManager (발음기호 백그라운드 조회)
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
