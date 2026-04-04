pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "EngTest"
include(":app")
// 로컬 build-cache 끄기: Gradle 8.13에서 local.enabled 는 DirectoryBuildCache 비공개라
// settings에서 설정 불가. 대신 gradle.properties 의 org.gradle.caching=false 사용.
