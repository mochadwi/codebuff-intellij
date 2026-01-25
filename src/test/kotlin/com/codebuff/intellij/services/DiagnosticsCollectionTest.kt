package com.codebuff.intellij.services

import com.codebuff.intellij.model.ContextItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * TDD Tests for diagnostics collection
 * Issue: cb-0jl.7
 * RED Phase: All tests should fail initially
 */
class DiagnosticsCollectionTest {

    // ============ Diagnostic Severity Tests ============

    @Test
    fun `Diagnostic with error severity`() {
        val diagnostic = ContextItem.Diagnostic(
            path = "src/Main.kt",
            line = 5,
            severity = "error",
            message = "Type mismatch: expected Int, got String"
        )

        assertEquals("error", diagnostic.severity)
        assertEquals("src/Main.kt", diagnostic.path)
        assertEquals(5, diagnostic.line)
    }

    @Test
    fun `Diagnostic with warning severity`() {
        val diagnostic = ContextItem.Diagnostic(
            path = "src/Helper.kt",
            line = 10,
            severity = "warning",
            message = "Variable 'unused' is never used"
        )

        assertEquals("warning", diagnostic.severity)
        assertTrue(diagnostic.message.contains("unused"))
    }

    @Test
    fun `Diagnostic with info severity`() {
        val diagnostic = ContextItem.Diagnostic(
            path = "src/Utils.kt",
            line = 15,
            severity = "info",
            message = "Consider using named parameters"
        )

        assertEquals("info", diagnostic.severity)
    }

    // ============ Line Number Tests ============

    @Test
    fun `Diagnostic tracks exact line number`() {
        val diag = ContextItem.Diagnostic(
            path = "file.kt",
            line = 42,
            severity = "error",
            message = "Syntax error"
        )

        assertEquals(42, diag.line)
    }

    @Test
    fun `Diagnostic with line 1`() {
        val diag = ContextItem.Diagnostic(
            path = "file.kt",
            line = 1,
            severity = "error",
            message = "First line error"
        )

        assertEquals(1, diag.line)
    }

    @Test
    fun `Diagnostic with large line number`() {
        val diag = ContextItem.Diagnostic(
            path = "file.kt",
            line = 10000,
            severity = "error",
            message = "Error at end of file"
        )

        assertEquals(10000, diag.line)
    }

    // ============ Message Tests ============

    @Test
    fun `Diagnostic stores full error message`() {
        val fullMessage = "Type mismatch: expected List<String>, got List<Int>"
        val diag = ContextItem.Diagnostic(
            path = "file.kt",
            line = 5,
            severity = "error",
            message = fullMessage
        )

        assertEquals(fullMessage, diag.message)
    }

    @Test
    fun `Diagnostic with multiline message support`() {
        val message = "Error in function foo:\n  Expected: Int\n  Got: String"
        val diag = ContextItem.Diagnostic(
            path = "file.kt",
            line = 5,
            severity = "error",
            message = message
        )

        assertTrue(diag.message.contains("\n"))
        assertTrue(diag.message.contains("Expected"))
    }

    @Test
    fun `Diagnostic with empty message`() {
        val diag = ContextItem.Diagnostic(
            path = "file.kt",
            line = 5,
            severity = "error",
            message = ""
        )

        assertEquals("", diag.message)
    }

    // ============ Path Tests ============

    @Test
    fun `Diagnostic with relative path`() {
        val diag = ContextItem.Diagnostic(
            path = "src/main/kotlin/App.kt",
            line = 1,
            severity = "error",
            message = "Error"
        )

        assertEquals("src/main/kotlin/App.kt", diag.path)
    }

    @Test
    fun `Diagnostic with simple filename`() {
        val diag = ContextItem.Diagnostic(
            path = "Main.kt",
            line = 1,
            severity = "error",
            message = "Error"
        )

        assertEquals("Main.kt", diag.path)
    }

    // ============ Equality Tests ============

    @Test
    fun `Diagnostics with same fields are equal`() {
        val diag1 = ContextItem.Diagnostic("file.kt", 5, "error", "msg")
        val diag2 = ContextItem.Diagnostic("file.kt", 5, "error", "msg")

        assertEquals(diag1, diag2)
    }

    @Test
    fun `Diagnostics with different severity are not equal`() {
        val diag1 = ContextItem.Diagnostic("file.kt", 5, "error", "msg")
        val diag2 = ContextItem.Diagnostic("file.kt", 5, "warning", "msg")

        assertTrue(diag1 != diag2)
    }

    @Test
    fun `Diagnostics with different line are not equal`() {
        val diag1 = ContextItem.Diagnostic("file.kt", 5, "error", "msg")
        val diag2 = ContextItem.Diagnostic("file.kt", 6, "error", "msg")

        assertTrue(diag1 != diag2)
    }

    // ============ Collection Tests ============

    @Test
    fun `Multiple diagnostics can be collected in list`() {
        val diagnostics = listOf(
            ContextItem.Diagnostic("file.kt", 1, "error", "Error 1"),
            ContextItem.Diagnostic("file.kt", 5, "warning", "Warning 1"),
            ContextItem.Diagnostic("file.kt", 10, "info", "Info 1")
        )

        assertEquals(3, diagnostics.size)
        assertEquals("error", diagnostics[0].severity)
        assertEquals("warning", diagnostics[1].severity)
        assertEquals("info", diagnostics[2].severity)
    }

    @Test
    fun `Empty diagnostics list is valid`() {
        val diagnostics: List<ContextItem.Diagnostic> = emptyList()

        assertEquals(0, diagnostics.size)
    }
}
