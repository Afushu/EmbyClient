package com.emby.client.player

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.emby.client.R
import com.emby.client.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class PlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView

    private var progressJob: Job? = null
    private var itemId: String? = null
    private var serverUrl: String? = null
    private var token: String? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.playerView)
        
        val videoUrl = intent.getStringExtra("VIDEO_URL") ?: ""
        itemId = intent.getStringExtra("ITEM_ID")
        serverUrl = intent.getStringExtra("SERVER_URL")
        token = intent.getStringExtra("TOKEN")
        userId = intent.getStringExtra("USER_ID")

        initializePlayer(videoUrl)
    }

    private fun initializePlayer(url: String) {
        if (url.isEmpty()) return

        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        lifecycleScope.launch {
            val finalUrl = resolveStrmIfNecessary(url)
            val mediaItem = MediaItem.fromUri(finalUrl)
            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.playWhenReady = true

            startProgressReporting()
        }
    }

    private suspend fun resolveStrmIfNecessary(url: String): String {
        if (!url.endsWith(".strm", ignoreCase = true)) return url
        
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val body = response.body?.string()?.trim()
                if (!body.isNullOrEmpty() && (body.startsWith("http") || body.startsWith("https"))) {
                    return@withContext body
                }
            } catch (e: Exception) {
                Log.e("PlayerActivity", "Failed to parse STRM", e)
            }
            url
        }
    }

    private fun startProgressReporting() {
        if (itemId == null || serverUrl == null || token == null) return
        
        val api = RetrofitClient.getClient(serverUrl!!)
        val authHeader = "MediaBrowser Client=\"EmbyClient\", Device=\"Android\", DeviceId=\"12345\", Version=\"1.0.0\", Token=\"$token\""

        progressJob = lifecycleScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(10000) // Report every 10 seconds
                val position = player?.currentPosition ?: 0
                val isPaused = player?.playWhenReady?.not() ?: true
                
                try {
                    api.reportProgress(itemId!!, position * 10000, isPaused, authHeader)
                } catch (e: Exception) {
                    Log.e("PlayerActivity", "Failed to report progress", e)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onStop() {
        super.onStop()
        player?.release()
        player = null
        progressJob?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        progressJob?.cancel()
    }
}
