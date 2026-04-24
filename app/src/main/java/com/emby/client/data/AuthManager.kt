package com.emby.client.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object AuthManager {
    private const val PREFS_NAME = "EmbyClientPrefs"
    private const val KEY_SERVERS = "servers"
    private const val KEY_ACTIVE_SERVER_ID = "active_server_id"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getServers(context: Context): List<ServerProfile> {
        val json = getPrefs(context).getString(KEY_SERVERS, null) ?: return emptyList()
        val type = object : TypeToken<List<ServerProfile>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun addServer(context: Context, server: ServerProfile) {
        val servers = getServers(context).toMutableList()
        servers.removeAll { it.url == server.url && it.username == server.username }
        servers.add(server)
        getPrefs(context).edit().putString(KEY_SERVERS, Gson().toJson(servers)).apply()
        setActiveServerId(context, server.id)
    }

    fun setActiveServerId(context: Context, id: String) {
        getPrefs(context).edit().putString(KEY_ACTIVE_SERVER_ID, id).apply()
    }

    fun getActiveServer(context: Context): ServerProfile? {
        val id = getPrefs(context).getString(KEY_ACTIVE_SERVER_ID, null) ?: return null
        return getServers(context).find { it.id == id }
    }

    fun logout(context: Context) {
        getPrefs(context).edit().remove(KEY_ACTIVE_SERVER_ID).apply()
    }
}
