package com.codebuff.intellij.backend

/**
 * Represents a single file diff from the backend.
 *
 * Issue: cb-blv.1
 */
data class FileDiff(
    val path: String,
    val before: String,
    val after: String,
    val operation: Operation,
) {
    enum class Operation {
        CREATE, // before is empty, after has content
        MODIFY, // both before and after have content
        DELETE, // before has content, after is empty
    }
}

/**
 * Parses DiffEvent from backend into FileDiff models.
 */
object DiffParser {
    fun parseFromEvent(event: DiffEvent): List<FileDiff> {
        return event.files.map { fileMap ->
            val path = fileMap["path"] as String
            val before = (fileMap["before"] as? String) ?: ""
            val after = (fileMap["after"] as? String) ?: ""

            val operation =
                when {
                    before.isEmpty() && after.isNotEmpty() -> FileDiff.Operation.CREATE
                    before.isNotEmpty() && after.isEmpty() -> FileDiff.Operation.DELETE
                    else -> FileDiff.Operation.MODIFY
                }

            FileDiff(
                path = path,
                before = before,
                after = after,
                operation = operation,
            )
        }
    }
}
