package com.codebuff.intellij.backend

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project

/**
 * Routes backend events to UI listeners, marshaling all callbacks to EDT.
 * Ensures thread safety for IntelliJ UI updates.
 *
 * Issue: cb-ble.3
 */
class StreamingEventRouter(private val project: Project) {
    
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
        synchronized(listeners) {
            listeners.add(listener)
        }
    }
    
    fun removeListener(listener: EventListener) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }
    
    fun routeEvent(event: BackendEvent) {
        ApplicationManager.getApplication().invokeLater {
            synchronized(listeners) {
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
}
