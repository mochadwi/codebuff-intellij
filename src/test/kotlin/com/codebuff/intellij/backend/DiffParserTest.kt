package com.codebuff.intellij.backend

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * TDD Test First: Tests for diff event parsing.
 *
 * Issue: cb-blv.6
 */
class DiffParserTest {
    private fun createDiffEvent(files: List<Map<String, String>>): DiffEvent {
        return DiffEvent(
            sessionId = "test-session",
            files = files,
        )
    }

    @Test
    fun `parseFromEvent extracts file paths`() {
        val event =
            createDiffEvent(
                listOf(
                    mapOf("path" to "src/Main.kt", "before" to "old", "after" to "new"),
                ),
            )

        val diffs = DiffParser.parseFromEvent(event)

        assertEquals(1, diffs.size)
        assertEquals("src/Main.kt", diffs[0].path)
    }

    @Test
    fun `parseFromEvent detects CREATE operation`() {
        val event =
            createDiffEvent(
                listOf(
                    mapOf("path" to "new.kt", "before" to "", "after" to "new content"),
                ),
            )

        val diffs = DiffParser.parseFromEvent(event)

        assertEquals(FileDiff.Operation.CREATE, diffs[0].operation)
    }

    @Test
    fun `parseFromEvent detects MODIFY operation`() {
        val event =
            createDiffEvent(
                listOf(
                    mapOf("path" to "mod.kt", "before" to "old", "after" to "new"),
                ),
            )

        val diffs = DiffParser.parseFromEvent(event)

        assertEquals(FileDiff.Operation.MODIFY, diffs[0].operation)
    }

    @Test
    fun `parseFromEvent detects DELETE operation`() {
        val event =
            createDiffEvent(
                listOf(
                    mapOf("path" to "del.kt", "before" to "content", "after" to ""),
                ),
            )

        val diffs = DiffParser.parseFromEvent(event)

        assertEquals(FileDiff.Operation.DELETE, diffs[0].operation)
    }

    @Test
    fun `parseFromEvent handles multiple files`() {
        val event =
            createDiffEvent(
                listOf(
                    mapOf("path" to "a.kt", "before" to "", "after" to "a"),
                    mapOf("path" to "b.kt", "before" to "b", "after" to ""),
                    mapOf("path" to "c.kt", "before" to "c1", "after" to "c2"),
                ),
            )

        val diffs = DiffParser.parseFromEvent(event)

        assertEquals(3, diffs.size)
    }

    @Test
    fun `parseFromEvent preserves unicode content`() {
        val event =
            createDiffEvent(
                listOf(
                    mapOf("path" to "unicode.kt", "before" to "привет", "after" to "世界"),
                ),
            )

        val diffs = DiffParser.parseFromEvent(event)

        assertEquals("привет", diffs[0].before)
        assertEquals("世界", diffs[0].after)
    }

    @Test
    fun `parseFromEvent handles null before for new files`() {
        val json =
            "{\"type\":\"diff\",\"sessionId\":\"test\",\"files\":[{\"path\":\"new.kt\",\"after\":\"code\"}]}"
        val event = Protocol.parseEvent(json) as DiffEvent

        val diffs = DiffParser.parseFromEvent(event)

        assertEquals(FileDiff.Operation.CREATE, diffs[0].operation)
        assertEquals("", diffs[0].before)
    }
}
