package com.codebuff.intellij

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class PluginDescriptorTest : BasePlatformTestCase() {

    @Test
    fun `plugin xml exists in correct location`() {
        val pluginXml = File("src/main/resources/META-INF/plugin.xml")
        assertTrue(pluginXml.exists())
    }

    @Test
    fun `plugin id is correctly configured`() {
        val content = loadPluginXml()
        assertTrue(content.contains("<id>com.codebuff.intellij</id>"))
    }

    @Test
    fun `toolwindow extension is registered`() {
        val content = loadPluginXml()
        assertTrue(content.contains("toolWindow"))
        assertTrue(content.contains("Codebuff"))
        assertTrue(content.contains("CodebuffToolWindowFactory"))
    }

    @Test
    fun `project services are registered`() {
        val content = loadPluginXml()
        assertTrue(content.contains("projectService"))
        assertTrue(content.contains("CodebuffProjectService"))
    }

    @Test
    fun `actions are registered with shortcuts`() {
        val content = loadPluginXml()
        assertTrue(content.contains("<action"))
        assertTrue(content.contains("keyboard-shortcut"))
    }

    private fun loadPluginXml(): String {
        return File("src/main/resources/META-INF/plugin.xml").readText()
    }
}
