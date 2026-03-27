package com.codebuff.intellij.diff

import com.codebuff.intellij.backend.FileDiff
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File

/**
 * Actions for accepting/rejecting diffs.
 *
 * Issue: cb-blv.3
 */
object DiffActions {
    fun applyDiff(
        project: Project,
        diff: FileDiff,
        onSuccess: () -> Unit,
    ) {
        WriteCommandAction.runWriteCommandAction(project) {
            val basePath = project.basePath ?: return@runWriteCommandAction
            when (diff.operation) {
                FileDiff.Operation.CREATE -> createFile(basePath, diff)
                FileDiff.Operation.MODIFY -> modifyFile(project, basePath, diff)
                FileDiff.Operation.DELETE -> deleteFile(basePath, diff)
            }
        }
        onSuccess()
    }

    fun rejectDiff(
        project: Project,
        diff: FileDiff,
    ) {
        // No-op - just dismiss
    }

    private fun createFile(
        basePath: String,
        diff: FileDiff,
    ) {
        val file = File(basePath, diff.path)
        file.parentFile?.mkdirs()
        file.writeText(diff.after)
    }

    private fun modifyFile(
        project: Project,
        basePath: String,
        diff: FileDiff,
    ) {
        val filePath = "$basePath/${diff.path}"
        val vFile =
            LocalFileSystem.getInstance().findFileByPath(filePath)
                ?: return

        val document = FileDocumentManager.getInstance().getDocument(vFile)
        document?.setText(diff.after)
    }

    private fun deleteFile(
        basePath: String,
        diff: FileDiff,
    ) {
        val file = File(basePath, diff.path)
        if (file.exists()) {
            file.delete()
        }
    }
}

/**
 * AnAction for accepting a single diff.
 */
class AcceptDiffAction :
    AnAction("Accept", "Apply this change", AllIcons.Actions.Commit) {
    override fun actionPerformed(e: AnActionEvent) {
        // Placeholder - will be connected to DiffViewerService
    }
}

/**
 * AnAction for rejecting a single diff.
 */
class RejectDiffAction :
    AnAction("Reject", "Discard this change", AllIcons.Actions.Cancel) {
    override fun actionPerformed(e: AnActionEvent) {
        // Placeholder - will be connected to DiffViewerService
    }
}

/**
 * AnAction for accepting all diffs at once.
 */
class AcceptAllAction :
    AnAction("Accept All", "Apply all changes", AllIcons.Actions.Commit) {
    override fun actionPerformed(e: AnActionEvent) {
        // Placeholder - will be connected to DiffViewerService
    }
}
