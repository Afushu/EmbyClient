package com.emby.client.ui.tv

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.emby.client.R

class TvDetailsActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv_details)

        if (savedInstanceState == null) {
            val fragment = TvDetailsFragment()
            fragment.arguments = intent.extras
            supportFragmentManager.beginTransaction()
                .replace(R.id.tv_details_frame, fragment)
                .commit()
        }
    }
}
