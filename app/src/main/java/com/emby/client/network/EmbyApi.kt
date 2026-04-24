package com.emby.client.network

import com.emby.client.data.AuthRequest
import com.emby.client.data.AuthResponse
import com.emby.client.data.QueryResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface EmbyApi {
    @POST("Users/AuthenticateByName")
    suspend fun authenticate(
        @Body request: AuthRequest,
        @Header("X-Emby-Authorization") authHeader: String
    ): AuthResponse

    @GET("Users/{userId}/Views")
    suspend fun getViews(
        @Path("userId") userId: String,
        @Header("X-Emby-Authorization") authHeader: String
    ): QueryResult

    @GET("Users/{userId}/Items")
    suspend fun getItems(
        @Path("userId") userId: String,
        @Query("ParentId") parentId: String? = null,
        @Query("SortBy") sortBy: String? = "SortName",
        @Query("SortOrder") sortOrder: String? = "Ascending",
        @Query("Fields") fields: String? = "Overview,CommunityRating,People",
        @Query("Recursive") recursive: Boolean = false,
        @Header("X-Emby-Authorization") authHeader: String
    ): QueryResult

    @GET("Items/{itemId}/PlaybackInfo")
    suspend fun getPlaybackInfo(
        @Path("itemId") itemId: String,
        @Query("UserId") userId: String,
        @Header("X-Emby-Authorization") authHeader: String
    ): Any

    @POST("Sessions/Playing/Progress")
    suspend fun reportProgress(
        @Query("ItemId") itemId: String,
        @Query("PositionTicks") positionTicks: Long,
        @Query("IsPaused") isPaused: Boolean,
        @Header("X-Emby-Authorization") authHeader: String
    )
}
