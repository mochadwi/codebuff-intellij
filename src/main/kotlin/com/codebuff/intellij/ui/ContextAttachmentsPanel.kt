package com.codebuff.intellij.ui

import com.codebuff.intellij.model.ContextItem
import com.intellij.ui.components.JBPanel
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JScrollPane
import javax.swing.border.EmptyBorder

/**
 * UI panel for managing context attachments
 * Displays attached files, selections, diagnostics, and diffs
 */
class ContextAttachmentsPanel : JBPanel<ContextAttachmentsPanel>() {
    private val attachments = mutableListOf<ContextItem>()
    private val listPanel = JBPanel<JBPanel<*>>()
    private val countLabel = JLabel("0 attachments")

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = EmptyBorder(8, 8, 8, 8)

        // Header
        val headerPanel = JBPanel<JBPanel<*>>()
        headerPanel.layout = BoxLayout(headerPanel, BoxLayout.X_AXIS)
        headerPanel.add(JLabel("Context Attachments"))
        headerPanel.add(javax.swing.Box.createHorizontalGlue())
        headerPanel.add(countLabel)

        // List area
        listPanel.layout = BoxLayout(listPanel, BoxLayout.Y_AXIS)
        val scrollPane = JScrollPane(listPanel)

        // Buttons
        val buttonPanel = JBPanel<JBPanel<*>>()
        buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.X_AXIS)
        val clearBtn = JButton("Clear All")
        clearBtn.addActionListener { clear() }
        buttonPanel.add(javax.swing.Box.createHorizontalGlue())
        buttonPanel.add(clearBtn)

        add(headerPanel)
        add(scrollPane)
        add(buttonPanel)
    }

    fun addAttachment(item: ContextItem) {
        attachments.add(item)
        refreshUI()
    }

    fun removeAttachment(index: Int) {
        if (index in attachments.indices) {
            attachments.removeAt(index)
            refreshUI()
        }
    }

    fun clear() {
        attachments.clear()
        refreshUI()
    }

    fun getAttachments(): List<ContextItem> = attachments.toList()

    fun getAttachmentCount(): Int = attachments.size

    fun getDisplayText(index: Int): String {
        if (index !in attachments.indices) return ""
        val item = attachments[index]
        return when (item) {
            is ContextItem.Selection -> "${item.path} (lines ${item.startLine}-${item.endLine})"
            is ContextItem.File -> "${item.path} (${formatFileSize(item.content.length.toLong())})"
            is ContextItem.Diagnostic -> "${item.path}:${item.line} [${item.severity}]"
            is ContextItem.GitDiff -> "Git Diff"
        }
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes bytes"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }

    private fun refreshUI() {
        countLabel.text = "${attachments.size} attachment${if (attachments.size != 1) "s" else ""}"
        listPanel.removeAll()
        listPanel.revalidate()
        listPanel.repaint()
    }
}
