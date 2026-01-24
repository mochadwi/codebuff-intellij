package com.codebuff.intellij.ui

import com.google.gson.JsonObject
import com.intellij.testFramework.BasePlatformTestCase
import com.codebuff.intellij.backend.*
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * TDD Test First: Tests for ChatPanel receiving streaming events.
 * 
 * Issue: cb-ble.10
 * Tests token display, loading states, error handling, and input state.
 */
class ChatPanelStreamingTest : BasePlatformTestCase() {
    
    private lateinit var chatPanel: ChatPanel
    private lateinit var router: StreamingEventRouter
    
    override fun setUp() {
        super.setUp()
        router = StreamingEventRouter(project)
        chatPanel = ChatPanel(project)
    }
    
    @Test
    fun `displays streaming tokens`() {
        router.routeEvent(TokenEvent("s1", "Hello "))
        router.routeEvent(TokenEvent("s1", "World"))
        waitForEdt()
        
        val displayText = chatPanel.getDisplayText()
        assertTrue(displayText.contains("Hello") && displayText.contains("World"))
    }
    
    @Test
    fun `shows loading indicator during streaming`() {
        router.routeEvent(TokenEvent("s1", "Starting..."))
        waitForEdt()
        
        assertTrue(chatPanel.isLoading, "Should show loading during streaming")
    }
    
    @Test
    fun `hides loading indicator on done`() {
        router.routeEvent(TokenEvent("s1", "Done"))
        router.routeEvent(DoneEvent("s1"))
        waitForEdt()
        
        assertFalse(chatPanel.isLoading, "Should hide loading after done")
    }
    
    @Test
    fun `displays error messages`() {
        router.routeEvent(ErrorEvent("s1", "Something went wrong"))
        waitForEdt()
        
        val displayText = chatPanel.getDisplayText()
        assertTrue(displayText.contains("Something went wrong"))
    }
    
    @Test
    fun `input disabled during streaming`() {
        router.routeEvent(TokenEvent("s1", "Working..."))
        waitForEdt()
        
        assertFalse(chatPanel.isInputEnabled, "Input should be disabled during streaming")
    }
    
    @Test
    fun `input enabled after done`() {
        router.routeEvent(DoneEvent("s1"))
        waitForEdt()
        
        assertTrue(chatPanel.isInputEnabled, "Input should be enabled after done")
    }
    
    @Test
    fun `multiple token events accumulate`() {
        val tokens = listOf("Hello", " ", "from", " ", "Codebuff")
        tokens.forEach { router.routeEvent(TokenEvent("s1", it)) }
        waitForEdt()
        
        val text = chatPanel.getDisplayText()
        kotlin.test.assertEquals("Hello from Codebuff", text.trim())
    }
    
    @Test
    fun `tool call event updates display`() {
        val input = JsonObject()
        input.addProperty("path", "test.kt")
        router.routeEvent(ToolCallEvent("s1", "read_files", input))
        waitForEdt()
        
        // Should show tool call in UI
        assertTrue(chatPanel.isLoading)
    }
    
    private fun waitForEdt() {
        Thread.sleep(100)
    }
}

// ChatPanel stub for testing
class ChatPanel(private val project: com.intellij.openapi.project.Project) : javax.swing.JPanel() {
    private val messages = StringBuilder()
    var isLoading = false
    var isInputEnabled = true
    
    fun getDisplayText(): String = messages.toString()
    
    fun onToken(event: TokenEvent) {
        messages.append(event.text)
        isLoading = true
        isInputEnabled = false
    }
    
    fun onDone(event: DoneEvent) {
        isLoading = false
        isInputEnabled = true
    }
    
    fun onError(event: ErrorEvent) {
        messages.append("\nError: ${event.message}")
    }
    
    fun onToolCall(event: ToolCallEvent) {
        isLoading = true
        isInputEnabled = false
    }
}
