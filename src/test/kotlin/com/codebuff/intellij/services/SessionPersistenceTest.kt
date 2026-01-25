package com.codebuff.intellij.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.project.Project
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for session persistence using PersistentStateComponent.
 *
 * Issue: cb-u20.6
 */
class SessionPersistenceTest {
    private lateinit var sessionManager: SessionManager

    @BeforeEach
    fun setUp() {
        val mockProject: Project = mockk(relaxed = true)
        sessionManager = SessionManager(mockProject)
    }

    @Test
    fun `implements PersistentStateComponent`() {
        assertTrue(sessionManager is PersistentStateComponent<*>)
    }

    @Test
    fun `getState returns current state`() {
        val state = sessionManager.state

        assertNotNull(state)
    }

    @Test
    fun `loadState restores sessions`() {
        val sessionInfo = SessionManager.SessionInfo("id-1", "Session 1", 1000L, 2000L)
        val savedState =
            SessionManager.State(
                sessions = mutableMapOf("id-1" to sessionInfo),
                activeSessionId = "id-1",
            )

        sessionManager.loadState(savedState)

        assertEquals(1, sessionManager.getSessions().size)
        assertEquals("id-1", sessionManager.activeSession?.id)
    }

    @Test
    fun `sessions persist across reload`() {
        sessionManager.createSession("Test Session")
        val originalState = sessionManager.state

        // Simulate reload
        sessionManager.loadState(originalState)

        assertEquals(1, sessionManager.getSessions().size)
    }

    @Test
    fun `active session restored on load`() {
        val session = sessionManager.createSession("Active")
        sessionManager.setActiveSession(session)
        val state = sessionManager.state

        sessionManager.loadState(state)

        assertEquals(session.id, sessionManager.activeSession?.id)
    }

    @Test
    fun `session info contains all required fields`() {
        val session = sessionManager.createSession("Test")

        assertNotNull(session.id)
        assertNotNull(session.title)
        assertTrue(session.createdAt > 0)
        assertTrue(session.lastActivityAt > 0)
    }
}
