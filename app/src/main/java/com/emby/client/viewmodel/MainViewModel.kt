package com.emby.client.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emby.client.data.AuthManager
import com.emby.client.data.ServerProfile
import com.emby.client.data.QueryResult
import com.emby.client.network.EmbyService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val context: Context) : ViewModel() {
    private val _serverProfile = MutableStateFlow<ServerProfile?>(null)
    val serverProfile: StateFlow<ServerProfile?> = _serverProfile

    private val _views = MutableStateFlow<QueryResult?>(null)
    val views: StateFlow<QueryResult?> = _views

    private val _resumeItems = MutableStateFlow<QueryResult?>(null)
    val resumeItems: StateFlow<QueryResult?> = _resumeItems

    private val _recentlyAdded = MutableStateFlow<QueryResult?>(null)
    val recentlyAdded: StateFlow<QueryResult?> = _recentlyAdded

    private val _favorites = MutableStateFlow<QueryResult?>(null)
    val favorites: StateFlow<QueryResult?> = _favorites

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadActiveServer()
    }

    private fun loadActiveServer() {
        val server = AuthManager.getActiveServer(context)
        _serverProfile.value = server
        if (server != null) {
            loadInitialData(server)
        }
    }

    private fun loadInitialData(server: ServerProfile) {
        val service = EmbyService(server)
        _isLoading.value = true

        viewModelScope.launch {
            try {
                _views.value = service.getViews()
                _resumeItems.value = service.getResumeItems()
                _recentlyAdded.value = service.getRecentlyAdded()
                _favorites.value = service.getFavorites()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData() {
        _serverProfile.value?.let { loadInitialData(it) }
    }

    fun switchServer(serverId: String) {
        AuthManager.setActiveServerId(context, serverId)
        loadActiveServer()
    }

    fun logout() {
        AuthManager.logout(context)
        _serverProfile.value = null
        _views.value = null
        _resumeItems.value = null
        _recentlyAdded.value = null
        _favorites.value = null
    }
}
