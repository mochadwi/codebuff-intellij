package com.codebuff.intellij.diff

import com.intellij.openapi.actionSystem.AnAction

/**
 * Custom diff action provider for Codebuff diffs.
 *
 * Issue: cb-blv.5
 */
class CodebuffDiffActionProvider {
    fun getActions(): List<AnAction> {
        return listOf(
            AcceptDiffAction(),
            RejectDiffAction(),
            AcceptAllAction(),
        )
    }

    fun getPopupActions(): List<AnAction> {
        return getActions()
    }
}
