package com.codebuff.intellij.ui

import com.codebuff.intellij.model.ContextItem
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.components.BorderLayoutPanel
import javax.swing.AbstractListModel
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JScrollPane
import javax.swing.ListCellRenderer
import javax.swing.border.EmptyBorder

/**
 * UI panel for managing context attachments
 * Displays attached files, selections, diagnostics, and diffs
 *
 * Thread-safety: All mutations must occur on the EDT
 */
class ContextAttachmentsPanel : BorderLayoutPanel() {
    private val attachments = mutableListOf<ContextItem>()
    private val model = ContextItemListModel()
    private val list = JBList(model)
    private val countLabel = JLabel("0 attachments")

    init {
        border = JBUI.Borders.empty(8)

        // Configure list renderer
        list.apply {
            cellRenderer = ContextItemCellRenderer()
            emptyText.text = "No context attachments"
        }

        // Header panel
        val headerPanel =
            JBPanel<BorderLayoutPanel>().apply {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                border = EmptyBorder(0, 0, 4, 0)
            }
        headerPanel.add(JLabel("Context Attachments"))
        headerPanel.add(javax.swing.Box.createHorizontalGlue())
        headerPanel.add(countLabel)

        // Scroll pane for list
        val scrollPane =
            JScrollPane(list).apply {
                preferredSize = JBUI.size(400, 200)
            }

        // Button panel
        val buttonPanel =
            JBPanel<BorderLayoutPanel>().apply {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                border = EmptyBorder(4, 0, 0, 0)
            }
        val removeBtn = JButton("Remove")
        removeBtn.addActionListener { removeSelected() }
        val clearBtn = JButton("Clear All")
        clearBtn.addActionListener { clear() }
        buttonPanel.add(removeBtn)
        buttonPanel.add(javax.swing.Box.createHorizontalStrut(4))
        buttonPanel.add(clearBtn)
        buttonPanel.add(javax.swing.Box.createHorizontalGlue())

        // Layout
        addToTop(headerPanel)
        addToCenter(scrollPane)
        addToBottom(buttonPanel)
    }

    fun addAttachment(item: ContextItem) {
        ApplicationManager.getApplication().assertIsDispatchThread()
        model.addItem(item)
        updateCount()
    }

    fun removeSelected() {
        ApplicationManager.getApplication().assertIsDispatchThread()
        val index = list.selectedIndex
        if (index >= 0) {
            model.removeItemAt(index)
            updateCount()
        }
    }

    fun removeAttachment(index: Int) {
        ApplicationManager.getApplication().assertIsDispatchThread()
        if (index in attachments.indices) {
            model.removeItemAt(index)
            updateCount()
        }
    }

    fun clear() {
        ApplicationManager.getApplication().assertIsDispatchThread()
        model.clear()
        updateCount()
    }

    fun getAttachments(): List<ContextItem> {
        ApplicationManager.getApplication().assertIsDispatchThread()
        return attachments.toList()
    }

    fun getAttachmentCount(): Int {
        ApplicationManager.getApplication().assertIsDispatchThread()
        return attachments.size
    }

    fun getDisplayText(index: Int): String {
        ApplicationManager.getApplication().assertIsDispatchThread()
        if (index !in attachments.indices) return ""
        return formatAttachmentText(attachments[index])
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }

    private fun byteSizeUtf8(s: String): Long = s.toByteArray(Charsets.UTF_8).size.toLong()

    private fun updateCount() {
        val count = attachments.size
        countLabel.text = "$count attachment" + if (count == 1) "" else "s"
    }

    private fun formatAttachmentText(item: ContextItem): String {
        return when (item) {
            is ContextItem.Selection -> "${item.path}:${item.startLine}-${item.endLine}"
            is ContextItem.File -> "${item.path} (${formatFileSize(byteSizeUtf8(item.content))})"
            is ContextItem.Diagnostic -> "${item.path}:${item.line} [${item.severity}] ${item.message}"
            is ContextItem.GitDiff -> "Git Diff (${formatFileSize(byteSizeUtf8(item.diff))})"
        }
    }

    /**
     * Custom list model for ContextItems
     */
    private inner class ContextItemListModel : AbstractListModel<ContextItem>() {
        fun addItem(item: ContextItem) {
            attachments.add(item)
            fireIntervalAdded(this, attachments.size - 1, attachments.size - 1)
        }

        fun removeItemAt(index: Int) {
            if (index in attachments.indices) {
                attachments.removeAt(index)
                fireIntervalRemoved(this, index, index)
            }
        }

        fun clear() {
            val size = attachments.size
            attachments.clear()
            if (size > 0) {
                fireIntervalRemoved(this, 0, size - 1)
            }
        }

        override fun getSize(): Int = attachments.size

        override fun getElementAt(index: Int): ContextItem = attachments[index]
    }

    /**
     * Custom cell renderer for ContextItems
     */
    private inner class ContextItemCellRenderer : ListCellRenderer<ContextItem> {
        override fun getListCellRendererComponent(
            list: javax.swing.JList<out ContextItem>,
            value: ContextItem,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean,
        ): java.awt.Component {
            val label = JLabel(formatAttachmentText(value))
            label.isOpaque = true
            label.background = if (isSelected) list.selectionBackground else list.background
            label.foreground = if (isSelected) list.selectionForeground else list.foreground
            label.border = EmptyBorder(4, 4, 4, 4)
            return label
        }
    }
}
