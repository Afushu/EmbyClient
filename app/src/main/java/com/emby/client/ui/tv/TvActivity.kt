package com.emby.client.ui.tv

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.emby.client.R

class TvActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.tv_frame, MainTvFragment())
                .commit()
        }
    }
}
