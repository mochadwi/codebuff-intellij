package com.codebuff.intellij

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class CodebuffToolWindowFactoryTest {

    @Test
    fun `tool window extension is declared in plugin xml`() {
        val content = loadPluginXml()

        assertTrue(content.contains("<toolWindow"), "plugin.xml should declare a <toolWindow> extension")
        assertTrue(content.contains("id=\"Codebuff\""), "tool window id should be 'Codebuff'")
    }

    @Test
    fun `tool window uses CodebuffToolWindowFactory`() {
        val content = loadPluginXml()

        assertTrue(
            content.contains("factoryClass=\"com.codebuff.intellij.ui.CodebuffToolWindowFactory\""),
            "tool window should be wired to CodebuffToolWindowFactory"
        )
    }

    @Test
    fun `tool window is anchored to the right`() {
        val content = loadPluginXml()

        assertTrue(
            content.contains("anchor=\"right\""),
            "tool window should be anchored to the right side of the IDE"
        )
    }

    private fun loadPluginXml(): String {
        return File("src/main/resources/META-INF/plugin.xml").readText()
    }
}
