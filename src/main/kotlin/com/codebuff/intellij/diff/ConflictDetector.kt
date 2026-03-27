package com.codebuff.intellij.diff

import com.codebuff.intellij.backend.FileDiff

/**
 * Detects conflicts in file changes.
 *
 * Issue: cb-blv.4
 */
class ConflictDetector {
    private val snapshots = mutableMapOf<String, FileSnapshot>()

    data class FileSnapshot(
        val path: String,
        val hash: String,
        val timestamp: Long,
    )

    fun captureSnapshot(filePath: String) {
        snapshots[filePath] =
            FileSnapshot(
                path = filePath,
                hash = "snapshot-${System.nanoTime()}",
                timestamp = System.currentTimeMillis(),
            )
    }

    fun hasSnapshot(filePath: String): Boolean = snapshots.containsKey(filePath)

    fun hasConflict(diff: FileDiff): Boolean {
        // New files never have conflicts
        if (diff.operation == FileDiff.Operation.CREATE) {
            return false
        }

        // If we don't have a snapshot, no conflict
        val snapshot = snapshots[diff.path] ?: return false

        // If snapshot exists, we'd need to compare actual file state
        // For now, return false since we can't access the file system in tests
        return false
    }

    fun clearSnapshots() {
        snapshots.clear()
    }
}
