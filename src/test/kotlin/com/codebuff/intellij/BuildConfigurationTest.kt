package com.codebuff.intellij

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class BuildConfigurationTest {
    @Test
    fun `build gradle kts exists and is valid`() {
        val buildFile = File("build.gradle.kts")
        assertTrue(buildFile.exists())
        assertTrue(buildFile.readText().contains("org.jetbrains.intellij.platform"))
    }

    @Test
    fun `settings gradle kts configures project name`() {
        val settingsFile = File("settings.gradle.kts")
        assertTrue(settingsFile.exists())
        assertTrue(settingsFile.readText().contains("codebuff-intellij"))
    }

    @Test
    fun `gradle properties has correct JVM target`() {
        val propsFile = File("gradle.properties")
        assertTrue(propsFile.exists())
        val content = propsFile.readText()
        assertTrue(
            content.contains("kotlin.jvmTarget=17") ||
                content.contains("jvmToolchain(17)"),
        )
    }

    @Test
    fun `required dependencies are configured`() {
        val buildFile = File("build.gradle.kts").readText()
        assertTrue(buildFile.contains("okhttp"))
        assertTrue(buildFile.contains("kotlinx-coroutines"))
        assertTrue(buildFile.contains("gson"))
    }
}
