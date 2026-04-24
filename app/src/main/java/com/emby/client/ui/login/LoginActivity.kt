package com.emby.client.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.emby.client.MainActivity
import com.emby.client.R
import com.emby.client.data.AuthManager
import com.emby.client.data.AuthRequest
import com.emby.client.data.ServerProfile
import com.emby.client.network.RetrofitClient
import kotlinx.coroutines.launch
import java.util.UUID

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val active = AuthManager.getActiveServer(this)
        if (active != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val etUrl = findViewById<EditText>(R.id.etUrl)
        val etUser = findViewById<EditText>(R.id.etUser)
        val etPass = findViewById<EditText>(R.id.etPass)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val url = etUrl.text.toString().trim()
            val user = etUser.text.toString().trim()
            val pass = etPass.text.toString().trim()

            if (url.isEmpty() || user.isEmpty()) {
                Toast.makeText(this, "URL and Username required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val api = RetrofitClient.getClient(url)
                    val authHeader = "MediaBrowser Client=\"EmbyClient\", Device=\"Android\", DeviceId=\"12345\", Version=\"1.0.0\""
                    val res = api.authenticate(AuthRequest(user, pass), authHeader)
                    
                    val profile = ServerProfile(
                        id = UUID.randomSequence().toString(),
                        url = url,
                        username = user,
                        token = res.AccessToken,
                        userId = res.User.Id
                    )
                    AuthManager.addServer(this@LoginActivity, profile)
                    
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
