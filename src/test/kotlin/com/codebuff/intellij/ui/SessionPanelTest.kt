package com.codebuff.intellij.ui

import org.junit.jupiter.api.Test

/**
 * Tests for SessionPanel UI component.
 *
 * Issue: cb-u20.7
 */
class SessionPanelTest {
    @Test
    fun `SessionPanel has refresh method`() {
        // Verify SessionPanel class has refresh method
        val methods = SessionPanel::class.java.methods.map { it.name }
        assert(methods.contains("refresh"))
    }

    @Test
    fun `SessionPanel has selectSession method`() {
        // Verify SessionPanel class has selectSession method
        val methods = SessionPanel::class.java.methods.map { it.name }
        assert(methods.contains("selectSession"))
    }

    @Test
    fun `SessionPanel has onNewSession method`() {
        // Verify SessionPanel class has onNewSession method
        val methods = SessionPanel::class.java.methods.map { it.name }
        assert(methods.contains("onNewSession"))
    }

    @Test
    fun `SessionPanel has renameSession method`() {
        // Verify SessionPanel class has renameSession method
        val methods = SessionPanel::class.java.methods.map { it.name }
        assert(methods.contains("renameSession"))
    }

    @Test
    fun `SessionPanel has deleteSession method`() {
        // Verify SessionPanel class has deleteSession method
        val methods = SessionPanel::class.java.methods.map { it.name }
        assert(methods.contains("deleteSession"))
    }

    @Test
    fun `SessionPanel methods are public`() {
        // Verify methods have correct visibility
        val refreshMethod = SessionPanel::class.java.getMethod("refresh")
        assert(java.lang.reflect.Modifier.isPublic(refreshMethod.modifiers))
    }
}
