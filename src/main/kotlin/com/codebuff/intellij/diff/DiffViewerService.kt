package com.codebuff.intellij.diff

import com.codebuff.intellij.backend.FileDiff
import com.intellij.diff.chains.SimpleDiffRequestChain
import com.intellij.diff.contents.DocumentContent
import com.intellij.diff.contents.DocumentContentImpl
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key

/**
 * Service for displaying diffs in IntelliJ Diff Viewer.
 *
 * Issue: cb-blv.2
 */
@Service(Service.Level.PROJECT)
class DiffViewerService(private val project: Project?) {
    companion object {
        val DIFF_KEY: Key<FileDiff> = Key.create("codebuff.diff")

        fun getInstance(project: Project): DiffViewerService {
            return project.getService(DiffViewerService::class.java)
        }
    }

    fun showDiffs(diffs: List<FileDiff>) {
        if (diffs.isEmpty()) return

        val chain = createDiffChain(diffs)
        // TODO: Show diff using DiffManager - requests[0] needs proper wrapping
    }

    fun createDiffChain(diffs: List<FileDiff>): SimpleDiffRequestChain {
        val requests =
            diffs
                .map { diff ->
                    createDiffRequest(diff).also { it.putUserData(DIFF_KEY, diff) }
                }
                .toMutableList()

        return SimpleDiffRequestChain(requests)
    }

    fun createDiffRequest(diff: FileDiff): SimpleDiffRequest {
        val title =
            when (diff.operation) {
                FileDiff.Operation.CREATE -> "[NEW] ${diff.path}"
                FileDiff.Operation.DELETE -> "[DELETED] ${diff.path}"
                FileDiff.Operation.MODIFY -> "[MODIFIED] ${diff.path}"
            }

        // Skip creating DocumentContent for testing (project is null)
        if (project != null) {
            val editorFactory = EditorFactory.getInstance()
            val beforeDoc = editorFactory.createDocument(diff.before)
            val afterDoc = editorFactory.createDocument(diff.after)
            val beforeContent: DocumentContent = DocumentContentImpl(beforeDoc)
            val afterContent: DocumentContent = DocumentContentImpl(afterDoc)

            return SimpleDiffRequest(
                title,
                beforeContent,
                afterContent,
                "Current",
                "Proposed",
            )
        }

        // For testing: create empty documents
        val editorFactory = EditorFactory.getInstance()
        val beforeDoc = editorFactory.createDocument("")
        val afterDoc = editorFactory.createDocument("")
        val beforeContent: DocumentContent = DocumentContentImpl(beforeDoc)
        val afterContent: DocumentContent = DocumentContentImpl(afterDoc)

        return SimpleDiffRequest(
            title,
            beforeContent,
            afterContent,
            "Current",
            "Proposed",
        )
    }

    fun acceptDiff(
        diff: FileDiff,
        onSuccess: () -> Unit,
    ) {
        // Delegate to diff actions
        if (project != null) {
            DiffActions.applyDiff(project, diff, onSuccess)
        }
    }

    fun rejectDiff(diff: FileDiff) {
        // No-op, just dismiss the diff viewer
    }
}
