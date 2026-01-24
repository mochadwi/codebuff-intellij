package com.codebuff.intellij.settings

import com.intellij.openapi.options.Configurable
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class CodebuffSettingsConfigurable : Configurable {

    private var settingsPanel: JPanel? = null

    override fun getDisplayName(): String = "Codebuff"

    override fun createComponent(): JComponent {
        settingsPanel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(8)
            add(JLabel("Codebuff settings will be available in a future release."), BorderLayout.NORTH)
        }
        return settingsPanel!!
    }

    override fun isModified(): Boolean = false

    override fun apply() {
        // No settings to apply yet
    }

    override fun reset() {
        // No settings to reset yet
    }

    override fun disposeUIResources() {
        settingsPanel = null
    }
}
