package com.emby.client.player

import android.content.Context
import com.emby.client.data.ServerProfile
import com.emby.client.network.EmbyService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

object PlayerUtils {
    suspend fun getPlaybackUrl(serverProfile: ServerProfile, itemId: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val service = EmbyService(serverProfile)
                val playbackInfo = service.getPlaybackInfo(itemId)
                // Parse playbackInfo to get the actual playback URL
                // This is a simplified implementation
                "http://example.com/stream"
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun parseStrmFile(strmContent: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                // If the strm content is a URL, return it directly
                if (strmContent.startsWith("http://") || strmContent.startsWith("https://")) {
                    return@withContext strmContent
                }
                // If it's a local file path, read the content
                else if (strmContent.startsWith("file://")) {
                    val filePath = strmContent.substring(7)
                    val file = java.io.File(filePath)
                    if (file.exists()) {
                        file.readText().trim()
                    } else {
                        null
                    }
                }
                // If it's a network URL, fetch the content
                else {
                    val url = URL(strmContent)
                    val connection = url.openConnection()
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val content = reader.readText()
                    reader.close()
                    content.trim()
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    fun generatePlaySessionId(): String {
        return java.util.UUID.randomUUID().toString()
    }
}
