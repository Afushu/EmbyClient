package com.emby.client.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emby.client.network.RetrofitClient
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _systemInfo = MutableLiveData<String>()
    val systemInfo: LiveData<String> = _systemInfo

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchSystemInfo(serverUrl: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val api = RetrofitClient.getClient(serverUrl)
                val authHeader = "MediaBrowser Client=\"EmbyClient\", Device=\"Android\", DeviceId=\"12345\", Version=\"1.0.0\", Token=\"$apiKey\""
                val result = api.getSystemInfo(authHeader)
                _systemInfo.postValue("Connected successfully to Emby Server")
            } catch (e: Exception) {
                _error.postValue(e.message ?: "Unknown error occurred")
            }
        }
    }
}
