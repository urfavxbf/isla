// Match the Android toolchain bundled with Code on the Go (Gradle 9.6.1).
// AGP 9 has built-in Kotlin; do NOT apply org.jetbrains.kotlin.android.
buildscript {
    dependencies {
        // Code on the Go's injected LogSender plugin also needs AGP on the root classpath.
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.21")
    }
}

plugins {
    id("com.android.application") version "9.3.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.21" apply false
}
