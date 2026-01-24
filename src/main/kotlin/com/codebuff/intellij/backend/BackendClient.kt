package com.codebuff.intellij.backend

import kotlinx.coroutines.flow.Flow

/**
 * Backend client interface for communicating with Codebuff CLI backend.
 * Handles connection lifecycle and message sending via JSON Lines protocol.
 *
 * Issue: cb-ble.1
 */
interface BackendClient {
    suspend fun connect()
    suspend fun disconnect()
    suspend fun sendMessage(request: SendMessageRequest): Flow<BackendEvent>
    suspend fun cancel(sessionId: String)
    val isConnected: Boolean
}

data class SendMessageRequest(
    val sessionId: String,
    val text: String,
    val context: List<ContextItem> = emptyList(),
)

sealed class BackendEvent {
    data class Token(val sessionId: String, val text: String) : BackendEvent()
    data class ToolCall(val sessionId: String, val tool: String, val input: Map<String, Any>) : BackendEvent()
    data class ToolResult(val sessionId: String, val tool: String, val output: Map<String, Any>) : BackendEvent()
    data class Diff(val sessionId: String, val files: List<FileDiff>) : BackendEvent()
    data class Error(val sessionId: String, val message: String) : BackendEvent()
    data class Done(val sessionId: String) : BackendEvent()
}

data class FileDiff(
    val path: String,
    val before: String,
    val after: String,
)

data class ContextItem(
    val type: String,
    val path: String,
    val content: String,
)
