package com.codebuff.intellij.backend

import com.intellij.openapi.project.Project
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.BufferedReader
import java.io.PrintWriter

/**
 * CLI backend client that communicates with codebuff via JSON Lines protocol.
 * Manages process lifecycle, reconnection, and streaming event handling.
 *
 * Issue: cb-ble.1
 */
class CliBackendClient(
    private val project: Project,
    private val cliPath: String = "codebuff"
) : BackendClient, Disposable {
    
    private var process: Process? = null
    private var reader: BufferedReader? = null
    private var writer: PrintWriter? = null
    private var isConnectedFlag = false
    private var reconnectJob: Job? = null
    
    var maxReconnectAttempts = 5
    private var reconnectAttempts = 0
    
    override val isConnected: Boolean
        get() = isConnectedFlag && process?.isAlive == true
    
    override suspend fun connect() {
        withContext(Dispatchers.IO) {
            try {
                process = ProcessBuilder(cliPath, "ide", "--stdio")
                    .redirectErrorStream(true)
                    .start()
                
                reader = process!!.inputStream.bufferedReader()
                writer = PrintWriter(process!!.outputStream, true)
                isConnectedFlag = true
                reconnectAttempts = 0
                
                // Monitor process for crashes
                startWatchdog()
            } catch (e: Exception) {
                isConnectedFlag = false
                throw e
            }
        }
    }
    
    override suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            try {
                writer?.close()
                reader?.close()
                process?.destroy()
                process?.waitFor()
            } finally {
                isConnectedFlag = false
                reconnectJob?.cancel()
            }
        }
    }
    
    override suspend fun sendMessage(request: SendMessageRequest): Flow<BackendEvent> = flow {
        if (!isConnected) {
            throw IllegalStateException("Backend not connected")
        }
        
        withContext(Dispatchers.IO) {
            val gson = com.google.gson.Gson()
            val json = gson.toJson(request)
            writer?.println(json)
            writer?.flush()
            
            // Stream events until done
            var done = false
            while (!done && isConnected) {
                val line = reader?.readLine()
                if (line != null) {
                    val event = Protocol.parseEvent(line)
                    emit(event)
                    if (event is DoneEvent) {
                        done = true
                    }
                }
            }
        }
    }
    
    override suspend fun cancel(sessionId: String) {
        if (!isConnected) return
        
        withContext(Dispatchers.IO) {
            val gson = com.google.gson.Gson()
            val request = CancelRequest(sessionId = sessionId)
            writer?.println(gson.toJson(request))
            writer?.flush()
        }
    }
    
    fun simulateCrash() {
        process?.destroy()
        isConnectedFlag = false
    }
    
    override fun dispose() {
        runBlocking {
            disconnect()
        }
    }
    
    private fun startWatchdog() {
        reconnectJob = GlobalScope.launch(Dispatchers.IO) {
            while (isConnectedFlag) {
                delay(1000)
                if (process?.isAlive == false) {
                    isConnectedFlag = false
                    attemptReconnect()
                }
            }
        }
    }
    
    private suspend fun attemptReconnect() {
        if (reconnectAttempts >= maxReconnectAttempts) {
            return
        }
        
        reconnectAttempts++
        val backoffMs = 1000L * (1 shl (reconnectAttempts - 1))
        delay(backoffMs)
        
        try {
            connect()
        } catch (e: Exception) {
            // Will retry on next watchdog cycle
        }
    }
}

interface Disposable {
    fun dispose()
}
