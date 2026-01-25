package com.codebuff.intellij.backend

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project

/**
 * Routes backend events to UI listeners, marshaling all callbacks to EDT.
 * Ensures thread safety for IntelliJ UI updates.
 *
 * Issue: cb-ble.3
 */
class StreamingEventRouter(private val project: Project) {
    private val log = Logger.getInstance(javaClass)
    private val listeners = mutableListOf<EventListener>()

    interface EventListener {
        fun onToken(event: TokenEvent)

        fun onToolCall(event: ToolCallEvent)

        fun onToolResult(event: ToolResultEvent)

        fun onDiff(event: DiffEvent)

        fun onError(event: ErrorEvent)

        fun onDone(event: DoneEvent)

        fun onUnknown(event: UnknownEvent) {}
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
        // Check disposal before dispatching
        if (project.isDisposed) {
            return
        }

        // Snapshot listeners to avoid holding lock during callbacks
        val snapshot = synchronized(listeners) { listeners.toList() }

        ApplicationManager.getApplication().invokeLater({
            // Check disposal again inside the lambda
            if (project.isDisposed) {
                return@invokeLater
            }

            when (event) {
                is TokenEvent -> snapshot.forEach { it.onToken(event) }
                is ToolCallEvent -> snapshot.forEach { it.onToolCall(event) }
                is ToolResultEvent -> snapshot.forEach { it.onToolResult(event) }
                is DiffEvent -> snapshot.forEach { it.onDiff(event) }
                is ErrorEvent -> snapshot.forEach { it.onError(event) }
                is DoneEvent -> snapshot.forEach { it.onDone(event) }
                is UnknownEvent -> snapshot.forEach { it.onUnknown(event) }
            }
        }, ModalityState.defaultModalityState())
    }
}
