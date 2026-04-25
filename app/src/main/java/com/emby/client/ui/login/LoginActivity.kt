package com.emby.client.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.emby.client.data.AuthManager
import com.emby.client.data.ServerProfile
import com.emby.client.network.EmbyApi
import com.emby.client.network.RetrofitClient
import com.emby.client.data.AuthRequest
import com.emby.client.data.AuthResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: com.emby.client.databinding.ActivityLoginBinding
    private var editingServer: ServerProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = com.emby.client.databinding.ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editingServer = intent.getSerializableExtra("server") as? ServerProfile
        if (editingServer != null) {
            binding.etServerUrl.setText(editingServer!!.url)
            binding.etUsername.setText(editingServer!!.username)
        }

        binding.btnLogin.setOnClickListener {
            val serverUrl = binding.etServerUrl.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (serverUrl.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            login(serverUrl, username, password)
        }

        binding.btnServerList.setOnClickListener {
            startActivity(Intent(this, ServerListActivity::class.java))
        }
    }

    private fun login(serverUrl: String, username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api: EmbyApi = RetrofitClient.getClient(serverUrl)
                val authHeader = "MediaBrowser Client=\"EmbyClient\", Device=\"Android\", DeviceId=\"${UUID.randomUUID()}\", Version=\"1.0\""
                val authRequest = AuthRequest(username, password)
                val authResponse: AuthResponse = api.authenticate(authRequest, authHeader)

                val serverProfile = ServerProfile(
                    id = editingServer?.id ?: UUID.randomUUID().toString(),
                    url = serverUrl,
                    username = username,
                    token = authResponse.AccessToken,
                    userId = authResponse.User.Id
                )

                if (editingServer != null) {
                    AuthManager.updateServer(this@LoginActivity, serverProfile)
                } else {
                    AuthManager.addServer(this@LoginActivity, serverProfile)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, com.emby.client.MainActivity::class.java))
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
