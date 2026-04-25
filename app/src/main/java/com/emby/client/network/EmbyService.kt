package com.emby.client.network

import com.emby.client.data.AuthRequest
import com.emby.client.data.AuthResponse
import com.emby.client.data.QueryResult
import com.emby.client.data.ServerProfile
import java.util.UUID

class EmbyService(private val serverProfile: ServerProfile) {
    private val api: EmbyApi by lazy {
        RetrofitClient.getClient(serverProfile.url)
    }

    private val authHeader: String
        get() = "MediaBrowser Client=\"EmbyClient\", Device=\"Android\", DeviceId=\"${UUID.randomUUID()}\", Version=\"1.0\", Token=\"${serverProfile.token}\""

    suspend fun authenticate(username: String, password: String): AuthResponse {
        val authRequest = AuthRequest(username, password)
        return api.authenticate(authRequest, authHeader)
    }

    suspend fun getViews(): QueryResult {
        return api.getViews(serverProfile.userId, authHeader)
    }

    suspend fun getItems(
        parentId: String? = null,
        sortBy: String? = "SortName",
        sortOrder: String? = "Ascending",
        fields: String? = "Overview,CommunityRating,People,Genres,Year,Studios,Tags,Runtime,MediaStreams,Audio,Video",
        recursive: Boolean = false,
        limit: Int? = null,
        offset: Int? = null,
        includeItemTypes: String? = null,
        filters: String? = null
    ): QueryResult {
        return api.getItems(
            serverProfile.userId,
            parentId,
            sortBy,
            sortOrder,
            fields,
            recursive,
            limit,
            offset,
            includeItemTypes,
            filters,
            authHeader
        )
    }

    suspend fun getItemDetails(itemId: String): Any {
        return api.getItemDetails(itemId, serverProfile.userId, authHeader = authHeader)
    }

    suspend fun getItemChildren(itemId: String): QueryResult {
        return api.getItemChildren(itemId, serverProfile.userId, authHeader = authHeader)
    }

    suspend fun getPlaybackInfo(itemId: String): Any {
        return api.getPlaybackInfo(itemId, serverProfile.userId, authHeader)
    }

    suspend fun reportProgress(itemId: String, positionTicks: Long, isPaused: Boolean, playSessionId: String) {
        api.reportProgress(itemId, positionTicks, isPaused, playSessionId, authHeader)
    }

    suspend fun toggleFavorite(itemId: String) {
        api.toggleFavorite(serverProfile.userId, itemId, authHeader)
    }

    suspend fun getResumeItems(limit: Int? = 20): QueryResult {
        return api.getResumeItems(serverProfile.userId, limit, authHeader)
    }

    suspend fun getRecentlyAdded(limit: Int? = 20, includeItemTypes: String? = "Movie,Episode"): QueryResult {
        return api.getRecentlyAdded(serverProfile.userId, limit, includeItemTypes, authHeader)
    }

    suspend fun getFavorites(limit: Int? = 20): QueryResult {
        return api.getFavorites(serverProfile.userId, limit, authHeader)
    }
}
