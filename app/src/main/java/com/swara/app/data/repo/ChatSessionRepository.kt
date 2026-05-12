package com.swara.app.data.repo

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.swara.app.data.model.ChatMessage
import com.swara.app.data.model.ChatSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatSessionRepository(
    context: Context,
    private val gson: Gson = Gson()
) {
    private val preferences = context.applicationContext
        .getSharedPreferences("swara_chat_sessions", Context.MODE_PRIVATE)
    private val sessionListType = object : TypeToken<List<ChatSession>>() {}.type
    private val messageMapType = object : TypeToken<Map<String, List<ChatMessage>>>() {}.type

    private val _sessions = MutableStateFlow(loadSessions())
    val sessions: StateFlow<List<ChatSession>> = _sessions.asStateFlow()

    private val _messagesBySession = MutableStateFlow(loadMessages())

    fun messagesFor(sessionId: String): List<ChatMessage> {
        return _messagesBySession.value[sessionId].orEmpty()
    }

    fun upsertSession(session: ChatSession) {
        val next = (_sessions.value.filterNot { it.id == session.id } + session)
            .sortedByDescending { it.updatedAt }
        _sessions.value = next
        persist()
    }

    fun saveMessages(sessionId: String, messages: List<ChatMessage>) {
        _messagesBySession.value = _messagesBySession.value + (sessionId to messages)
        persist()
    }

    fun deleteSession(sessionId: String) {
        _sessions.value = _sessions.value.filterNot { it.id == sessionId }
        _messagesBySession.value = _messagesBySession.value - sessionId
        persist()
    }

    private fun loadSessions(): List<ChatSession> {
        val json = preferences.getString(KEY_SESSIONS, null) ?: return emptyList()
        return runCatching {
            gson.fromJson<List<ChatSession>>(json, sessionListType).orEmpty()
        }.getOrDefault(emptyList())
    }

    private fun loadMessages(): Map<String, List<ChatMessage>> {
        val json = preferences.getString(KEY_MESSAGES, null) ?: return emptyMap()
        return runCatching {
            gson.fromJson<Map<String, List<ChatMessage>>>(json, messageMapType).orEmpty()
        }.getOrDefault(emptyMap())
    }

    private fun persist() {
        preferences.edit()
            .putString(KEY_SESSIONS, gson.toJson(_sessions.value))
            .putString(KEY_MESSAGES, gson.toJson(_messagesBySession.value))
            .apply()
    }

    companion object {
        private const val KEY_SESSIONS = "sessions"
        private const val KEY_MESSAGES = "messages"
    }
}
