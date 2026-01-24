package com.codebuff.intellij

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class PluginDescriptorTest {

    @Test
    fun `plugin xml exists in correct location`() {
        val pluginXml = File("src/main/resources/META-INF/plugin.xml")
        assertTrue(pluginXml.exists(), "plugin.xml should exist")
    }

    @Test
    fun `plugin id is correctly configured`() {
        val content = TestUtils.loadPluginXml()
        assertTrue(content.contains("<id>com.codebuff.intellij</id>"), "plugin.xml should have correct id")
    }

    @Test
    fun `toolwindow extension is registered`() {
        val content = TestUtils.loadPluginXml()
        assertTrue(content.contains("toolWindow"), "plugin.xml should register toolWindow")
        assertTrue(content.contains("Codebuff"), "toolWindow should be named Codebuff")
        assertTrue(content.contains("CodebuffToolWindowFactory"), "toolWindow should use CodebuffToolWindowFactory")
    }

    @Test
    fun `project services are registered`() {
        val content = TestUtils.loadPluginXml()
        assertTrue(content.contains("projectService"), "plugin.xml should register projectService")
        assertTrue(content.contains("CodebuffProjectService"), "plugin.xml should register CodebuffProjectService")
    }

    @Test
    fun `actions are registered with shortcuts`() {
        val content = TestUtils.loadPluginXml()
        assertTrue(content.contains("<action"), "plugin.xml should register actions")
        assertTrue(content.contains("keyboard-shortcut"), "actions should have keyboard shortcuts")
    }
}
