// Plugins are declared with a version in the root build file; here the module applies them.
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    // Package of the generated R class (resource ids) and base for relative names
    // in the manifest (".MainActivity" means "<namespace>.MainActivity").
    namespace = "fr.lucentis.intruder"

    // Android API the code is compiled against: it decides which Android APIs you can call.
    compileSdk = 37

    defaultConfig {
        // Identity of the app on a device and on stores. It differs from the prototype's id,
        // so both apps can be installed side by side.
        applicationId = "fr.lucentis.intruder"

        // Oldest Android version that can install the app (31 = Android 12).
        minSdk = 31

        // Android version whose behavior the app declares it supports. Android applies
        // behavior changes (background limits, edge-to-edge, ...) according to this value.
        targetSdk = 36

        // versionCode: integer the system uses to compare versions, must only increase.
        // versionName: free text shown to the user.
        versionCode = 1
        versionName = "0.1.0"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // BOM (Bill of Materials): pins mutually compatible versions of all Compose libraries,
    // so the individual Compose dependencies below declare no version.
    val composeBom = platform("androidx.compose:compose-bom:2026.09.00")

    // implementation: on the classpath when compiling and running the app.
    implementation(composeBom)
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.13.0") // setContent {} for Compose
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")

    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    // ksp: processor that runs at build time and generates code. It is not shipped in the APK.
    ksp("androidx.room:room-compiler:2.8.5")

    implementation("androidx.datastore:datastore-preferences:1.2.0")

    // debugImplementation: only in debug builds (developer tooling).
    debugImplementation("androidx.compose.ui:ui-tooling")

    // testImplementation: only for local unit tests (src/test), which run on the JVM.
    testImplementation("junit:junit:4.13.2")
}