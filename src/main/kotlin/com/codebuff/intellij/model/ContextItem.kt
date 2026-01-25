package com.codebuff.intellij.model

/**
 * Sealed class representing different types of context items that can be attached to messages
 */
sealed class ContextItem {
    /**
     * Selected text from editor with location information
     *
     * @property path File path relative to project
     * @property content Selected text
     * @property startLine Start line number (1-indexed)
     * @property endLine End line number (1-indexed)
     * @property language Programming language (e.g., "kotlin", "java")
     */
    data class Selection(
        val path: String,
        val content: String,
        val startLine: Int,
        val endLine: Int,
        val language: String,
    ) : ContextItem()

    /**
     * Entire file content
     *
     * @property path File path relative to project
     * @property content Full file content
     * @property language Programming language
     */
    data class File(
        val path: String,
        val content: String,
        val language: String,
    ) : ContextItem()

    /**
     * Diagnostic/inspection issue from IntelliJ
     *
     * @property path File path relative to project
     * @property line Line number (1-indexed)
     * @property severity "error", "warning", "info"
     * @property message Diagnostic message
     */
    data class Diagnostic(
        val path: String,
        val line: Int,
        val severity: String,
        val message: String,
    ) : ContextItem()

    /**
     * Git diff for uncommitted changes
     *
     * @property diff Unified diff format
     */
    data class GitDiff(
        val diff: String,
    ) : ContextItem()
}

/**
 * Convert ContextItem to Map<String, Any> for backend protocol
 */
fun ContextItem.toProtocolMap(): Map<String, Any> {
    return when (this) {
        is ContextItem.Selection ->
            mapOf(
                "type" to "selection",
                "path" to path,
                "content" to content,
                "startLine" to startLine,
                "endLine" to endLine,
                "language" to language,
            )
        is ContextItem.File ->
            mapOf(
                "type" to "file",
                "path" to path,
                "content" to content,
                "language" to language,
            )
        is ContextItem.Diagnostic ->
            mapOf(
                "type" to "diagnostic",
                "path" to path,
                "line" to line,
                "severity" to severity,
                "message" to message,
            )
        is ContextItem.GitDiff ->
            mapOf(
                "type" to "diff",
                "diff" to diff,
            )
    }
}
