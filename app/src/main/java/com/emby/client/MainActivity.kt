package com.emby.client

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.emby.client.data.AuthManager
import com.emby.client.ui.login.LoginActivity
import com.emby.client.ui.mobile.MobileActivity
import com.emby.client.ui.tv.TvActivity
import com.emby.client.utils.DeviceUtils

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (AuthManager.getActiveServer(this) == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        if (DeviceUtils.isTv(this)) {
            startActivity(Intent(this, TvActivity::class.java))
        } else {
            startActivity(Intent(this, MobileActivity::class.java))
        }
        finish()
    }
}
