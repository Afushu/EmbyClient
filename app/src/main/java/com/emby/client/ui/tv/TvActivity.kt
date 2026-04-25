package com.emby.client.ui.tv

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.emby.client.R

class TvActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv)

        if (savedInstanceState == null) {
            val fragment = TvHomeFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }
}
