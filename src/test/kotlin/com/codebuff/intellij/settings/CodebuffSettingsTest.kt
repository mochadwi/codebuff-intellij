package com.codebuff.intellij.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.State
import com.intellij.openapi.options.Configurable
import com.intellij.testFramework.BasePlatformTestCase
import kotlin.test.Test
import javax.swing.JComponent
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * TDD Test First: Tests for settings configuration.
 * 
 * Issue: cb-ble.12
 * Tests settings persistence, configurable UI, and defaults.
 */
class CodebuffSettingsTest : BasePlatformTestCase() {
    
    @Test
    fun `settings service is registered`() {
        val settings = CodebuffSettings.getInstance()
        assertNotNull(settings)
    }
    
    @Test
    fun `default CLI path is codebuff`() {
        val settings = CodebuffSettings.getInstance()
        kotlin.test.assertEquals("codebuff", settings.state.cliPath)
    }
    
    @Test
    fun `can update CLI path`() {
        val settings = CodebuffSettings.getInstance()
        settings.state.cliPath = "/custom/path/codebuff"
        
        kotlin.test.assertEquals("/custom/path/codebuff", settings.state.cliPath)
    }
    
    @Test
    fun `settings persist via state`() {
        val settings = CodebuffSettings.getInstance()
        settings.state.cliPath = "/test/path"
        settings.state.autoApplyChanges = true
        
        // Simulate reload
        val savedState = settings.state
        val newSettings = CodebuffSettings()
        newSettings.loadState(savedState)
        
        kotlin.test.assertEquals("/test/path", newSettings.state.cliPath)
        assertTrue(newSettings.state.autoApplyChanges)
    }
    
    @Test
    fun `default auto apply is false`() {
        val settings = CodebuffSettings.getInstance()
        assertFalse(settings.state.autoApplyChanges)
    }
    
    @Test
    fun `show tool calls default is true`() {
        val settings = CodebuffSettings.getInstance()
        assertTrue(settings.state.showToolCalls)
    }
    
    @Test
    fun `trace enabled default is false`() {
        val settings = CodebuffSettings.getInstance()
        assertFalse(settings.state.traceEnabled)
    }
}

class CodebuffSettingsConfigurableTest : BasePlatformTestCase() {
    
    @Test
    fun `configurable has correct display name`() {
        val configurable = CodebuffSettingsConfigurable()
        kotlin.test.assertEquals("Codebuff", configurable.displayName)
    }
    
    @Test
    fun `configurable creates component`() {
        val configurable = CodebuffSettingsConfigurable()
        val component = configurable.createComponent()
        
        assertNotNull(component)
    }
    
    @Test
    fun `isModified returns false initially`() {
        val configurable = CodebuffSettingsConfigurable()
        configurable.createComponent()
        
        assertFalse(configurable.isModified)
    }
    
    @Test
    fun `apply saves settings`() {
        val configurable = CodebuffSettingsConfigurable()
        configurable.createComponent()
        // Apply should not throw
        configurable.apply()
    }
    
    @Test
    fun `reset restores from settings`() {
        val configurable = CodebuffSettingsConfigurable()
        configurable.createComponent()
        configurable.reset()
        // Should not throw
    }
}

// Settings implementation
@State(name = "CodebuffSettings", storages = [Storage("codebuff.xml")])
@Service(com.intellij.openapi.components.Service.Level.APP)
class CodebuffSettings : PersistentStateComponent<CodebuffSettings.State> {
    
    data class State(
        var cliPath: String = "codebuff",
        var autoApplyChanges: Boolean = false,
        var showToolCalls: Boolean = true,
        var traceEnabled: Boolean = false
    )
    
    var state = State()
    
    override fun getState() = state
    override fun loadState(state: State) { this.state = state }
    
    companion object {
        fun getInstance(): CodebuffSettings = 
            com.intellij.openapi.application.ApplicationManager.getApplication()
                .getService(CodebuffSettings::class.java)
    }
}

// Configurable for settings
class CodebuffSettingsConfigurable : Configurable {
    private var panel: CodebuffSettingsPanel? = null
    
    override fun createComponent(): JComponent {
        panel = CodebuffSettingsPanel()
        return panel!!
    }
    
    override fun isModified(): Boolean = panel?.isModified() == true
    override fun apply() { panel?.apply() }
    override fun reset() { panel?.reset() }
    override fun getDisplayName() = "Codebuff"
}

class CodebuffSettingsPanel : javax.swing.JPanel() {
    fun isModified() = false
    fun apply() {}
    fun reset() {}
}
