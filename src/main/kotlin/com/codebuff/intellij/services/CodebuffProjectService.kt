package com.codebuff.intellij.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class CodebuffProjectService(private val project: Project) : Disposable {
    val sessionManager: SessionManager by lazy {
        project.getService(SessionManager::class.java)
    }

    override fun dispose() {
        // Cleanup resources when project is closed
    }
}
