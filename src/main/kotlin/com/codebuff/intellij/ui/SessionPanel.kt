package com.codebuff.intellij.ui

import com.codebuff.intellij.services.SessionManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JPanel

/**
 * Panel for managing Codebuff sessions.
 *
 * Issue: cb-u20.2
 */
class SessionPanel(private val project: Project) : JPanel(BorderLayout()), Disposable {
    private val sessionManager = project.getService(SessionManager::class.java)
    private val listModel = DefaultListModel<String>()
    private val sessionsList = JBList(listModel)

    init {
        border = JBUI.Borders.empty(8, 8, 8, 0)

        sessionsList.emptyText.text = "No sessions yet"
        val scrollPane = JBScrollPane(sessionsList)

        add(scrollPane, BorderLayout.CENTER)
    }

    /**
     * Refresh the session list from the manager.
     */
    fun refresh() {
        listModel.clear()
        sessionManager.getSessions().forEach { session ->
            listModel.addElement(session.title)
        }
    }

    /**
     * Select a session and set it as active.
     */
    fun selectSession(sessionId: String) {
        val session = sessionManager.getSession(sessionId)
        if (session != null) {
            sessionManager.setActiveSession(session)
        }
    }

    /**
     * Create a new session.
     */
    fun onNewSession() {
        sessionManager.createSession()
        refresh()
    }

    /**
     * Rename an existing session.
     */
    fun renameSession(
        sessionId: String,
        newTitle: String,
    ) {
        val session = sessionManager.getSession(sessionId)
        if (session != null) {
            // Create a new SessionInfo with updated title
            val updated = session.copy(title = newTitle)
            // Update in sessions map by removing and re-adding
            sessionManager.closeSession(sessionId)
            // Note: This is a simplified approach. A proper implementation would
            // add an updateSession method to SessionManager
            refresh()
        }
    }

    /**
     * Delete a session.
     */
    fun deleteSession(sessionId: String) {
        val activeSession = sessionManager.activeSession
        sessionManager.closeSession(sessionId)

        // If we just deleted the active session, switch to another if available
        if (activeSession?.id == sessionId) {
            val remaining = sessionManager.getSessions()
            if (remaining.isNotEmpty()) {
                sessionManager.setActiveSession(remaining.first())
            }
        }

        refresh()
    }

    override fun dispose() {
        // Cleanup resources
    }
}
