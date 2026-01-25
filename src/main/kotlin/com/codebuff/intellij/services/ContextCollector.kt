package com.codebuff.intellij.services

import com.codebuff.intellij.model.ContextItem
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager


private val LOG = Logger.getInstance(ContextCollector::class.java)

/**
 * Service for collecting editor context (selections, files, diagnostics, git diff)
 * to send as context with user prompts
 *
 * Thread-safety: All methods assume EDT (Editor, selections) or background thread (files).
 * PSI access is wrapped in ReadAction to ensure proper IDE state.
 */
@Service(Service.Level.PROJECT)
class ContextCollector(private val project: Project) {

    companion object {
        const val MAX_FILE_SIZE = 1024 * 1024 // 1MB
        const val MAX_SELECTION_SIZE = 100_000 // 100KB - prevent huge payloads
    }

    /**
     * Collect currently selected text from editor
     * Called on EDT when user selection changes
     *
     * @return Selection context item or null if no selection
     */
    fun collectSelection(editor: Editor): ContextItem.Selection? {
        ApplicationManager.getApplication().assertIsDispatchThread()

        val selectionModel = editor.selectionModel
        if (!selectionModel.hasSelection()) return null

        val startOffset = selectionModel.selectionStart
        val endOffset = selectionModel.selectionEnd
        val selectedText = selectionModel.selectedText ?: return null

        // Guard against huge selections
        if (selectedText.length > MAX_SELECTION_SIZE) {
            LOG.warn("Selection exceeds max size: ${selectedText.length} > $MAX_SELECTION_SIZE")
            return null
        }

        val document = editor.document
        val file = FileDocumentManager.getInstance().getFile(document)

        // Normalize line endings for consistent protocol
        val normalizedText = selectedText.replace("\r\n", "\n").replace("\r", "\n")

        // Compute line numbers, handling edge case where selection ends at column 0 of next line
        val endOffsetForLine = maxOf(startOffset, endOffset - 1)
        val startLine = document.getLineNumber(startOffset) + 1
        val endLine = document.getLineNumber(endOffsetForLine) + 1

        return ReadAction.compute<ContextItem.Selection?, RuntimeException> {
            val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
                ?: file?.let { PsiManager.getInstance(project).findFile(it) }

            ContextItem.Selection(
                path = file?.let { getRelativePath(it) } ?: "untitled",
                content = normalizedText,
                startLine = startLine,
                endLine = endLine,
                language = psiFile?.language?.id ?: "text"
            )
        }
    }

    /**
     * Collect entire file content
     * Safe to call from background threads
     *
     * @return File context item or null if file is invalid, too large, or binary
     */
    fun collectFile(virtualFile: VirtualFile): ContextItem.File? {
        // Validate file state
        if (!virtualFile.isValid) {
            LOG.debug("Cannot collect invalid file: ${virtualFile.path}")
            return null
        }

        if (virtualFile.isDirectory) {
            LOG.debug("Cannot collect directory: ${virtualFile.path}")
            return null
        }

        if (virtualFile.length < 0) {
            LOG.warn("File size unknown: ${virtualFile.path}")
            return null
        }

        if (virtualFile.length > MAX_FILE_SIZE) {
            LOG.debug("File exceeds max size: ${virtualFile.path} (${virtualFile.length} > $MAX_FILE_SIZE)")
            return null
        }

        // Skip binary files to avoid garbage data
        if (virtualFile.fileType.isBinary) {
            LOG.debug("Skipping binary file: ${virtualFile.path}")
            return null
        }

        // Load file content, respecting charset
        val content = try {
            VfsUtilCore.loadText(virtualFile)
        } catch (t: Throwable) {
            LOG.warn("Failed to load file content: ${virtualFile.path}", t)
            return null
        }

        // PSI lookup under read action
        return ReadAction.compute<ContextItem.File?, RuntimeException> {
            val psiFile = PsiManager.getInstance(project).findFile(virtualFile)

            ContextItem.File(
                path = getRelativePath(virtualFile),
                content = content,
                language = psiFile?.language?.id ?: "text"
            )
        }
    }

    /**
     * Collect diagnostics/inspections for a file
     * @return List of diagnostic context items
     */
    fun collectDiagnostics(@Suppress("UNUSED_PARAMETER") file: VirtualFile): List<ContextItem.Diagnostic> {
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
     * Handles Windows paths and symlinks correctly
     */
    private fun getRelativePath(file: VirtualFile): String {
        @Suppress("DEPRECATION")
        val baseDir = project.baseDir
        return if (baseDir != null) {
            VfsUtilCore.getRelativePath(file, baseDir) ?: file.path
        } else {
            project.basePath?.let { basePath ->
                val path = file.path
                if (path.startsWith(basePath)) {
                    path.removePrefix(basePath).removePrefix("/")
                } else {
                    path
                }
            } ?: file.path
        }
    }
}
