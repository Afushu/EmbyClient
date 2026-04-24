package com.emby.client.player

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlayerActivity : AppCompatActivity() {
    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private var playSessionId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.player_view)
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        val playbackUrl = intent.getStringExtra("playbackUrl")
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
        })
    }

    private fun reportProgress() {
        // Implement progress reporting to Emby server
        // This will be implemented in a later step
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onStop() {
        super.onStop()
        player.release()
    }
}
