package com.codebuff.intellij.backend

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow

// Backend Client Interface
interface BackendClient {
    suspend fun connect()
    suspend fun disconnect()
    suspend fun sendMessage(request: SendMessageRequest): Flow<BackendEvent>
    suspend fun cancel(sessionId: String)
    val isConnected: Boolean
}

/**
 * JSON Lines protocol for Codebuff backend communication.
 * Handles serialization of requests and deserialization of events.
 *
 * Issue: cb-ble.2
 */

// Request Classes
data class SendMessageRequest(
    val id: String,
    val type: String = "sendMessage",
    val sessionId: String,
    val text: String,
    val context: List<Map<String, Any>> = emptyList()
)

data class CancelRequest(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: String = "cancel",
    val sessionId: String
)

// Event Base Class
sealed class BackendEvent {
    abstract val sessionId: String
}

// Event Types
data class TokenEvent(
    override val sessionId: String,
    val text: String
) : BackendEvent()

data class ToolCallEvent(
    override val sessionId: String,
    val tool: String,
    val input: JsonObject
) : BackendEvent()

data class ToolResultEvent(
    override val sessionId: String,
    val tool: String,
    val output: JsonObject
) : BackendEvent()

data class DiffEvent(
    override val sessionId: String,
    val files: List<Map<String, Any>>
) : BackendEvent()

data class ErrorEvent(
    override val sessionId: String,
    val message: String
) : BackendEvent()

data class DoneEvent(
    override val sessionId: String
) : BackendEvent()

// Protocol Parser
object Protocol {
    fun parseEvent(json: String): BackendEvent {
        val gson = Gson()
        val obj = gson.fromJson(json, JsonObject::class.java)
        val type = obj.get("type").asString
        val sessionId = obj.get("sessionId").asString
        
        return when (type) {
            "token" -> TokenEvent(
                sessionId = sessionId,
                text = obj.get("text").asString
            )
            "tool_call" -> ToolCallEvent(
                sessionId = sessionId,
                tool = obj.get("tool").asString,
                input = obj.get("input").asJsonObject
            )
            "tool_result" -> ToolResultEvent(
                sessionId = sessionId,
                tool = obj.get("tool").asString,
                output = obj.get("output").asJsonObject
            )
            "diff" -> {
                val files = mutableListOf<Map<String, Any>>()
                if (obj.has("files")) {
                    obj.get("files").asJsonArray.forEach { fileEl ->
                        val fileObj = fileEl.asJsonObject
                        files.add(mapOf(
                            "path" to fileObj.get("path").asString,
                            "before" to fileObj.get("before").asString,
                            "after" to fileObj.get("after").asString
                        ))
                    }
                }
                DiffEvent(
                    sessionId = sessionId,
                    files = files
                )
            }
            "error" -> ErrorEvent(
                sessionId = sessionId,
                message = obj.get("message").asString
            )
            "done" -> DoneEvent(sessionId = sessionId)
            else -> DoneEvent(sessionId = sessionId) // Unknown type handled gracefully
        }
    }
}
