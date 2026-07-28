plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // KSP drives both Room and Hilt annotation processing
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.navigation.safeargs)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.aiinterviewcoach"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.aiinterviewcoach"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.example.aiinterviewcoach.HiltTestRunner"
    }

    // Required for Kotest JUnit 5 runner in unit tests
    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
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
        viewBinding = true
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Production dependencies
// ──────────────────────────────────────────────────────────────────────────────

dependencies {

    // Core AndroidX
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.activity.ktx)
    implementation(libs.fragment.ktx)

    // Lifecycle (ViewModel + runtime)
    implementation(libs.bundles.lifecycle)

    // Jetpack Navigation Component
    implementation(libs.bundles.navigation)

    // Room — runtime + KTX; compiler via KSP
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    // CameraX — camera2, lifecycle, video capture, view
    implementation(libs.bundles.camerax)

    // Hilt — runtime; compiler via KSP
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Kotlin Coroutines
    implementation(libs.bundles.coroutines)

    // ML Kit Face & Pose Detection
    implementation(libs.mlkit.face.detection)
    implementation(libs.mlkit.pose.detection)

    // MPAndroidChart for visualization
    implementation(libs.mpandroidchart)

    // Firebase Authentication & Google Sign-In
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.play.services.auth)

    // ──────────────────────────────────────────────────────────────────────────
    // Unit test dependencies  (src/test/)
    // ──────────────────────────────────────────────────────────────────────────

    // Legacy JUnit 4 (kept for any non-Kotest tests)
    testImplementation(libs.junit)

    // Kotest — runner + property testing (JUnit 5 platform)
    testImplementation(libs.bundles.kotest)

    // MockK — mocking for Kotlin
    testImplementation(libs.mockk)

    // Coroutines test utilities
    testImplementation(libs.coroutines.test)

    // Room in-memory database for unit tests
    testImplementation(libs.room.testing)

    // ──────────────────────────────────────────────────────────────────────────
    // Instrumented test dependencies  (src/androidTest/)
    // ──────────────────────────────────────────────────────────────────────────

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Hilt testing support
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler.test)

    // Fragment scenario for isolated fragment testing
    debugImplementation(libs.fragment.testing)
}
