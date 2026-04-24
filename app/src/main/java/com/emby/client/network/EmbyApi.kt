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
        @Query("Fields") fields: String? = "Overview,CommunityRating,People,Genres,Year,Studios,Tags,Runtime,MediaStreams,Audio,Video",
        @Query("Recursive") recursive: Boolean = false,
        @Query("Limit") limit: Int? = null,
        @Query("Offset") offset: Int? = null,
        @Query("IncludeItemTypes") includeItemTypes: String? = null,
        @Query("Filters") filters: String? = null,
        @Header("X-Emby-Authorization") authHeader: String
    ): QueryResult

    @GET("Items/{itemId}")
    suspend fun getItemDetails(
        @Path("itemId") itemId: String,
        @Query("UserId") userId: String,
        @Query("Fields") fields: String? = "Overview,CommunityRating,People,Genres,Year,Studios,Tags,Runtime,MediaStreams,Audio,Video,Chapters",
        @Header("X-Emby-Authorization") authHeader: String
    ): Any

    @GET("Items/{itemId}/Children")
    suspend fun getItemChildren(
        @Path("itemId") itemId: String,
        @Query("UserId") userId: String,
        @Query("SortBy") sortBy: String? = "SortName",
        @Query("SortOrder") sortOrder: String? = "Ascending",
        @Query("Fields") fields: String? = "Overview,CommunityRating,People,Genres,Year,Studios,Tags,Runtime,MediaStreams,Audio,Video",
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
        @Query("PlaySessionId") playSessionId: String,
        @Header("X-Emby-Authorization") authHeader: String
    )

    @POST("Users/{userId}/Items/{itemId}/Favorite")
    suspend fun toggleFavorite(
        @Path("userId") userId: String,
        @Path("itemId") itemId: String,
        @Header("X-Emby-Authorization") authHeader: String
    )

    @GET("Users/{userId}/Items/Resume")
    suspend fun getResumeItems(
        @Path("userId") userId: String,
        @Query("Limit") limit: Int? = 20,
        @Header("X-Emby-Authorization") authHeader: String
    ): QueryResult

    @GET("Users/{userId}/Items/RecentlyAdded")
    suspend fun getRecentlyAdded(
        @Path("userId") userId: String,
        @Query("Limit") limit: Int? = 20,
        @Query("IncludeItemTypes") includeItemTypes: String? = "Movie,Episode",
        @Header("X-Emby-Authorization") authHeader: String
    ): QueryResult

    @GET("Users/{userId}/Items/Favorites")
    suspend fun getFavorites(
        @Path("userId") userId: String,
        @Query("Limit") limit: Int? = 20,
        @Header("X-Emby-Authorization") authHeader: String
    ): QueryResult
}
