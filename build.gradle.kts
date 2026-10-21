/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // CoGo 26.38+ bundles AGP 9.3.1 with Gradle 9.6.1 in its local Maven repository.
        // Using an older AGP can pull Guava/Gradle dependencies missing on device.
        classpath("com.android.tools.build:gradle:9.3.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.21")
        classpath("org.jetbrains.kotlin:compose-compiler-gradle-plugin:2.3.21")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.60.1")
        classpath("androidx.baselineprofile:androidx.baselineprofile.gradle.plugin:1.4.1")
    }
}
