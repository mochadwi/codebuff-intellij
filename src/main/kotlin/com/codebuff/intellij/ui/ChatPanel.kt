package com.codebuff.intellij.ui

import com.codebuff.intellij.backend.DiffEvent
import com.codebuff.intellij.backend.DoneEvent
import com.codebuff.intellij.backend.ErrorEvent
import com.codebuff.intellij.backend.StreamingEventRouter
import com.codebuff.intellij.backend.TokenEvent
import com.codebuff.intellij.backend.ToolCallEvent
import com.codebuff.intellij.backend.ToolResultEvent
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea

/**
 * Chat panel that displays streaming tokens and responses from backend.
 * Handles loading states, error display, and input enablement.
 *
 * Issue: cb-ble.4
 */
class ChatPanel(private val project: Project) : JPanel(), StreamingEventRouter.EventListener, Disposable {
    private val displayArea =
        JTextArea().apply {
            isEditable = false
            lineWrap = true
            wrapStyleWord = true
        }
    private val scrollPane = JScrollPane(displayArea)

    val cancelButton = JButton("Cancel").apply { isVisible = false }
    private var internalLoading = false
    private var internalInputEnabled = true
    private var currentMessage = ""

    var isLoading: Boolean
        get() = internalLoading
        set(value) {
            internalLoading = value
        }

    var isInputEnabled: Boolean
        get() = internalInputEnabled
        set(value) {
            internalInputEnabled = value
        }

    init {
        layout = BorderLayout()
        add(scrollPane, BorderLayout.CENTER)
        add(cancelButton, BorderLayout.SOUTH)
    }

    fun getDisplayText(): String = displayArea.text

    @JvmName("updateLoading")
    fun setLoading(loading: Boolean) {
        isLoading = loading
        cancelButton.isVisible = loading
        isInputEnabled = !loading
    }

    fun onCancel() {
        isLoading = false
        cancelButton.isVisible = false
        isInputEnabled = true
        currentMessage = ""
    }

    fun setCurrentMessage(msg: String) {
        currentMessage = msg
    }

    fun getCurrentMessage(): String = currentMessage

    // StreamingEventRouter.EventListener implementation
    override fun onToken(event: TokenEvent) {
        displayArea.append(event.text)
        isLoading = true
        isInputEnabled = false
        currentMessage += event.text
    }

    override fun onToolCall(event: ToolCallEvent) {
        isLoading = true
        isInputEnabled = false
        displayArea.append("\n[Calling tool: ${event.tool}]\n")
    }

    override fun onToolResult(event: ToolResultEvent) {
        // Tool results processed internally, may update display
    }

    override fun onDiff(event: DiffEvent) {
        // Diff events routed to diff viewer
    }

    override fun onError(event: ErrorEvent) {
        displayArea.append("\nError: ${event.message}\n")
    }

    override fun onDone(event: DoneEvent) {
        isLoading = false
        isInputEnabled = true
        cancelButton.isVisible = false
    }

    override fun dispose() {
        // Cleanup resources
    }
}
