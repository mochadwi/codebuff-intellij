package com.codebuff.intellij.model

/**
 * Sealed class representing different types of context items that can be attached to messages
 */
sealed class ContextItem {
    /**
     * Selected text from editor with location information
     */
    data class Selection(
        val path: String,           // File path relative to project
        val content: String,        // Selected text
        val startLine: Int,         // Start line number (1-indexed)
        val endLine: Int,           // End line number (1-indexed)
        val language: String        // Programming language (e.g., "kotlin", "java")
    ) : ContextItem()

    /**
     * Entire file content
     */
    data class File(
        val path: String,           // File path relative to project
        val content: String,        // Full file content
        val language: String        // Programming language
    ) : ContextItem()

    /**
     * Diagnostic/inspection issue from IntelliJ
     */
    data class Diagnostic(
        val path: String,           // File path relative to project
        val line: Int,              // Line number (1-indexed)
        val severity: String,       // "error", "warning", "info"
        val message: String         // Diagnostic message
    ) : ContextItem()

    /**
     * Git diff for uncommitted changes
     */
    data class GitDiff(
        val diff: String            // Unified diff format
    ) : ContextItem()
}
<<<<<<< HEAD

/**
 * Convert ContextItem to Map<String, Any> for backend protocol
 */
fun ContextItem.toProtocolMap(): Map<String, Any> {
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
=======
>>>>>>> cb00d6f ([cb-0jl.6] Write tests for ContextCollector service (RED phase))
