package com.codebuff.intellij.diff

import com.codebuff.intellij.backend.FileDiff
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * TDD Test First: Tests for file conflict detection.
 *
 * Issue: cb-blv.9
 */
class ConflictDetectorTest {
    @Test
    fun `snapshot stores file path`() {
        val detector = ConflictDetector()

        detector.captureSnapshot("test.kt")

        assertTrue(detector.hasSnapshot("test.kt"))
    }

    @Test
    fun `hasConflict returns false for new file`() {
        val detector = ConflictDetector()
        val diff = FileDiff("new.kt", "", "content", FileDiff.Operation.CREATE)

        val result = detector.hasConflict(diff)

        assertFalse(result)
    }

    @Test
    fun `hasConflict returns false when file not modified after snapshot`() {
        val detector = ConflictDetector()
        detector.captureSnapshot("test.kt")
        val diff = FileDiff("test.kt", "content", "new", FileDiff.Operation.MODIFY)

        val result = detector.hasConflict(diff)

        assertFalse(result)
    }

    @Test
    fun `hasConflict returns false when no snapshot`() {
        val detector = ConflictDetector()
        val diff = FileDiff("test.kt", "content", "new", FileDiff.Operation.MODIFY)

        val result = detector.hasConflict(diff)

        assertFalse(result)
    }

    @Test
    fun `clearSnapshots clears all state`() {
        val detector = ConflictDetector()
        detector.captureSnapshot("a.kt")
        detector.captureSnapshot("b.kt")

        detector.clearSnapshots()

        assertFalse(detector.hasSnapshot("a.kt"))
        assertFalse(detector.hasSnapshot("b.kt"))
    }

    @Test
    fun `snapshot can be updated`() {
        val detector = ConflictDetector()
        detector.captureSnapshot("test.kt")

        assertTrue(detector.hasSnapshot("test.kt"))

        // Capture again
        detector.captureSnapshot("test.kt")

        assertTrue(detector.hasSnapshot("test.kt"))
    }

    @Test
    fun `multiple snapshots can be stored`() {
        val detector = ConflictDetector()
        detector.captureSnapshot("a.kt")
        detector.captureSnapshot("b.kt")
        detector.captureSnapshot("c.kt")

        assertTrue(detector.hasSnapshot("a.kt"))
        assertTrue(detector.hasSnapshot("b.kt"))
        assertTrue(detector.hasSnapshot("c.kt"))
    }
}
