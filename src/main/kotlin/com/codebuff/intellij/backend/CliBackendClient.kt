package com.codebuff.intellij.backend

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import kotlin.coroutines.cancellation.CancellationException

/**
 * CLI backend client that communicates with codebuff via JSON Lines protocol.
 * Manages process lifecycle, reconnection, and streaming event handling.
 *
 * Issue: cb-ble.1
 */
class CliBackendClient(
    private val project: Project,
    private val cliPath: String = "codebuff",
) : BackendClient, Disposable {
    private val log = Logger.getInstance(javaClass)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var process: Process? = null

    @Volatile
    private var reader: BufferedReader? = null

    @Volatile
    private var writer: BufferedWriter? = null

    @Volatile
    private var isConnectedFlag = false

    @Volatile
    private var reconnectAttempts = 0

    var maxReconnectAttempts = 5

    override val isConnected: Boolean
        get() = isConnectedFlag && process?.isAlive == true

    override suspend fun connect() {
        withContext(Dispatchers.IO) {
            try {
                process =
                    ProcessBuilder(cliPath, "ide", "--stdio")
                        .start()

                val proc = process ?: throw IllegalStateException("Process creation failed")
                reader = proc.inputStream.bufferedReader()
                writer = BufferedWriter(OutputStreamWriter(proc.outputStream, StandardCharsets.UTF_8))
                isConnectedFlag = true
                reconnectAttempts = 0

                // Consume stderr on separate coroutine
                scope.launch {
                    try {
                        proc.errorStream.bufferedReader().useLines { lines ->
                            lines.forEach { line -> log.debug("[codebuff stderr] $line") }
                        }
                    } catch (e: Exception) {
                        log.debug("Error reading stderr: ${e.message}")
                    }
                }

                // Monitor process for crashes
                startWatchdog()
            } catch (e: Exception) {
                isConnectedFlag = false
                log.error("Failed to connect to Codebuff backend", e)
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
            } catch (e: Exception) {
                log.warn("Error during disconnect", e)
            } finally {
                isConnectedFlag = false
            }
        }
    }

    override suspend fun sendMessage(request: SendMessageRequest): Flow<BackendEvent> =
        flow {
            if (!isConnected) {
                throw IllegalStateException("Backend not connected")
            }

            withContext(Dispatchers.IO) {
                val gson = com.google.gson.Gson()
                val json = gson.toJson(request)
                val w = writer ?: throw IllegalStateException("Writer not initialized")
                try {
                    w.write(json)
                    w.write("\n")
                    w.flush()
                } catch (e: Exception) {
                    log.error("Failed to send message", e)
                    throw e
                }

                // Stream events until done
                var done = false
                val r = reader ?: throw IllegalStateException("Reader not initialized")
                while (!done && isConnected) {
                    val line =
                        try {
                            r.readLine()
                        } catch (e: Exception) {
                            log.error("Error reading from backend", e)
                            emit(ErrorEvent(request.sessionId, "Backend communication error: ${e.message}"))
                            break
                        }

                    if (line == null) {
                        // EOF reached
                        log.warn("Backend closed connection unexpectedly")
                        emit(ErrorEvent(request.sessionId, "Backend connection closed"))
                        break
                    }

                    val event = Protocol.parseEvent(line)
                    emit(event)
                    if (event is DoneEvent) {
                        done = true
                    }
                }
            }
        }

    override suspend fun cancel(sessionId: String) {
        if (!isConnected) return

        withContext(Dispatchers.IO) {
            try {
                val gson = com.google.gson.Gson()
                val request = CancelRequest(sessionId = sessionId)
                val w = writer ?: return@withContext
                w.write(gson.toJson(request))
                w.write("\n")
                w.flush()
            } catch (e: Exception) {
                log.warn("Failed to send cancel request", e)
            }
        }
    }

    fun simulateCrash() {
        process?.destroy()
        isConnectedFlag = false
    }

    override fun dispose() {
        scope.cancel()
        try {
            writer?.close()
            reader?.close()
            process?.destroy()
        } catch (e: Exception) {
            log.warn("Error during disposal", e)
        }
    }

    private fun startWatchdog() {
        scope.launch {
            try {
                while (isActive) {
                    delay(1000)
                    if (!isConnectedFlag) break
                    if (process?.isAlive == false) {
                        isConnectedFlag = false
                        attemptReconnect()
                    }
                }
            } catch (e: CancellationException) {
                // Expected during shutdown
            } catch (e: Exception) {
                log.error("Watchdog error", e)
            }
        }
    }

    private suspend fun attemptReconnect() {
        if (reconnectAttempts >= maxReconnectAttempts) {
            log.info("Max reconnection attempts reached")
            return
        }

        reconnectAttempts++
        val backoffMs = 1000L * (1 shl (reconnectAttempts - 1))
        delay(backoffMs)

        try {
            log.info("Attempting reconnection ($reconnectAttempts/$maxReconnectAttempts)")
            connect()
        } catch (e: Exception) {
            log.warn("Reconnection attempt failed, will retry on next watchdog cycle", e)
        }
    }
}
