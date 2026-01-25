package com.codebuff.intellij.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.State
import com.intellij.openapi.options.Configurable
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
class CodebuffSettingsTest {
    
    @Test
    fun `settings service is registered`() {
        val settings = CodebuffSettings()
        assertNotNull(settings)
    }
    
    @Test
    fun `default CLI path is codebuff`() {
        val settings = CodebuffSettings()
        kotlin.test.assertEquals("codebuff", settings.state.cliPath)
    }
    
    @Test
    fun `can update CLI path`() {
        val settings = CodebuffSettings()
        settings.state.cliPath = "/custom/path/codebuff"
        
        kotlin.test.assertEquals("/custom/path/codebuff", settings.state.cliPath)
    }
    
    @Test
    fun `settings persist via state`() {
        val settings = CodebuffSettings()
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
        val settings = CodebuffSettings()
        assertFalse(settings.state.autoApplyChanges)
    }
    
    @Test
    fun `show tool calls default is true`() {
        val settings = CodebuffSettings()
        assertTrue(settings.state.showToolCalls)
    }
    
    @Test
    fun `trace enabled default is false`() {
        val settings = CodebuffSettings()
        assertFalse(settings.state.traceEnabled)
    }
}

class CodebuffSettingsConfigurableTest {
    
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
