package com.codebuff.intellij.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JTextArea

class ChatPanel(private val project: Project) : JPanel(BorderLayout()), Disposable {

    private val transcriptArea = JTextArea().apply {
        isEditable = false
        lineWrap = true
        wrapStyleWord = true
        border = JBUI.Borders.empty(8)
    }

    init {
        border = JBUI.Borders.empty()
        val scrollPane = JBScrollPane(transcriptArea)
        add(scrollPane, BorderLayout.CENTER)
    }

    override fun dispose() {
        // Cleanup resources
    }
}
