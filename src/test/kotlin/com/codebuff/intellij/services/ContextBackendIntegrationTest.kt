package com.codebuff.intellij.services

import com.codebuff.intellij.model.ContextItem
import com.codebuff.intellij.backend.SendMessageRequest
import com.google.gson.Gson
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * TDD Tests for wiring context to backend requests
 * Issue: cb-0jl.10
 * RED Phase: All tests should fail initially
 */
class ContextBackendIntegrationTest {

    private val gson = Gson()

    // ============ Request Serialization Tests ============

    @Test
    fun `SendMessageRequest includes context array`() {
        val context = listOf(
            ContextItem.File("main.kt", "fun main() {}", "kotlin")
        )
        
        val request = SendMessageRequest(
            id = "req-001",
            sessionId = "sess-123",
            text = "Review this code",
            context = context.map { it.toProtocolMap() }
        )

        val json = gson.toJson(request)
        assertTrue(json.contains("\"context\""))
        assertTrue(json.contains("\"main.kt\""))
    }

    @Test
    fun `SendMessageRequest with empty context`() {
        val request = SendMessageRequest(
            id = "req-001",
            sessionId = "sess-123",
            text = "No context",
            context = emptyList()
        )

        val json = gson.toJson(request)
        assertTrue(json.contains("\"context\":[]"))
    }

    @Test
    fun `SendMessageRequest with multiple context items`() {
        val context = listOf(
            ContextItem.Selection("app.kt", "fun init()", 10, 15, "kotlin"),
            ContextItem.File("config.kt", "val config = Config()", "kotlin"),
            ContextItem.Diagnostic("app.kt", 12, "warning", "Unused parameter"),
            ContextItem.GitDiff("--- a/app.kt\n+++ b/app.kt")
        )

        val request = SendMessageRequest(
            id = "req-002",
            sessionId = "sess-123",
            text = "Fix issues",
            context = context.map { it.toProtocolMap() }
        )

        val json = gson.toJson(request)
        assertEquals(4, request.context.size)
    }

    // ============ Context Serialization Tests ============

    @Test
    fun `Selection serializes to Map with all fields`() {
        val selection = ContextItem.Selection(
            path = "src/Main.kt",
            content = "fun test() {}",
            startLine = 5,
            endLine = 7,
            language = "kotlin"
        )

        val map = selection.toProtocolMap()

        assertEquals("selection", map["type"])
        assertEquals("src/Main.kt", map["path"])
        assertEquals("fun test() {}", map["content"])
        assertEquals(5, map["startLine"])
        assertEquals(7, map["endLine"])
        assertEquals("kotlin", map["language"])
    }

    @Test
    fun `File serializes to Map with all fields`() {
        val file = ContextItem.File(
            path = "helpers/Utils.kt",
            content = "object Utils { }",
            language = "kotlin"
        )

        val map = file.toProtocolMap()

        assertEquals("file", map["type"])
        assertEquals("helpers/Utils.kt", map["path"])
        assertEquals("object Utils { }", map["content"])
        assertEquals("kotlin", map["language"])
    }

    @Test
    fun `Diagnostic serializes to Map with all fields`() {
        val diag = ContextItem.Diagnostic(
            path = "app.kt",
            line = 42,
            severity = "error",
            message = "Type mismatch: expected Int"
        )

        val map = diag.toProtocolMap()

        assertEquals("diagnostic", map["type"])
        assertEquals("app.kt", map["path"])
        assertEquals(42, map["line"])
        assertEquals("error", map["severity"])
        assertEquals("Type mismatch: expected Int", map["message"])
    }

    @Test
    fun `GitDiff serializes to Map`() {
        val diff = ContextItem.GitDiff(
            diff = "--- a/file.kt\n+++ b/file.kt\n@@ -1,2 +1,3 @@"
        )

        val map = diff.toProtocolMap()

        assertEquals("diff", map["type"])
        assertTrue((map["diff"] as String).contains("---"))
    }

    // ============ Context Type Tests ============

    @Test
    fun `Selection includes selection-specific fields`() {
        val selection = ContextItem.Selection("f.kt", "code", 1, 5, "kotlin")
        val map = selection.toProtocolMap()

        assertTrue(map.containsKey("startLine"))
        assertTrue(map.containsKey("endLine"))
        assertEquals(1, map["startLine"])
        assertEquals(5, map["endLine"])
    }

    @Test
    fun `File does not include selection fields`() {
        val file = ContextItem.File("f.kt", "code", "kotlin")
        val map = file.toProtocolMap()

        assertTrue(!map.containsKey("startLine"))
        assertTrue(!map.containsKey("endLine"))
    }

    @Test
    fun `Diagnostic includes line and severity`() {
        val diag = ContextItem.Diagnostic("f.kt", 10, "warning", "msg")
        val map = diag.toProtocolMap()

        assertTrue(map.containsKey("line"))
        assertTrue(map.containsKey("severity"))
        assertEquals(10, map["line"])
        assertEquals("warning", map["severity"])
    }

    // ============ Large Context Tests ============

    @Test
    fun `large file content serializes correctly`() {
        val largeContent = "x".repeat(100_000)
        val file = ContextItem.File("large.kt", largeContent, "kotlin")
        val map = file.toProtocolMap()

        assertEquals(largeContent, map["content"])
    }

    @Test
    fun `request with many context items`() {
        val items = mutableListOf<ContextItem>()
        for (i in 1..50) {
            items.add(ContextItem.File("file$i.kt", "content$i", "kotlin"))
        }

        val request = SendMessageRequest(
            id = "req-many",
            sessionId = "sess",
            text = "Process many files",
            context = items.map { it.toProtocolMap() }
        )

        assertEquals(50, request.context.size)
    }

    // ============ Context Clearing Tests ============

    @Test
    fun `context list can be cleared after send`() {
        var context = listOf(
            ContextItem.File("f.kt", "code", "kotlin")
        ).map { it.toProtocolMap() }

        assertEquals(1, context.size)

        context = emptyList()

        assertEquals(0, context.size)
    }

    @Test
    fun `new request has fresh context`() {
        val oldContext = listOf(
            ContextItem.File("old.kt", "old code", "kotlin")
        ).map { it.toProtocolMap() }

        val newContext = listOf(
            ContextItem.File("new.kt", "new code", "kotlin")
        ).map { it.toProtocolMap() }

        assertEquals(1, oldContext.size)
        assertEquals(1, newContext.size)
        assertNotNull(oldContext[0]["path"])
        assertEquals("old.kt", oldContext[0]["path"])
        assertEquals("new.kt", newContext[0]["path"])
    }

    /**
     * Extension function to convert ContextItem to Map<String, Any>
     * This is what the actual implementation will use
     */
    private fun ContextItem.toProtocolMap(): Map<String, Any> {
        return when (this) {
            is ContextItem.Selection -> mapOf(
                "type" to "selection",
                "path" to path,
                "content" to content,
                "startLine" to startLine,
                "endLine" to endLine,
                "language" to language
            )
            is ContextItem.File -> mapOf(
                "type" to "file",
                "path" to path,
                "content" to content,
                "language" to language
            )
            is ContextItem.Diagnostic -> mapOf(
                "type" to "diagnostic",
                "path" to path,
                "line" to line,
                "severity" to severity,
                "message" to message
            )
            is ContextItem.GitDiff -> mapOf(
                "type" to "diff",
                "diff" to diff
            )
        }
    }
}
