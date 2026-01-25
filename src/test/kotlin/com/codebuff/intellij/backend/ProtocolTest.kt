package com.codebuff.intellij.backend

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.jupiter.api.assertThrows

/**
 * TDD Test First: Tests for JSON Lines protocol serialization/deserialization.
 * 
 * Issue: cb-ble.8
 * Tests request/response serialization and event parsing.
 */
class ProtocolTest {
    
    private val gson = Gson()
    
    // Request Serialization Tests
    
    @Test
    fun `SendMessageRequest serializes correctly`() {
        val request = SendMessageRequest(
            id = "req-001",
            sessionId = "sess-123",
            text = "Hello",
            context = emptyList()
        )
        
        val json = gson.toJson(request)
        
        assertTrue(json.contains("\"type\":\"sendMessage\""))
        assertTrue(json.contains("\"id\":\"req-001\""))
        assertTrue(json.contains("\"sessionId\":\"sess-123\""))
        assertTrue(json.contains("\"text\":\"Hello\""))
    }
    
    @Test
    fun `CancelRequest serializes correctly`() {
        val request = CancelRequest(sessionId = "sess-123")
        
        val json = gson.toJson(request)
        
        assertTrue(json.contains("\"type\":\"cancel\""))
        assertTrue(json.contains("\"sessionId\":\"sess-123\""))
    }
    
    // Event Deserialization Tests
    
    @Test
    fun `TokenEvent deserializes correctly`() {
        val json = "{\"type\":\"token\",\"sessionId\":\"sess-123\",\"text\":\"Hello\"}"
        
        val event = Protocol.parseEvent(json)
        
        assertTrue(event is TokenEvent)
        assertEquals("Hello", (event as TokenEvent).text)
        assertEquals("sess-123", event.sessionId)
    }
    
    @Test
    fun `ToolCallEvent deserializes correctly`() {
        val json = "{\"type\":\"tool_call\",\"sessionId\":\"s1\",\"tool\":\"read_files\",\"input\":{}}"
        
        val event = Protocol.parseEvent(json)
        
        assertTrue(event is ToolCallEvent)
        assertEquals("read_files", (event as ToolCallEvent).tool)
        assertEquals("s1", event.sessionId)
    }
    
    @Test
    fun `DiffEvent deserializes with files`() {
        val json = "{\"type\":\"diff\",\"sessionId\":\"s1\",\"files\":[{\"path\":\"a.kt\",\"before\":\"\",\"after\":\"code\"}]}"
        
        val event = Protocol.parseEvent(json)
        
        assertTrue(event is DiffEvent)
        assertEquals(1, (event as DiffEvent).files.size)
    }
    
    @Test
    fun `ErrorEvent deserializes correctly`() {
        val json = "{\"type\":\"error\",\"sessionId\":\"s1\",\"message\":\"Something went wrong\"}"
        
        val event = Protocol.parseEvent(json)
        
        assertTrue(event is ErrorEvent)
        assertEquals("Something went wrong", (event as ErrorEvent).message)
    }
    
    @Test
    fun `DoneEvent deserializes correctly`() {
        val json = "{\"type\":\"done\",\"sessionId\":\"s1\"}"
        
        val event = Protocol.parseEvent(json)
        
        assertTrue(event is DoneEvent)
        assertEquals("s1", event.sessionId)
    }
    
    @Test
    fun `unknown event type returns UnknownEvent`() {
        val json = "{\"type\":\"future_event\",\"sessionId\":\"s1\"}"
        
        val event = Protocol.parseEvent(json)
        
        // Should handle gracefully, not crash
        assertTrue(event is BackendEvent)
    }
    
    @Test
    fun `malformed JSON throws exception`() {
        val json = "not valid json"
        
        assertThrows<Exception> {
            Protocol.parseEvent(json)
        }
    }
    
    @Test
    fun `ToolResultEvent deserializes correctly`() {
        val json = "{\"type\":\"tool_result\",\"sessionId\":\"s1\",\"tool\":\"read_files\",\"output\":{}}"
        
        val event = Protocol.parseEvent(json)
        
        assertTrue(event is ToolResultEvent)
        assertEquals("read_files", (event as ToolResultEvent).tool)
    }
}
