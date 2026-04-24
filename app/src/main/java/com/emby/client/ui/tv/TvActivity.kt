package com.emby.client.ui.tv

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import com.emby.client.R
import com.emby.client.player.PlayerActivity

class TvActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv)

        val btnPlay = findViewById<Button>(R.id.btnPlaySample)
        btnPlay.requestFocus()
        btnPlay.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra("VIDEO_URL", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
            }
            startActivity(intent)
        }
    }
}
