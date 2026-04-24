package com.emby.client.data

import java.io.Serializable

data class AuthRequest(val Username: String, val Pw: String)
data class AuthResponse(val AccessToken: String, val User: UserDto)
data class UserDto(val Id: String, val Name: String)

data class QueryResult(val Items: List<BaseItemDto>, val TotalRecordCount: Int)

data class BaseItemDto(
    val Id: String,
    val Name: String,
    val Type: String,
    val RunTimeTicks: Long?,
    val ImageTags: Map<String, String>?,
    val IsFolder: Boolean,
    val Overview: String?,
    val CommunityRating: Float?,
    val ProductionYear: Int?,
    val IndexNumber: Int?,
    val ParentIndexNumber: Int?,
    val UserData: UserItemDataDto?
)

data class UserItemDataDto(
    val PlaybackPositionTicks: Long,
    val PlayCount: Int,
    val IsFavorite: Boolean,
    val Played: Boolean
)

data class ServerProfile(
    val id: String,
    val url: String,
    val username: String,
    val token: String,
    val userId: String
) : Serializable
