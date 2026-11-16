/*
 * Gradle settings file.
 *
 * This file defines how the project is structured and where Gradle
 * can find the plugins and dependencies required by the project.
 */

// Defines where Gradle should look for plugins declared in build.gradle.kts files.
pluginManagement {
    repositories {
        // Google's Maven repository contains Android Gradle Plugin and Android libraries.
        google()

        // Maven Central hosts many Java and Kotlin libraries.
        mavenCentral()

        // Gradle Plugin Portal hosts plugins published specifically for Gradle.
        gradlePluginPortal()
    }
}

// Defines where Gradle should look for project dependencies (libraries).
dependencyResolutionManagement {

    // Prevents individual modules from declaring their own repositories.
    // This keeps dependency resolution centralized and consistent across the project.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
    }
}

// Sets the name displayed by Gradle for this project.
rootProject.name = "Intruder"

// Declares the modules that belong to this Gradle project.
// The application itself will live in the "app" directory.
include(":app")
