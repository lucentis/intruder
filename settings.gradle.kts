// Where Gradle downloads the build PLUGINS from (Android Gradle Plugin, KSP, ...).
// google() is Google's Maven repository: it hosts the Android Gradle Plugin and the
// AndroidX libraries. It has nothing to do with Google accounts or services.
pluginManagement {
    repositories {
        google()
        mavenCentral() // general-purpose libraries (JUnit, kotlinx, ...)
        gradlePluginPortal() // community Gradle plugins
    }
}

// Where Gradle downloads the app's DEPENDENCIES (the libraries it uses) from.
// FAIL_ON_PROJECT_REPOS forbids modules from declaring their own repositories,
// so every download source is listed once, here.
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "intruder"

// A Gradle module is a buildable unit. ":app" is the only one: it produces the APK.
include(":app")