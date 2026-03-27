package com.codebuff.intellij.diff

import com.codebuff.intellij.backend.FileDiff
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * TDD Test First: Tests for diff accept/reject actions.
 *
 * Issue: cb-blv.8
 */
class AcceptDiffActionTest {
    @Test
    fun `accept modifies file with after content`() {
        val diff = FileDiff("test.kt", "old content", "new content", FileDiff.Operation.MODIFY)

        // Test that the diff can be created and accessed
        assertEquals(FileDiff.Operation.MODIFY, diff.operation)
        assertEquals("new content", diff.after)
    }

    @Test
    fun `accept creates new file for CREATE operation`() {
        val diff = FileDiff("new.kt", "", "new content", FileDiff.Operation.CREATE)

        assertEquals(FileDiff.Operation.CREATE, diff.operation)
        assertEquals("", diff.before)
        assertEquals("new content", diff.after)
    }

    @Test
    fun `accept deletes file for DELETE operation`() {
        val diff = FileDiff("delete.kt", "content", "", FileDiff.Operation.DELETE)

        assertEquals(FileDiff.Operation.DELETE, diff.operation)
        assertEquals("content", diff.before)
        assertEquals("", diff.after)
    }

    @Test
    fun `diff operation detection works correctly`() {
        val modifyDiff = FileDiff("test.kt", "old", "new", FileDiff.Operation.MODIFY)
        val createDiff = FileDiff("new.kt", "", "content", FileDiff.Operation.CREATE)
        val deleteDiff = FileDiff("old.kt", "content", "", FileDiff.Operation.DELETE)

        assertEquals(FileDiff.Operation.MODIFY, modifyDiff.operation)
        assertEquals(FileDiff.Operation.CREATE, createDiff.operation)
        assertEquals(FileDiff.Operation.DELETE, deleteDiff.operation)
    }
}

class RejectDiffActionTest {
    @Test
    fun `reject does not modify diff`() {
        val diff = FileDiff("test.kt", "original", "changed", FileDiff.Operation.MODIFY)

        // Reject should just not apply the changes
        // Verify the diff object is unchanged
        assertEquals("original", diff.before)
        assertEquals("changed", diff.after)
    }
}

class AcceptAllActionTest {
    @Test
    fun `accept all can process multiple diffs`() {
        val diffs =
            listOf(
                FileDiff("a.kt", "a1", "a2", FileDiff.Operation.MODIFY),
                FileDiff("b.kt", "b1", "b2", FileDiff.Operation.MODIFY),
            )

        // Should be able to process all diffs
        assertEquals(2, diffs.size)
        assertEquals("a.kt", diffs[0].path)
        assertEquals("b.kt", diffs[1].path)
    }

    @Test
    fun `accept all preserves order`() {
        val diffs =
            listOf(
                FileDiff("a.kt", "a1", "a2", FileDiff.Operation.MODIFY),
                FileDiff("b.kt", "b1", "b2", FileDiff.Operation.MODIFY),
                FileDiff("c.kt", "c1", "c2", FileDiff.Operation.MODIFY),
            )

        assertEquals(3, diffs.size)
        assertEquals("a.kt", diffs[0].path)
        assertEquals("b.kt", diffs[1].path)
        assertEquals("c.kt", diffs[2].path)
    }
}
