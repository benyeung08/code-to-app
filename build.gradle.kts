// Root build.gradle.kts - central plugin version definition for Gradle 9.4.1 + Kotlin 2.3.2
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("com.android.library") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.3.2" apply false
    id("org.jetbrains.kotlin.plugin.parcelize") version "2.3.2" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.2" apply false
    id("com.google.devtools.ksp") version "2.3.2-1.0.28" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
