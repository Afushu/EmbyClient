package com.emby.client.player

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class PlayerActivity : AppCompatActivity() {
    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        playerView = findViewById(R.id.player_view)
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        val playbackUrl = intent.getStringExtra("playbackUrl")
        if (playbackUrl != null) {
            val mediaItem = MediaItem.fromUri(playbackUrl)
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        } else {
            Toast.makeText(this, "No playback URL provided", Toast.LENGTH_SHORT).show()
            finish()
        }
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
