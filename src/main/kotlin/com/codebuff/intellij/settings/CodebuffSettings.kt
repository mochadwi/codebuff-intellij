package com.codebuff.intellij.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.options.Configurable
import java.awt.FlowLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * Settings management for Codebuff plugin.
 * Persists CLI path, UI preferences, and debug options.
 *
 * Issue: cb-ble.6
 */
@State(name = "CodebuffSettings", storages = [Storage("codebuff.xml")])
@Service(Service.Level.APP)
class CodebuffSettings : PersistentStateComponent<CodebuffSettings.State> {
    data class State(
        var cliPath: String = "codebuff",
        var autoApplyChanges: Boolean = false,
        var showToolCalls: Boolean = true,
        var traceEnabled: Boolean = false,
    )

    private var internalState = State()

    @get:JvmName("getStateData")
    @set:JvmName("setStateData")
    var state: State
        get() = internalState
        set(value) {
            internalState = value
        }

    override fun getState() = internalState

    override fun loadState(state: State) {
        internalState = state
    }

    companion object {
        fun getInstance(): CodebuffSettings =
            ApplicationManager.getApplication()
                .getService(CodebuffSettings::class.java)
    }
}

// Settings Configurable UI
class CodebuffSettingsConfigurable : Configurable {
    private var panel: CodebuffSettingsPanel? = null

    override fun createComponent(): JComponent {
        panel = CodebuffSettingsPanel()
        return panel!!
    }

    override fun isModified(): Boolean = panel?.isModified() == true

    override fun apply() {
        panel?.apply()
    }

    override fun reset() {
        panel?.reset()
    }

    override fun getDisplayName() = "Codebuff"
}

class CodebuffSettingsPanel(private val settings: CodebuffSettings = getSettings()) : JPanel() {
    private val cliPathField = JTextField(settings.state.cliPath, 30)

    init {
        layout = FlowLayout(FlowLayout.LEFT)
        add(JLabel("CLI Path:"))
        add(cliPathField)
    }

    fun isModified() = cliPathField.text != settings.state.cliPath

    fun apply() {
        settings.state.cliPath = cliPathField.text
    }

    fun reset() {
        cliPathField.text = settings.state.cliPath
    }

    companion object {
        private fun getSettings(): CodebuffSettings {
            return try {
                CodebuffSettings.getInstance()
            } catch (e: Exception) {
                // Fallback for tests without ApplicationManager
                CodebuffSettings()
            }
        }
    }
}
