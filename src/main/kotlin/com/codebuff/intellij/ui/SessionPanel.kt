package com.codebuff.intellij.ui

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.DefaultListModel
import javax.swing.JPanel

class SessionPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val listModel = DefaultListModel<String>()
    private val sessionsList = JBList(listModel)

    init {
        border = JBUI.Borders.empty(8, 8, 8, 0)

        sessionsList.emptyText.text = "No sessions yet"
        val scrollPane = JBScrollPane(sessionsList)

        add(scrollPane, BorderLayout.CENTER)
    }
}
