package com.codebuff.intellij.diff

import com.codebuff.intellij.backend.FileDiff
import com.intellij.openapi.project.Project

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
        // TODO: Implement diff application
        // - Write file to disk using WriteCommandAction
        // - Handle create/modify/delete operations
        onSuccess()
    }

    fun rejectDiff(
        project: Project,
        diff: FileDiff,
    ) {
        // No-op for now
    }
}
