package com.emby.client.ui.mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.emby.client.R
import com.emby.client.player.PlayerActivity

class MobileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mobile)

        findViewById<Button>(R.id.btnPlaySample).setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra("VIDEO_URL", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
            }
            startActivity(intent)
        }
    }
}
