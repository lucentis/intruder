// Root build file: it only declares plugin versions, in one place.
// "apply false" makes a plugin available to the modules without applying it to the
// root project; each module then applies what it needs by id.
plugins {
    // Android Gradle Plugin (AGP): compiles the project, merges resources and the
    // manifest, and packages the APK. Since AGP 9 it also compiles Kotlin itself.
    id("com.android.application") version "9.3.1" apply false

    // Kotlin compiler plugin required by Jetpack Compose: it rewrites @Composable
    // functions at compile time. Its version is the Kotlin version of the build.
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.10" apply false

    // Kotlin Symbol Processing: runs annotation processors during the build.
    // Room uses it to generate the database code from its annotations.
    id("com.google.devtools.ksp") version "2.3.11" apply false
}