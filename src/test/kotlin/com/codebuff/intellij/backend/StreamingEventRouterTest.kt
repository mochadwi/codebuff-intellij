package com.codebuff.intellij.backend

import com.google.gson.JsonObject
import com.intellij.testFramework.BasePlatformTestCase
import kotlin.test.Test
import javax.swing.SwingUtilities
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * TDD Test First: Tests for StreamingEventRouter.
 * 
 * Issue: cb-ble.9
 * Tests event routing, EDT delivery, and listener management.
 */
class StreamingEventRouterTest : BasePlatformTestCase() {
    
    private lateinit var router: StreamingEventRouter
    private lateinit var listener: TestEventListener
    
    override fun setUp() {
        super.setUp()
        router = StreamingEventRouter(project)
        listener = TestEventListener()
        router.addListener(listener)
    }
    
    @Test
    fun `routes TokenEvent to listener`() {
        val event = TokenEvent("sess-1", "text")
        
        router.routeEvent(event)
        waitForEdt()
        
        assertEquals(1, listener.tokenEvents.size)
        assertEquals("text", listener.tokenEvents[0].text)
    }
    
    @Test
    fun `routes ToolCallEvent to listener`() {
        val event = ToolCallEvent("sess-1", "read_files", JsonObject())
        
        router.routeEvent(event)
        waitForEdt()
        
        assertEquals(1, listener.toolCallEvents.size)
    }
    
    @Test
    fun `routes DiffEvent to listener`() {
        val event = DiffEvent("sess-1", listOf())
        
        router.routeEvent(event)
        waitForEdt()
        
        assertEquals(1, listener.diffEvents.size)
    }
    
    @Test
    fun `routes ErrorEvent to listener`() {
        val event = ErrorEvent("sess-1", "Error message")
        
        router.routeEvent(event)
        waitForEdt()
        
        assertEquals(1, listener.errorEvents.size)
    }
    
    @Test
    fun `routes DoneEvent to listener`() {
        val event = DoneEvent("sess-1")
        
        router.routeEvent(event)
        waitForEdt()
        
        assertEquals(1, listener.doneEvents.size)
    }
    
    @Test
    fun `events delivered on EDT`() {
        var wasOnEdt = false
        val edtListener = object : StreamingEventRouter.EventListener {
            override fun onToken(event: TokenEvent) {
                wasOnEdt = SwingUtilities.isEventDispatchThread()
            }
            override fun onToolCall(event: ToolCallEvent) {}
            override fun onToolResult(event: ToolResultEvent) {}
            override fun onDiff(event: DiffEvent) {}
            override fun onError(event: ErrorEvent) {}
            override fun onDone(event: DoneEvent) {}
        }
        router.addListener(edtListener)
        
        router.routeEvent(TokenEvent("s1", "t"))
        waitForEdt()
        
        assertTrue(wasOnEdt)
    }
    
    @Test
    fun `multiple listeners receive events`() {
        val listener2 = TestEventListener()
        router.addListener(listener2)
        
        router.routeEvent(TokenEvent("s1", "t"))
        waitForEdt()
        
        assertEquals(1, listener.tokenEvents.size)
        assertEquals(1, listener2.tokenEvents.size)
    }
    
    @Test
    fun `removed listener stops receiving events`() {
        router.removeListener(listener)
        
        router.routeEvent(TokenEvent("s1", "t"))
        waitForEdt()
        
        assertEquals(0, listener.tokenEvents.size)
    }
    
    private fun waitForEdt() {
        Thread.sleep(100)
    }
    
    private class TestEventListener : StreamingEventRouter.EventListener {
        val tokenEvents = mutableListOf<TokenEvent>()
        val toolCallEvents = mutableListOf<ToolCallEvent>()
        val toolResultEvents = mutableListOf<ToolResultEvent>()
        val diffEvents = mutableListOf<DiffEvent>()
        val errorEvents = mutableListOf<ErrorEvent>()
        val doneEvents = mutableListOf<DoneEvent>()
        
        override fun onToken(event: TokenEvent) { tokenEvents.add(event) }
        override fun onToolCall(event: ToolCallEvent) { toolCallEvents.add(event) }
        override fun onToolResult(event: ToolResultEvent) { toolResultEvents.add(event) }
        override fun onDiff(event: DiffEvent) { diffEvents.add(event) }
        override fun onError(event: ErrorEvent) { errorEvents.add(event) }
        override fun onDone(event: DoneEvent) { doneEvents.add(event) }
    }
}

// Router stub for testing
class StreamingEventRouter(private val project: com.intellij.openapi.project.Project) {
    private val listeners = mutableListOf<EventListener>()
    
    interface EventListener {
        fun onToken(event: TokenEvent)
        fun onToolCall(event: ToolCallEvent)
        fun onToolResult(event: ToolResultEvent)
        fun onDiff(event: DiffEvent)
        fun onError(event: ErrorEvent)
        fun onDone(event: DoneEvent)
    }
    
    fun addListener(listener: EventListener) {
        listeners.add(listener)
    }
    
    fun removeListener(listener: EventListener) {
        listeners.remove(listener)
    }
    
    fun routeEvent(event: BackendEvent) {
        com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater {
            when (event) {
                is TokenEvent -> listeners.forEach { it.onToken(event) }
                is ToolCallEvent -> listeners.forEach { it.onToolCall(event) }
                is ToolResultEvent -> listeners.forEach { it.onToolResult(event) }
                is DiffEvent -> listeners.forEach { it.onDiff(event) }
                is ErrorEvent -> listeners.forEach { it.onError(event) }
                is DoneEvent -> listeners.forEach { it.onDone(event) }
            }
        }
    }
}
