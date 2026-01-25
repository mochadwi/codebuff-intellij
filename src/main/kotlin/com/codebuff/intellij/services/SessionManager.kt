package com.codebuff.intellij.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Manages Codebuff sessions with persistence across IDE restarts.
 *
 * Issue: cb-u20.1
 */
@Service(Service.Level.PROJECT)
@State(
    name = "CodebuffSessionState",
    storages = [Storage("codebuff-sessions.xml")],
)
class SessionManager(private val project: Project) :
    PersistentStateComponent<SessionManager.State>,
    Disposable {
    private var internalState = State()
    private val sessions = ConcurrentHashMap<String, SessionInfo>()

    init {
        // Initialize internal state
        internalState.sessions = sessions
    }

    data class State(
        var sessions: MutableMap<String, SessionInfo> = mutableMapOf(),
        var activeSessionId: String? = null,
    ) {
        // For XML serialization
        var sessionsList: MutableList<SessionInfo> = mutableListOf()

        fun toList(): MutableList<SessionInfo> = sessions.values.toMutableList()

        fun fromList(list: List<SessionInfo>) {
            sessions.clear()
            list.forEach { sessions[it.id] = it }
        }
    }

    data class SessionInfo(
        var id: String = "",
        var title: String = "",
        var createdAt: Long = 0L,
        var lastActivityAt: Long = 0L,
    )

    fun createSession(title: String = "Session ${UUID.randomUUID().toString().take(8)}"): SessionInfo {
        val now = System.currentTimeMillis()
        val sessionInfo =
            SessionInfo(
                id = UUID.randomUUID().toString(),
                title = title,
                createdAt = now,
                lastActivityAt = now,
            )
        sessions[sessionInfo.id] = sessionInfo
        return sessionInfo
    }

    fun getSession(id: String): SessionInfo? = sessions[id]

    fun getSessions(): List<SessionInfo> = sessions.values.toList()

    fun closeSession(id: String) {
        sessions.remove(id)
        if (internalState.activeSessionId == id) {
            internalState.activeSessionId = null
        }
    }

    fun setActiveSession(session: SessionInfo) {
        internalState.activeSessionId = session.id
    }

    val activeSession: SessionInfo?
        get() = internalState.activeSessionId?.let { sessions[it] }

    override fun getState(): State {
        internalState.sessions = sessions
        internalState.sessionsList = sessions.values.toMutableList()
        return internalState
    }

    override fun loadState(state: State) {
        internalState = state
        sessions.clear()
        state.sessions.forEach { (id, info) -> sessions[id] = info }
        // Also handle legacy sessionsList format
        state.sessionsList.forEach { info -> sessions[info.id] = info }
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
