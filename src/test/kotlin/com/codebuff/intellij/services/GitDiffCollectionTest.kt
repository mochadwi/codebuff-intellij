package com.codebuff.intellij.services

import com.codebuff.intellij.model.ContextItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * TDD Tests for git diff collection
 * Issue: cb-0jl.8
 * RED Phase: All tests should fail initially
 */
class GitDiffCollectionTest {
    // ============ Unified Diff Format Tests ============

    @Test
    fun `GitDiff with unified diff format headers`() {
        val diff =
            """
            --- a/src/main.kt
            +++ b/src/main.kt
            @@ -1,3 +1,4 @@
             fun main() {
            +    println("hello")
                 val x = 1
            """.trimIndent()

        val gitDiff = ContextItem.GitDiff(diff = diff)

        assertTrue(gitDiff.diff.contains("---"))
        assertTrue(gitDiff.diff.contains("+++"))
        assertTrue(gitDiff.diff.contains("@@"))
    }

    @Test
    fun `GitDiff with file addition`() {
        val diff =
            """
            --- /dev/null
            +++ b/new_file.kt
            @@ -0,0 +1,5 @@
            +fun newFunction() {
            +    return 42
            +}
            """.trimIndent()

        val gitDiff = ContextItem.GitDiff(diff = diff)

        assertTrue(gitDiff.diff.contains("/dev/null"))
        assertTrue(gitDiff.diff.contains("new_file.kt"))
    }

    @Test
    fun `GitDiff with file deletion`() {
        val diff =
            """
            --- a/old_file.kt
            +++ /dev/null
            @@ -1,3 +0,0 @@
            -fun oldFunction() {
            -    return null
            -}
            """.trimIndent()

        val gitDiff = ContextItem.GitDiff(diff = diff)

        assertTrue(gitDiff.diff.contains("old_file.kt"))
        assertTrue(gitDiff.diff.contains("/dev/null"))
    }

    // ============ Added/Removed Lines Tests ============

    @Test
    fun `GitDiff tracks added lines with plus prefix`() {
        val diff =
            """
            --- a/file.kt
            +++ b/file.kt
             existing line
            +new added line
             another existing line
            """.trimIndent()

        val gitDiff = ContextItem.GitDiff(diff = diff)

        assertTrue(gitDiff.diff.contains("+new added line"))
    }

    @Test
    fun `GitDiff tracks removed lines with minus prefix`() {
        val diff =
            """
            --- a/file.kt
            +++ b/file.kt
             existing line
            -removed line
             another existing line
            """.trimIndent()

        val gitDiff = ContextItem.GitDiff(diff = diff)

        assertTrue(gitDiff.diff.contains("-removed line"))
    }

    @Test
    fun `GitDiff with multiple hunks`() {
        val diff =
            """
            --- a/file.kt
            +++ b/file.kt
            @@ -1,3 +1,4 @@
             line 1
            +inserted 1
             line 2
            @@ -10,3 +11,4 @@
             line 10
            +inserted 2
             line 11
            """.trimIndent()

        val gitDiff = ContextItem.GitDiff(diff = diff)

        assertTrue(gitDiff.diff.contains("@@ -1,3 +1,4 @@"))
        assertTrue(gitDiff.diff.contains("@@ -10,3 +11,4 @@"))
    }

    // ============ Empty Diff Tests ============

    @Test
    fun `GitDiff with empty string is valid`() {
        val gitDiff = ContextItem.GitDiff(diff = "")

        assertEquals("", gitDiff.diff)
    }

    @Test
    fun `GitDiff with whitespace-only diff`() {
        val gitDiff = ContextItem.GitDiff(diff = "\n\n  ")

        assertTrue(gitDiff.diff.isNotEmpty())
    }

    // ============ Content Tests ============

    @Test
    fun `GitDiff preserves complete diff content`() {
        val diffContent =
            """
            --- a/src/App.kt
            +++ b/src/App.kt
            @@ -5,6 +5,7 @@ class Application {
                 fun start() {
            +        logger.info("Starting app")
                     val config = loadConfig()
                     initialize(config)
            """.trimIndent()

        val gitDiff = ContextItem.GitDiff(diff = diffContent)

        assertEquals(diffContent, gitDiff.diff)
    }

    @Test
    fun `GitDiff with binary file markers`() {
        val diff =
            """
            diff --git a/image.png b/image.png
            Binary files a/image.png and b/image.png differ
            """.trimIndent()

        val gitDiff = ContextItem.GitDiff(diff = diff)

        assertTrue(gitDiff.diff.contains("Binary"))
    }

    // ============ Multiline/Large Diff Tests ============

    @Test
    fun `GitDiff supports large diffs`() {
        val lines = mutableListOf<String>()
        lines.add("--- a/file.kt")
        lines.add("+++ b/file.kt")
        lines.add("@@ -1,100 +1,101 @@")
        for (i in 1..100) {
            lines.add(" line $i")
        }
        lines.add("+new line at end")

        val diff = lines.joinToString("\n")
        val gitDiff = ContextItem.GitDiff(diff = diff)

        assertTrue(gitDiff.diff.contains("line 100"))
        assertTrue(gitDiff.diff.contains("+new line at end"))
    }

    @Test
    fun `GitDiff with multiple files`() {
        val diff =
            """
            diff --git a/file1.kt b/file1.kt
            --- a/file1.kt
            +++ b/file1.kt
            @@ -1,2 +1,2 @@
            -old
            +new
            diff --git a/file2.kt b/file2.kt
            --- a/file2.kt
            +++ b/file2.kt
            @@ -1,2 +1,2 @@
            -removed
            +added
            """.trimIndent()

        val gitDiff = ContextItem.GitDiff(diff = diff)

        assertTrue(gitDiff.diff.contains("file1.kt"))
        assertTrue(gitDiff.diff.contains("file2.kt"))
    }

    // ============ Equality Tests ============

    @Test
    fun `GitDiffs with same content are equal`() {
        val diff1 = ContextItem.GitDiff(diff = "--- a/f.kt\n+++ b/f.kt")
        val diff2 = ContextItem.GitDiff(diff = "--- a/f.kt\n+++ b/f.kt")

        assertEquals(diff1, diff2)
    }

    @Test
    fun `GitDiffs with different content are not equal`() {
        val diff1 = ContextItem.GitDiff(diff = "--- a/f1.kt\n+++ b/f1.kt")
        val diff2 = ContextItem.GitDiff(diff = "--- a/f2.kt\n+++ b/f2.kt")

        assertTrue(diff1 != diff2)
    }

    // ============ Collection Tests ============

    @Test
    fun `GitDiff can be collected in lists`() {
        val diffs =
            listOf(
                ContextItem.GitDiff(diff = "--- a/f1\n+++ b/f1"),
                ContextItem.GitDiff(diff = "--- a/f2\n+++ b/f2"),
            )

        assertEquals(2, diffs.size)
    }
}
