package com.emby.client.player

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.emby.client.data.AuthManager
import com.emby.client.network.EmbyService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlayerActivity : AppCompatActivity(), GestureDetector.GestureListener {
    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private var playSessionId: String = ""
    private var itemId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        playerView = findViewById(R.id.player_view)
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        // Setup gesture detector
        val gestureDetector = GestureDetector(this, this)
        gestureDetector.attachToView(playerView)

        val playbackUrl = intent.getStringExtra("playbackUrl")
        itemId = intent.getStringExtra("itemId") ?: ""
        playSessionId = intent.getStringExtra("playSessionId") ?: PlayerUtils.generatePlaySessionId()

        if (playbackUrl != null) {
            CoroutineScope(Dispatchers.Main).launch {
                val actualUrl = if (playbackUrl.endsWith(".strm")) {
                    // Handle STRM file
                    PlayerUtils.parseStrmFile(playbackUrl)
                } else {
                    playbackUrl
                }

                if (actualUrl != null) {
                    val mediaItem = MediaItem.fromUri(actualUrl)
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.play()
                } else {
                    Toast.makeText(this@PlayerActivity, "Failed to parse STRM file", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        } else {
            Toast.makeText(this, "No playback URL provided", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Setup player listeners for progress reporting
        player.addListener(object : Player.Listener {
            override fun onPositionDiscontinuity(reason: Int) {
                super.onPositionDiscontinuity(reason)
                // Report progress when position changes
                reportProgress()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                // Report progress when play/pause state changes
                reportProgress()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (playbackState == Player.STATE_ENDED) {
                    // Report completed playback
                    reportProgress()
                }
            }
        })
    }

    override fun onHorizontalSwipe(distance: Float) {
        // Handle horizontal swipe for progress control
        val currentPosition = player.currentPosition
        val duration = player.duration
        if (duration > 0) {
            val seekAmount = (distance * 1000).toLong() // 1 second per 100 pixels
            val newPosition = currentPosition + seekAmount
            player.seekTo(Math.max(0, Math.min(newPosition, duration)))
        }
    }

    override fun onVerticalSwipeLeft(distance: Float) {
        // Handle vertical swipe on left side for brightness control
        val layoutParams = window.attributes
        val currentBrightness = layoutParams.screenBrightness
        val newBrightness = Math.max(0.1f, Math.min(currentBrightness + distance / 1000, 1.0f))
        layoutParams.screenBrightness = newBrightness
        window.attributes = layoutParams
    }

    override fun onVerticalSwipeRight(distance: Float) {
        // Handle vertical swipe on right side for volume control
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
        val volumeChange = (distance / 100).toInt()
        val newVolume = Math.max(0, Math.min(currentVolume + volumeChange, maxVolume))
        audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, newVolume, 0)
    }

    private fun reportProgress() {
        if (itemId.isEmpty()) return

        val serverProfile = AuthManager.getActiveServer(this)
        if (serverProfile == null) return

        val positionTicks = player.currentPosition * 10000 // Convert milliseconds to ticks
        val isPaused = !player.isPlaying

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val service = EmbyService(serverProfile)
                service.reportProgress(itemId, positionTicks, isPaused, playSessionId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    override fun onPause() {
        super.onPause()
        player.pause()
        reportProgress()
    }

    override fun onStop() {
        super.onStop()
        reportProgress()
        player.release()
    }
}
