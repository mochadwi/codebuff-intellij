package com.codebuff.intellij

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class ServiceLayerTest {

    @Test
    fun `plugin xml declares CodebuffProjectService`() {
        val content = loadPluginXml()
        assertTrue(
            content.contains("CodebuffProjectService"),
            "plugin.xml should declare CodebuffProjectService"
        )
        assertTrue(
            content.contains("projectService"),
            "plugin.xml should use projectService extension"
        )
    }

    @Test
    fun `plugin xml declares SessionManager service`() {
        val content = loadPluginXml()
        assertTrue(
            content.contains("SessionManager"),
            "plugin.xml should declare SessionManager"
        )
    }

    @Test
    fun `CodebuffProjectService class exists`() {
        val serviceFile = File("src/main/kotlin/com/codebuff/intellij/services/CodebuffProjectService.kt")
        assertTrue(serviceFile.exists(), "CodebuffProjectService.kt should exist")
    }

    @Test
    fun `SessionManager class exists`() {
        val serviceFile = File("src/main/kotlin/com/codebuff/intellij/services/SessionManager.kt")
        assertTrue(serviceFile.exists(), "SessionManager.kt should exist")
    }

    @Test
    fun `BackendClient interface exists`() {
        val interfaceFile = File("src/main/kotlin/com/codebuff/intellij/backend/BackendClient.kt")
        assertTrue(interfaceFile.exists(), "BackendClient.kt interface should exist")
    }

    @Test
    fun `BackendClient interface defines required methods`() {
        val interfaceFile = File("src/main/kotlin/com/codebuff/intellij/backend/BackendClient.kt")
        assertTrue(interfaceFile.exists(), "BackendClient.kt should exist")
        
        val content = interfaceFile.readText()
        assertTrue(content.contains("fun connect"), "BackendClient should have connect method")
        assertTrue(content.contains("fun disconnect"), "BackendClient should have disconnect method")
        assertTrue(content.contains("fun sendMessage"), "BackendClient should have sendMessage method")
        assertTrue(content.contains("fun cancel"), "BackendClient should have cancel method")
    }

    private fun loadPluginXml(): String {
        return File("src/main/resources/META-INF/plugin.xml").readText()
    }
}
