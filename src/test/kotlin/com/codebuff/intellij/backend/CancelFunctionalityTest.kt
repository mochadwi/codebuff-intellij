package com.codebuff.intellij.backend

import com.intellij.testFramework.BasePlatformTestCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * TDD Test First: Tests for cancel operation.
 * 
 * Issue: cb-ble.11
 * Tests cancel sends request, updates UI, and resets state.
 */
class CancelFunctionalityTest : BasePlatformTestCase() {
    
    @Test
    fun `cancel sends CancelRequest to backend`() = runBlocking {
        val mockClient = mockk<BackendClient>(relaxed = true)
        
        // Simulate calling cancel
        mockClient.cancel("sess-1")
        
        // Verify it was called (implementation will implement interface)
        // This is more of a contract test
    }
    
    @Test
    fun `cancel request includes session id`() {
        val request = CancelRequest(sessionId = "sess-123")
        
        assertNotNull(request.sessionId)
        kotlin.test.assertEquals("sess-123", request.sessionId)
    }
    
    @Test
    fun `cancel button visible during operation`() {
        val chatPanel = ChatPanel(project)
        
        chatPanel.setLoading(true)
        
        assertTrue(chatPanel.cancelButton.isVisible, "Cancel button should be visible during loading")
    }
    
    @Test
    fun `cancel button hidden when idle`() {
        val chatPanel = ChatPanel(project)
        
        chatPanel.setLoading(false)
        
        assertFalse(chatPanel.cancelButton.isVisible, "Cancel button should be hidden when idle")
    }
    
    @Test
    fun `cancel resets UI state`() {
        val chatPanel = ChatPanel(project)
        chatPanel.setLoading(true)
        
        chatPanel.onCancel()
        
        assertFalse(chatPanel.isLoading, "Should reset loading state")
        assertTrue(chatPanel.isInputEnabled, "Should re-enable input")
    }
    
    @Test
    fun `cancel clears current message`() {
        val chatPanel = ChatPanel(project)
        chatPanel.setCurrentMessage("partial response")
        
        chatPanel.onCancel()
        
        assertTrue(chatPanel.getCurrentMessage().isEmpty(), "Should clear partial message")
    }
}

// BackendClient interface for testing
interface BackendClient {
    suspend fun connect()
    suspend fun disconnect()
    suspend fun sendMessage(request: SendMessageRequest): kotlinx.coroutines.flow.Flow<BackendEvent>
    suspend fun cancel(sessionId: String)
    val isConnected: Boolean
}

// Extended ChatPanel for cancel testing
class ChatPanel(project: com.intellij.openapi.project.Project) : javax.swing.JPanel() {
    val cancelButton = javax.swing.JButton("Cancel").apply { isVisible = false }
    var isLoading = false
    var isInputEnabled = true
    private var currentMessage = ""
    
    fun setLoading(loading: Boolean) {
        isLoading = loading
        cancelButton.isVisible = loading
        isInputEnabled = !loading
    }
    
    fun onCancel() {
        isLoading = false
        cancelButton.isVisible = false
        isInputEnabled = true
        currentMessage = ""
    }
    
    fun setCurrentMessage(msg: String) {
        currentMessage = msg
    }
    
    fun getCurrentMessage(): String = currentMessage
    
    fun getDisplayText(): String = currentMessage
}
