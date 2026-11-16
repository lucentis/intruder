/*
 * Android application module configuration.
 *
 * This file defines how the "app" module is built, which Android
 * versions it supports and which libraries it uses.
 */

plugins {
    // Applies the Android application plugin declared in the root build file.
    id("com.android.application")

    // Enables compiler support for Jetpack Compose.
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    // Unique package namespace used for generated Android classes and resources.
    namespace = "fr.lucentis.intruder"

    // Android API level used to compile the application.
    // This does not define the minimum Android version supported by the app.
    compileSdk = 37

    defaultConfig {
        // Unique identifier installed on the device.
        applicationId = "fr.lucentis.intruder"

        // Minimum Android API level required to install the application.
        minSdk = 29

        // Android API level whose behavior the app is designed and tested for.
        targetSdk = 36

        // Internal version number used by Android to identify app updates.
        versionCode = 1

        // Human-readable version displayed to users.
        versionName = "0.1.0"
    }

    // Enables Jetpack Compose support in this Android module.
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Compose BOM (Bill of Materials).
    // It centralizes compatible versions for Compose libraries,
    // so individual Compose dependencies do not need explicit versions.
    val composeBom = platform("androidx.compose:compose-bom:2026.09.00")

    implementation(composeBom)

    // Integration between Android Activity and Jetpack Compose.
    implementation("androidx.activity:activity-compose:1.13.0")

    // Material Design 3 components for Compose.
    implementation("androidx.compose.material3:material3")

    // Core Compose UI APIs.
    implementation("androidx.compose.ui:ui")

    // Helpers for displaying Compose previews in Android Studio.
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Development-only tools for inspecting Compose layouts.
    debugImplementation("androidx.compose.ui:ui-tooling")
}
