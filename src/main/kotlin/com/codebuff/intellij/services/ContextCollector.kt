package com.codebuff.intellij.services

import com.codebuff.intellij.model.ContextItem
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager

/**
 * Service for collecting editor context (selections, files, diagnostics, git diff)
 * to send as context with user prompts
 */
@Service(Service.Level.PROJECT)
class ContextCollector(private val project: Project) {

    companion object {
        const val MAX_FILE_SIZE = 1024 * 1024 // 1MB
    }

    /**
     * Collect currently selected text from editor
     * @return Selection context item or null if no selection
     */
    fun collectSelection(editor: Editor): ContextItem.Selection? {
        val selectionModel = editor.selectionModel
        if (!selectionModel.hasSelection()) return null

        val document = editor.document
        val file = FileDocumentManager.getInstance().getFile(document)
        val psiFile = file?.let { PsiManager.getInstance(project).findFile(it) }

        val selectedText = selectionModel.selectedText ?: return null
        val startOffset = selectionModel.selectionStart
        val endOffset = selectionModel.selectionEnd

        val startLine = document.getLineNumber(startOffset) + 1
        val endLine = document.getLineNumber(endOffset) + 1

        return ContextItem.Selection(
            path = file?.let { getRelativePath(it) } ?: "untitled",
            content = selectedText,
            startLine = startLine,
            endLine = endLine,
            language = psiFile?.language?.id ?: "text"
        )
    }

    /**
     * Collect entire file content
     * @return File context item or null if file is too large
     */
    fun collectFile(virtualFile: VirtualFile): ContextItem.File? {
        // Check file size limit
        if (virtualFile.length > MAX_FILE_SIZE) return null

        val content = try {
            String(virtualFile.contentsToByteArray(), Charsets.UTF_8)
        } catch (e: Exception) {
            return null
        }

        val psiFile = PsiManager.getInstance(project).findFile(virtualFile)

        return ContextItem.File(
            path = getRelativePath(virtualFile),
            content = content,
            language = psiFile?.language?.id ?: "text"
        )
    }

    /**
     * Collect diagnostics/inspections for a file
     * @return List of diagnostic context items
     */
    fun collectDiagnostics(file: VirtualFile): List<ContextItem.Diagnostic> {
        // TODO: Implement diagnostics collection in separate task
        return emptyList()
    }

    /**
     * Collect git diff for uncommitted changes
     * @return GitDiff context item or null if not a git repo or no changes
     */
    fun collectGitDiff(): ContextItem.GitDiff? {
        // TODO: Implement git diff collection in separate task
        return null
    }

    /**
     * Get relative path from project root
     */
    private fun getRelativePath(file: VirtualFile): String {
        val basePath = project.basePath ?: return file.path
        val path = file.path
        return if (path.startsWith(basePath)) {
            path.removePrefix(basePath).removePrefix("/")
        } else {
            path
        }
    }
}
