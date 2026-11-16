/*
 * Root build file.
 *
 * This file declares the Gradle plugins used by the project.
 * Individual modules decide which plugins they actually apply.
 */

plugins {
    // Android Gradle Plugin (AGP).
    // It adds Android-specific build tasks, such as compiling the app,
    // packaging resources and generating APK files.
    id("com.android.application") version "9.3.1" apply false

    // Kotlin Compose plugin.
    // It enables Kotlin compiler support for Jetpack Compose,
    // Android's modern declarative UI toolkit.
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.10" apply false
}
