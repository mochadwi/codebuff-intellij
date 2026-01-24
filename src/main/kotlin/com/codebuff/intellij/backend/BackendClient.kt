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
