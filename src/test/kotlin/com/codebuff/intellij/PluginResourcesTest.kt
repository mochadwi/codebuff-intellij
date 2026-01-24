package com.codebuff.intellij

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class PluginResourcesTest {

    @Test
    fun `codebuff svg icon exists`() {
        val iconFile = File("src/main/resources/icons/codebuff.svg")
        assertTrue(iconFile.exists(), "codebuff.svg icon should exist")
    }

    @Test
    fun `codebuff dark svg icon exists`() {
        val iconFile = File("src/main/resources/icons/codebuff_dark.svg")
        assertTrue(iconFile.exists(), "codebuff_dark.svg icon should exist")
    }

    @Test
    fun `message bundle properties file exists`() {
        val bundleFile = File("src/main/resources/messages/CodebuffBundle.properties")
        assertTrue(bundleFile.exists(), "CodebuffBundle.properties should exist")
    }

    @Test
    fun `message bundle contains toolwindow title`() {
        val bundleFile = File("src/main/resources/messages/CodebuffBundle.properties")
        assertTrue(bundleFile.exists(), "CodebuffBundle.properties should exist")
        
        val content = bundleFile.readText()
        assertTrue(
            content.contains("toolwindow.title"),
            "Bundle should contain toolwindow.title key"
        )
    }

    @Test
    fun `CodebuffBundle class exists`() {
        val bundleClass = File("src/main/kotlin/com/codebuff/intellij/CodebuffBundle.kt")
        assertTrue(bundleClass.exists(), "CodebuffBundle.kt should exist")
    }

    @Test
    fun `CodebuffBundle has message function`() {
        val bundleClass = File("src/main/kotlin/com/codebuff/intellij/CodebuffBundle.kt")
        assertTrue(bundleClass.exists(), "CodebuffBundle.kt should exist")
        
        val content = bundleClass.readText()
        assertTrue(
            content.contains("fun message"),
            "CodebuffBundle should have message function"
        )
    }

    @Test
    fun `plugin xml references icon path`() {
        val content = TestUtils.loadPluginXml()
        assertTrue(
            content.contains("/icons/codebuff.svg"),
            "plugin.xml should reference codebuff.svg icon"
        )
    }
}
