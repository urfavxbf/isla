plugins {
    id("com.android.application")
    // AGP 9 already compiles Kotlin. Only Compose needs its compiler plugin.
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.pao.isla"
    // Android SDK 36 ships with the current Code on the Go toolchain.
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pao.isla"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "1.2.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

// AndroidX Activity/Compose may pull in ProfileInstaller only for baseline-profile
// startup optimization. Isla does not ship a baseline profile; that optional AAR
// is absent from some Code on the Go offline Maven bundles.
configurations.configureEach {
    exclude(group = "androidx.profileinstaller", module = "profileinstaller")
}

dependencies {
    // Prefer the AndroidX versions shipped in Code on the Go's harvested Maven repository.
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
}
