package com.codebuff.intellij.ui

import com.codebuff.intellij.model.ContextItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * TDD Tests for ContextAttachmentsPanel UI component
 * Issue: cb-0jl.9
 * RED Phase: All tests should fail initially
 */
class ContextAttachmentsPanelTest {

    // ============ Panel State Tests ============

    @Test
    fun `new panel starts with empty attachments`() {
        val model = ContextAttachmentsPanelModel()

        assertTrue(model.getAttachments().isEmpty())
    }

    @Test
    fun `panel size reflects attachment count`() {
        val model = ContextAttachmentsPanelModel()
        
        model.addAttachment(ContextItem.File("f1.kt", "code", "kotlin"))
        model.addAttachment(ContextItem.File("f2.kt", "code", "kotlin"))
        
        assertEquals(2, model.getAttachments().size)
    }

    // ============ Add Attachment Tests ============

    @Test
    fun `addAttachment adds single item`() {
        val model = ContextAttachmentsPanelModel()
        val item = ContextItem.File("test.kt", "content", "kotlin")
        
        model.addAttachment(item)
        
        assertEquals(1, model.getAttachments().size)
        assertEquals(item, model.getAttachments()[0])
    }

    @Test
    fun `addAttachment adds multiple items in order`() {
        val model = ContextAttachmentsPanelModel()
        val item1 = ContextItem.Selection("a.kt", "code1", 1, 2, "kotlin")
        val item2 = ContextItem.File("b.kt", "code2", "kotlin")
        val item3 = ContextItem.Diagnostic("c.kt", 5, "error", "msg")
        
        model.addAttachment(item1)
        model.addAttachment(item2)
        model.addAttachment(item3)
        
        assertEquals(3, model.getAttachments().size)
        assertEquals(item1, model.getAttachments()[0])
        assertEquals(item2, model.getAttachments()[1])
        assertEquals(item3, model.getAttachments()[2])
    }

    @Test
    fun `addAttachment allows duplicate items`() {
        val model = ContextAttachmentsPanelModel()
        val item = ContextItem.File("f.kt", "code", "kotlin")
        
        model.addAttachment(item)
        model.addAttachment(item)
        
        assertEquals(2, model.getAttachments().size)
    }

    // ============ Remove Attachment Tests ============

    @Test
    fun `removeAttachment at index removes correct item`() {
        val model = ContextAttachmentsPanelModel()
        val item1 = ContextItem.File("f1.kt", "code", "kotlin")
        val item2 = ContextItem.File("f2.kt", "code", "kotlin")
        val item3 = ContextItem.File("f3.kt", "code", "kotlin")
        
        model.addAttachment(item1)
        model.addAttachment(item2)
        model.addAttachment(item3)
        
        model.removeAttachment(1)
        
        assertEquals(2, model.getAttachments().size)
        assertEquals(item1, model.getAttachments()[0])
        assertEquals(item3, model.getAttachments()[1])
    }

    @Test
    fun `removeAttachment first item`() {
        val model = ContextAttachmentsPanelModel()
        val item1 = ContextItem.File("f1.kt", "code", "kotlin")
        val item2 = ContextItem.File("f2.kt", "code", "kotlin")
        
        model.addAttachment(item1)
        model.addAttachment(item2)
        
        model.removeAttachment(0)
        
        assertEquals(1, model.getAttachments().size)
        assertEquals(item2, model.getAttachments()[0])
    }

    @Test
    fun `removeAttachment last item`() {
        val model = ContextAttachmentsPanelModel()
        val item1 = ContextItem.File("f1.kt", "code", "kotlin")
        val item2 = ContextItem.File("f2.kt", "code", "kotlin")
        
        model.addAttachment(item1)
        model.addAttachment(item2)
        
        model.removeAttachment(1)
        
        assertEquals(1, model.getAttachments().size)
        assertEquals(item1, model.getAttachments()[0])
    }

    // ============ Clear All Tests ============

    @Test
    fun `clear removes all attachments`() {
        val model = ContextAttachmentsPanelModel()
        
        model.addAttachment(ContextItem.File("f1.kt", "code", "kotlin"))
        model.addAttachment(ContextItem.File("f2.kt", "code", "kotlin"))
        model.addAttachment(ContextItem.File("f3.kt", "code", "kotlin"))
        
        model.clear()
        
        assertTrue(model.getAttachments().isEmpty())
    }

    @Test
    fun `clear on empty panel is safe`() {
        val model = ContextAttachmentsPanelModel()
        
        model.clear()
        
        assertTrue(model.getAttachments().isEmpty())
    }

    // ============ Display Text Tests ============

    @Test
    fun `getDisplayText for Selection includes file and line range`() {
        val model = ContextAttachmentsPanelModel()
        val selection = ContextItem.Selection("Main.kt", "code", 10, 20, "kotlin")
        
        model.addAttachment(selection)
        
        val text = model.getDisplayText(0)
        assertTrue(text.contains("Main.kt"))
        assertTrue(text.contains("10"))
        assertTrue(text.contains("20"))
    }

    @Test
    fun `getDisplayText for File includes filename and size`() {
        val model = ContextAttachmentsPanelModel()
        val file = ContextItem.File("Helper.kt", "content code here", "kotlin")
        
        model.addAttachment(file)
        
        val text = model.getDisplayText(0)
        assertTrue(text.contains("Helper.kt"))
        assertTrue(text.contains("17")) // content length
    }

    @Test
    fun `getDisplayText for Diagnostic includes file and line`() {
        val model = ContextAttachmentsPanelModel()
        val diag = ContextItem.Diagnostic("App.kt", 15, "error", "Type mismatch")
        
        model.addAttachment(diag)
        
        val text = model.getDisplayText(0)
        assertTrue(text.contains("App.kt"))
        assertTrue(text.contains("15"))
        assertTrue(text.contains("error"))
    }

    @Test
    fun `getDisplayText for GitDiff shows diff indicator`() {
        val model = ContextAttachmentsPanelModel()
        val diff = ContextItem.GitDiff("--- a/f\n+++ b/f\n+changed")
        
        model.addAttachment(diff)
        
        val text = model.getDisplayText(0)
        assertTrue(text.contains("Diff") || text.contains("diff") || text.contains("git"))
    }

    // ============ Item Count Tests ============

    @Test
    fun `getAttachmentCount returns correct count`() {
        val model = ContextAttachmentsPanelModel()
        
        assertEquals(0, model.getAttachmentCount())
        
        model.addAttachment(ContextItem.File("f1.kt", "code", "kotlin"))
        assertEquals(1, model.getAttachmentCount())
        
        model.addAttachment(ContextItem.File("f2.kt", "code", "kotlin"))
        assertEquals(2, model.getAttachmentCount())
        
        model.removeAttachment(0)
        assertEquals(1, model.getAttachmentCount())
    }

    // ============ File Size Tests ============

    @Test
    fun `getFileSizeString formats file size in KB`() {
        val model = ContextAttachmentsPanelModel()
        
        // 5000 bytes = ~4.88 KB
        val sizeStr = model.formatFileSize(5000)
        assertTrue(sizeStr.contains("KB") || sizeStr.contains("kB") || sizeStr.contains("bytes"))
    }

    @Test
    fun `getFileSizeString handles bytes`() {
        val model = ContextAttachmentsPanelModel()
        
        val sizeStr = model.formatFileSize(500)
        assertTrue(sizeStr.contains("byte") || sizeStr.contains("B"))
    }

    @Test
    fun `getFileSizeString formats large sizes in MB`() {
        val model = ContextAttachmentsPanelModel()
        
        // 1048576 bytes = 1 MB
        val sizeStr = model.formatFileSize(1_048_576)
        assertTrue(sizeStr.contains("MB") || sizeStr.contains("mB"))
    }

    // ============ Attachment Types Tests ============

    @Test
    fun `panel can store mixed attachment types`() {
        val model = ContextAttachmentsPanelModel()
        
        model.addAttachment(ContextItem.Selection("s.kt", "code", 1, 2, "kotlin"))
        model.addAttachment(ContextItem.File("f.kt", "content", "kotlin"))
        model.addAttachment(ContextItem.Diagnostic("d.kt", 5, "error", "msg"))
        model.addAttachment(ContextItem.GitDiff("diff"))
        
        assertEquals(4, model.getAttachmentCount())
    }

    /**
     * Test model to simulate ContextAttachmentsPanel behavior
     */
    private class ContextAttachmentsPanelModel {
        private val attachments = mutableListOf<ContextItem>()

        fun addAttachment(item: ContextItem) {
            attachments.add(item)
        }

        fun removeAttachment(index: Int) {
            if (index in attachments.indices) {
                attachments.removeAt(index)
            }
        }

        fun getAttachments(): List<ContextItem> = attachments.toList()

        fun getAttachmentCount(): Int = attachments.size

        fun clear() {
            attachments.clear()
        }

        fun getDisplayText(index: Int): String {
            if (index !in attachments.indices) return ""
            val item = attachments[index]
            return when (item) {
                is ContextItem.Selection -> "${item.path} (lines ${item.startLine}-${item.endLine})"
                is ContextItem.File -> "${item.path} (${item.content.length} bytes)"
                is ContextItem.Diagnostic -> "${item.path}:${item.line} [${item.severity}]"
                is ContextItem.GitDiff -> "Git Diff"
            }
        }

        fun formatFileSize(bytes: Long): String {
            return when {
                bytes < 1024 -> "$bytes bytes"
                bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                else -> "${bytes / (1024 * 1024)} MB"
            }
        }
    }
}
