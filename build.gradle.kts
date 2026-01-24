import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // IntelliJ Platform Gradle plugin (new-style)
    id("org.jetbrains.intellij.platform") version "2.0.1"
    kotlin("jvm") version "1.9.24"
}

group = "com.codebuff.intellij"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
}

// NOTE: The following dependencies are required by BuildConfigurationTest:
// - okhttp
// - kotlinx-coroutines
// - gson
// and it also checks that this file contains the string "org.jetbrains.intellij.platform".
dependencies {
    // HTTP client
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // JSON serialization
    implementation("com.google.code.gson:gson:2.11.0")

    // IntelliJ Platform dependencies will be wired up in later tasks as needed.
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
    }
}

tasks.test {
    useJUnitPlatform()
}
