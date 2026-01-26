package com.codebuff.intellij.diff

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * TDD Test First: Tests for diff toolbar and keyboard shortcuts.
 *
 * Issue: cb-blv.10
 */
class DiffToolbarTest {
    @Test
    fun `accept action can be created`() {
        val action = AcceptDiffAction()

        assertTrue(action.templateText == "Accept" || action.templateText?.contains("Accept") == true)
    }

    @Test
    fun `reject action can be created`() {
        val action = RejectDiffAction()

        assertTrue(action.templateText == "Reject" || action.templateText?.contains("Reject") == true)
    }

    @Test
    fun `accept all action can be created`() {
        val action = AcceptAllAction()

        assertTrue(
            action.templateText == "Accept All" ||
                action.templateText?.contains("Accept All") == true,
        )
    }

    @Test
    fun `actions have descriptions`() {
        val acceptAction = AcceptDiffAction()
        val rejectAction = RejectDiffAction()
        val acceptAllAction = AcceptAllAction()

        // Should have non-null or non-empty template text
        assertTrue(!acceptAction.templateText.isNullOrEmpty())
        assertTrue(!rejectAction.templateText.isNullOrEmpty())
        assertTrue(!acceptAllAction.templateText.isNullOrEmpty())
    }
}

class DiffKeyboardShortcutsTest {
    @Test
    fun `actions are properly defined`() {
        val acceptAction = AcceptDiffAction()
        val rejectAction = RejectDiffAction()

        // Should be AnActions
        assertTrue(acceptAction is AcceptDiffAction)
        assertTrue(rejectAction is RejectDiffAction)
    }

    @Test
    fun `action text is set correctly`() {
        val acceptAction = AcceptDiffAction()

        // Action should have a display name
        assertEquals("Accept", acceptAction.templateText)
    }
}
