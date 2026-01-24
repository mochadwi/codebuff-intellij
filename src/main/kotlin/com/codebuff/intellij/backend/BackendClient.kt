package com.codebuff.intellij.backend

import kotlinx.coroutines.flow.Flow

interface BackendClient {

    suspend fun connect()

    suspend fun disconnect()

    suspend fun sendMessage(sessionId: String, text: String, context: List<ContextItem> = emptyList()): Flow<BackendEvent>

    suspend fun cancel(sessionId: String)
}

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
    val after: String
)

data class ContextItem(
    val type: String,
    val path: String,
    val content: String
)
