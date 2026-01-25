package com.codebuff.intellij

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class EditorActionsTest {
    @Test
    fun `plugin xml declares OpenToolWindow action`() {
        val content = TestUtils.loadPluginXml()
        assertTrue(
            content.contains("Codebuff.OpenToolWindow"),
            "plugin.xml should declare OpenToolWindow action",
        )
    }

    @Test
    fun `plugin xml declares SendSelection action`() {
        val content = TestUtils.loadPluginXml()
        assertTrue(
            content.contains("Codebuff.SendSelection"),
            "plugin.xml should declare SendSelection action",
        )
    }

    @Test
    fun `OpenToolWindow action has keyboard shortcut`() {
        val content = TestUtils.loadPluginXml()
        assertTrue(
            content.contains("keyboard-shortcut") && content.contains("Codebuff.OpenToolWindow"),
            "OpenToolWindow should have keyboard shortcut",
        )
    }

    @Test
    fun `SendSelection action is added to EditorPopupMenu`() {
        val content = TestUtils.loadPluginXml()
        assertTrue(
            content.contains("EditorPopupMenu"),
            "SendSelection should be added to EditorPopupMenu",
        )
    }

    @Test
    fun `OpenToolWindowAction class exists`() {
        val actionFile = File("src/main/kotlin/com/codebuff/intellij/actions/OpenToolWindowAction.kt")
        assertTrue(actionFile.exists(), "OpenToolWindowAction.kt should exist")
    }

    @Test
    fun `SendSelectionAction class exists`() {
        val actionFile = File("src/main/kotlin/com/codebuff/intellij/actions/SendSelectionAction.kt")
        assertTrue(actionFile.exists(), "SendSelectionAction.kt should exist")
    }

    @Test
    fun `OpenToolWindowAction extends AnAction`() {
        val actionFile = File("src/main/kotlin/com/codebuff/intellij/actions/OpenToolWindowAction.kt")
        assertTrue(actionFile.exists(), "OpenToolWindowAction.kt should exist")

        val content = actionFile.readText()
        assertTrue(
            content.contains("AnAction") && content.contains("actionPerformed"),
            "OpenToolWindowAction should extend AnAction with actionPerformed",
        )
    }

    @Test
    fun `SendSelectionAction extends AnAction`() {
        val actionFile = File("src/main/kotlin/com/codebuff/intellij/actions/SendSelectionAction.kt")
        assertTrue(actionFile.exists(), "SendSelectionAction.kt should exist")

        val content = actionFile.readText()
        assertTrue(
            content.contains("AnAction") && content.contains("actionPerformed"),
            "SendSelectionAction should extend AnAction with actionPerformed",
        )
    }
}
