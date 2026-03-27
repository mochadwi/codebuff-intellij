package com.codebuff.intellij.diff

import com.codebuff.intellij.backend.FileDiff
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * TDD Test First: Tests for diff viewer service.
 *
 * Issue: cb-blv.7
 */
class DiffViewerServiceTest {
    private fun createMockProject() =
        object {
            fun <T> getService(clazz: Class<T>): T? = null
        }

    @Test
    fun `createDiffChain creates single request for single file`() {
        val service = DiffViewerService(null)
        val diffs =
            listOf(
                FileDiff("test.kt", "old", "new", FileDiff.Operation.MODIFY),
            )

        val chain = service.createDiffChain(diffs)

        assertEquals(1, chain.requests.size)
    }

    @Test
    fun `createDiffChain creates chain for multiple files`() {
        val service = DiffViewerService(null)
        val diffs =
            listOf(
                FileDiff("a.kt", "a1", "a2", FileDiff.Operation.MODIFY),
                FileDiff("b.kt", "b1", "b2", FileDiff.Operation.MODIFY),
            )

        val chain = service.createDiffChain(diffs)

        assertEquals(2, chain.requests.size)
    }

    @Test
    fun `diff request chain creates requests with correct size`() {
        val service = DiffViewerService(null)
        val diff = FileDiff("test.kt", "old", "new", FileDiff.Operation.MODIFY)

        val chain = service.createDiffChain(listOf(diff))

        // Should have a request with the FileDiff stored
        assertEquals(1, chain.requests.size)
    }

    @Test
    fun `showDiffs does nothing for empty list`() {
        val service = DiffViewerService(null)
        service.showDiffs(emptyList())

        // Should not throw
    }
}
