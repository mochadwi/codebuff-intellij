import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // IntelliJ Platform Gradle plugin (new-style)
    id("org.jetbrains.intellij.platform") version "2.0.1"
    kotlin("jvm") version "1.9.24"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

group = "com.codebuff.intellij"
version = "0.1.0-SNAPSHOT"

repositories {
    // Standard dependencies
    mavenCentral()

    // Required for IntelliJ Platform artifacts (fixes 'No IntelliJ Platform dependency found')
    intellijPlatform.defaultRepositories()
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
    // IntelliJ Platform SDK (needed for plugin build & tests like BasePlatformTestCase)
    intellijPlatform {
        intellijIdeaCommunity("2024.2")
        // Tools for bytecode instrumentation and Java compiler used by IntelliJ plugin tests
        instrumentationTools()
        // Test framework for BasePlatformTestCase and other IntelliJ platform testing utilities
        testFramework(TestFrameworkType.Platform)

        // Plugin Verifier for verifyPlugin task
        pluginVerifier()
    }

    // JUnit 5 for unit tests (includes API, params, engine)
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")

    // JUnit 4 required by IntelliJ Platform test runner (JUnit5TestSessionListener needs junit.framework.TestCase)
    testRuntimeOnly("junit:junit:4.13.2")

    // Kotlin test assertions and JUnit 4/5 integration
    testImplementation(kotlin("test"))

    // MockK for mocking
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("io.mockk:mockk-jvm:1.13.8")

    // HTTP client
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

    // JSON serialization
    implementation("com.google.code.gson:gson:2.11.0")
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
    }
}

tasks.test {
    // Tests (including PluginDescriptorTest and CodebuffToolWindowFactoryTest)
    // are executed via Docker: `docker compose run --rm gradle test ...`
    useJUnitPlatform()
}

// Configure Plugin Verification
intellijPlatform {
    pluginVerification {
        ides {
            recommended()
        }
    }
}
