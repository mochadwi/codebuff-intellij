package com.codebuff.intellij.backend

import com.intellij.testFramework.BasePlatformTestCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * TDD Test First: Tests for CLI process lifecycle before implementing.
 * 
 * Issue: cb-ble.7
 * Tests process start/stop, reconnection logic, and cleanup.
 */
class CliBackendClientTest : BasePlatformTestCase() {
    
    private lateinit var client: CliBackendClient
    
    override fun setUp() {
        super.setUp()
        // Use echo as mock CLI for testing
        client = CliBackendClient(project, "echo")
    }
    
    override fun tearDown() {
        client.dispose()
        super.tearDown()
    }
    
    @Test
    fun `connect starts process`() = runBlocking {
        client.connect()
        
        assertTrue(client.isConnected, "Process should be connected after connect()")
    }
    
    @Test
    fun `disconnect stops process`() = runBlocking {
        client.connect()
        client.disconnect()
        
        assertFalse(client.isConnected, "Process should be disconnected after disconnect()")
    }
    
    @Test
    fun `dispose cleans up process`() = runBlocking {
        client.connect()
        client.dispose()
        
        assertFalse(client.isConnected, "Process should be cleaned up after dispose()")
    }
    
    @Test
    fun `reconnects after process crash`() = runBlocking {
        client.connect()
        assertTrue(client.isConnected, "Initial connection should succeed")
        
        // Simulate crash
        client.simulateCrash()
        
        // Wait for reconnect with backoff
        delay(2000)
        
        assertTrue(client.isConnected, "Should reconnect after crash")
    }
    
    @Test
    fun `respects max reconnect attempts`() = runBlocking {
        client.maxReconnectAttempts = 2
        client.connect()
        
        // Trigger crashes beyond max attempts
        repeat(3) {
            client.simulateCrash()
            delay(1000)
        }
        
        delay(2000)
        
        assertFalse(client.isConnected, "Should give up after max attempts")
    }
}
