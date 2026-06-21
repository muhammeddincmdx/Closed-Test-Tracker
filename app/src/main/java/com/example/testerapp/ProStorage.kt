package com.mdstudio.closedtesttracker

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class PostTemplate(val id: Long, val name: String, val body: String)

data class TesterNote(
    val id: Long,
    val username: String,
    val profileUrl: String,
    val note: String,
    val reliable: Boolean
)

class ProStorage(context: Context) {
    companion object {
        const val MAX_TEMPLATES = 3
        private const val PREFS_NAME = "pro_content"
        private const val KEY_TEMPLATES = "templates"
        private const val KEY_TESTERS = "testers"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun templates(): List<PostTemplate> = runCatching {
        val array = JSONArray(prefs.getString(KEY_TEMPLATES, "[]"))
        List(array.length()) { index ->
            val item = array.getJSONObject(index)
            PostTemplate(item.getLong("id"), item.getString("name"), item.getString("body"))
        }
    }.getOrDefault(emptyList())

    fun saveTemplate(name: String, body: String, editingId: Long? = null): Boolean {
        val current = templates().toMutableList()
        if (editingId == null && current.size >= MAX_TEMPLATES) return false
        val item = PostTemplate(editingId ?: System.currentTimeMillis(), name.trim(), body.trim())
        val existingIndex = current.indexOfFirst { it.id == editingId }
        if (existingIndex >= 0) current[existingIndex] = item else current += item
        writeTemplates(current)
        return true
    }

    fun deleteTemplate(id: Long) = writeTemplates(templates().filterNot { it.id == id })

    fun testers(): List<TesterNote> = runCatching {
        val array = JSONArray(prefs.getString(KEY_TESTERS, "[]"))
        List(array.length()) { index ->
            val item = array.getJSONObject(index)
            TesterNote(
                id = item.getLong("id"),
                username = item.getString("username"),
                profileUrl = item.optString("profileUrl"),
                note = item.optString("note"),
                reliable = item.optBoolean("reliable")
            )
        }
    }.getOrDefault(emptyList())

    fun saveTester(username: String, profileUrl: String, note: String, reliable: Boolean, editingId: Long? = null) {
        val current = testers().toMutableList()
        val item = TesterNote(editingId ?: System.currentTimeMillis(), username.trim(), profileUrl.trim(), note.trim(), reliable)
        val existingIndex = current.indexOfFirst { it.id == editingId }
        if (existingIndex >= 0) current[existingIndex] = item else current += item
        writeTesters(current)
    }

    fun deleteTester(id: Long) = writeTesters(testers().filterNot { it.id == id })

    private fun writeTemplates(items: List<PostTemplate>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(JSONObject().put("id", item.id).put("name", item.name).put("body", item.body))
        }
        prefs.edit().putString(KEY_TEMPLATES, array.toString()).apply()
    }

    private fun writeTesters(items: List<TesterNote>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("id", item.id)
                    .put("username", item.username)
                    .put("profileUrl", item.profileUrl)
                    .put("note", item.note)
                    .put("reliable", item.reliable)
            )
        }
        prefs.edit().putString(KEY_TESTERS, array.toString()).apply()
    }
}
