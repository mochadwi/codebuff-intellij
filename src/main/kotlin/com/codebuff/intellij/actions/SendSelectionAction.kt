package com.codebuff.intellij.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.wm.ToolWindowManager

class SendSelectionAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val selectionModel = editor.selectionModel
        val selectedText = selectionModel.selectedText ?: return

        // Open the Codebuff tool window
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Codebuff")
        toolWindow?.show()

        // TODO: Send the selected text to the chat panel
        // This will be wired up in Phase 2 when backend integration is complete
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val hasSelection = editor?.selectionModel?.hasSelection() == true

        e.presentation.isEnabled = e.project != null && hasSelection
        e.presentation.isVisible = e.project != null
    }
}
