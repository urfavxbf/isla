/*
 * Smart Island (2026)
 * © Animesh Gupta — github.com/agupta07505
 * Licensed under the GNU GPL v3 License
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Isla"

// Main Android project is the fully redesigned Isla app.
// The old v1.2.1 project is preserved in archive/isla-v1.2.1.
include(":app")
project(":app").projectDir = file("isla-lab/app")
include(":baselineprofile")
project(":baselineprofile").projectDir = file("isla-lab/baselineprofile")
