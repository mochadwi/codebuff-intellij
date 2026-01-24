package com.codebuff.intellij.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

@Service(Service.Level.PROJECT)
class SessionManager(private val project: Project) : Disposable {
    private val sessions = ConcurrentHashMap<String, Session>()

    fun createSession(): Session {
        val session =
            Session(
                id = UUID.randomUUID().toString(),
                projectPath = project.basePath ?: "",
            )
        sessions[session.id] = session
        return session
    }

    fun getSession(id: String): Session? = sessions[id]

    fun getAllSessions(): List<Session> = sessions.values.toList()

    fun closeSession(id: String) {
        sessions.remove(id)
    }

    override fun dispose() {
        sessions.clear()
    }
}

data class Session(
    val id: String,
    val projectPath: String,
    val messages: CopyOnWriteArrayList<Message> = CopyOnWriteArrayList(),
)

data class Message(
    val id: String,
    val role: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
)
