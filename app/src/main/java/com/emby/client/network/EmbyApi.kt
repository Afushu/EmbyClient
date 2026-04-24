package com.emby.client.network

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface EmbyApi {
    @GET("System/Info")
    suspend fun getSystemInfo(
        @Header("X-Emby-Authorization") authHeader: String
    ): Any // Map to a specific DTO in a real app

    @GET("Users/{userId}/Items")
    suspend fun getItems(
        @Path("userId") userId: String,
        @Query("ParentId") parentId: String? = null,
        @Query("IncludeItemTypes") itemTypes: String? = "Movie,Series",
        @Query("Recursive") recursive: Boolean = true,
        @Header("X-Emby-Authorization") authHeader: String
    ): Any

    @GET("Items/{itemId}/PlaybackInfo")
    suspend fun getPlaybackInfo(
        @Path("itemId") itemId: String,
        @Query("UserId") userId: String,
        @Header("X-Emby-Authorization") authHeader: String
    ): Any
}
