package com.codebuff.intellij.services

import com.codebuff.intellij.model.ContextItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * TDD Tests for ContextItem data classes
 * Issue: cb-0jl.6
 * RED Phase: All tests should fail initially
 */
class ContextCollectorTest {
    // ============ Selection Context Tests ============

    @Test
    fun `Selection creation with valid data`() {
        val selection =
            ContextItem.Selection(
                path = "src/Main.kt",
                content = "fun main() {}",
                startLine = 1,
                endLine = 1,
                language = "kotlin",
            )

        assertEquals("src/Main.kt", selection.path)
        assertEquals("fun main() {}", selection.content)
        assertEquals(1, selection.startLine)
        assertEquals(1, selection.endLine)
        assertEquals("kotlin", selection.language)
    }

    @Test
    fun `Selection supports multiline range`() {
        val selection =
            ContextItem.Selection(
                path = "File.kt",
                content = "line1\nline2\nline3",
                startLine = 10,
                endLine = 15,
                language = "kotlin",
            )

        assertEquals(10, selection.startLine)
        assertEquals(15, selection.endLine)
        assertTrue(selection.content.contains("\n"))
    }

    @Test
    fun `Selection equals based on all fields`() {
        val sel1 = ContextItem.Selection("path.kt", "code", 1, 2, "kotlin")
        val sel2 = ContextItem.Selection("path.kt", "code", 1, 2, "kotlin")
        val sel3 = ContextItem.Selection("path.kt", "code", 1, 3, "kotlin")

        assertEquals(sel1, sel2)
        assertTrue(sel1 != sel3)
    }

    // ============ File Context Tests ============

    @Test
    fun `File creation with valid data`() {
        val file =
            ContextItem.File(
                path = "src/utils/Helper.kt",
                content = "object Helper { }",
                language = "kotlin",
            )

        assertEquals("src/utils/Helper.kt", file.path)
        assertEquals("object Helper { }", file.content)
        assertEquals("kotlin", file.language)
    }

    @Test
    fun `File supports large content`() {
        val largeContent = "x".repeat(100_000)
        val file =
            ContextItem.File(
                path = "large.txt",
                content = largeContent,
                language = "text",
            )

        assertEquals(largeContent.length, file.content.length)
    }

    @Test
    fun `File equals based on all fields`() {
        val file1 = ContextItem.File("path.kt", "code", "kotlin")
        val file2 = ContextItem.File("path.kt", "code", "kotlin")
        val file3 = ContextItem.File("other.kt", "code", "kotlin")

        assertEquals(file1, file2)
        assertTrue(file1 != file3)
    }

    // ============ Diagnostic Context Tests ============

    @Test
    fun `Diagnostic creation with error severity`() {
        val diag =
            ContextItem.Diagnostic(
                path = "src/Main.kt",
                line = 10,
                severity = "error",
                message = "Type mismatch",
            )

        assertEquals("src/Main.kt", diag.path)
        assertEquals(10, diag.line)
        assertEquals("error", diag.severity)
        assertEquals("Type mismatch", diag.message)
    }

    @Test
    fun `Diagnostic supports different severity levels`() {
        val error = ContextItem.Diagnostic("f.kt", 1, "error", "msg")
        val warning = ContextItem.Diagnostic("f.kt", 1, "warning", "msg")
        val info = ContextItem.Diagnostic("f.kt", 1, "info", "msg")

        assertEquals("error", error.severity)
        assertEquals("warning", warning.severity)
        assertEquals("info", info.severity)
    }

    // ============ GitDiff Context Tests ============

    @Test
    fun `GitDiff creation with diff content`() {
        val diff =
            ContextItem.GitDiff(
                diff = "--- a/file.txt\n+++ b/file.txt\n@@ -1,1 +1,2 @@\n line\n+new",
            )

        assertTrue(diff.diff.contains("---"))
        assertTrue(diff.diff.contains("+++"))
        assertTrue(diff.diff.contains("@@"))
    }

    @Test
    fun `GitDiff empty diff is valid`() {
        val emptyDiff = ContextItem.GitDiff(diff = "")

        assertEquals("", emptyDiff.diff)
    }

    // ============ Polymorphic Context Tests ============

    @Test
    fun `Context items are polymorphic`() {
        val items: List<ContextItem> =
            listOf(
                ContextItem.Selection("s.kt", "code", 1, 2, "kotlin"),
                ContextItem.File("f.kt", "content", "kotlin"),
                ContextItem.Diagnostic("d.kt", 5, "error", "msg"),
                ContextItem.GitDiff("diff content"),
            )

        assertEquals(4, items.size)
        assertTrue(items[0] is ContextItem.Selection)
        assertTrue(items[1] is ContextItem.File)
        assertTrue(items[2] is ContextItem.Diagnostic)
        assertTrue(items[3] is ContextItem.GitDiff)
    }
}
