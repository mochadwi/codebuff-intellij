package com.codebuff.intellij.ui

import com.codebuff.intellij.CodebuffBundle
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBTextField
import com.intellij.ui.content.ContentFactory
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JSplitPane

class CodebuffToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(
        project: Project,
        toolWindow: ToolWindow,
    ) {
        val contentFactory = ContentFactory.getInstance()
        val mainPanel = CodebuffMainPanel(project)
        val content = contentFactory.createContent(mainPanel, null, false)
        Disposer.register(content, mainPanel)
        toolWindow.contentManager.addContent(content)
    }
}

internal class CodebuffMainPanel(project: Project) : JPanel(BorderLayout()), Disposable {
    private val chatPanel = ChatPanel(project)
    private val sessionPanel = SessionPanel(project)
    private val inputField = JBTextField()
    private val sendButton = JButton(CodebuffBundle.message("chat.send"))

    init {
        Disposer.register(this, chatPanel)
        Disposer.register(this, sessionPanel)

        val splitPane =
            JSplitPane(JSplitPane.HORIZONTAL_SPLIT).apply {
                leftComponent = sessionPanel
                rightComponent = chatPanel
                resizeWeight = 0.25
                border = null
            }

        val inputPanel =
            JPanel(BorderLayout()).apply {
                border = JBUI.Borders.empty(8, 8)
                add(inputField, BorderLayout.CENTER)
                add(sendButton, BorderLayout.EAST)
            }

        border = JBUI.Borders.empty()
        add(splitPane, BorderLayout.CENTER)
        add(inputPanel, BorderLayout.SOUTH)
    }

    override fun dispose() {
        // Child panels are disposed via Disposer.register
    }
}
