package com.emby.client.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
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

        // 初始化协议选择器
        val protocols = arrayOf("HTTPS", "HTTP")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, protocols)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spProtocol.adapter = adapter

        // 初始化端口输入框
        binding.etPort.setText("443")

        editingServer = intent.getSerializableExtra("server") as? ServerProfile
        if (editingServer != null) {
            binding.etServerUrl.setText(editingServer!!.url)
            binding.etUsername.setText(editingServer!!.username)
        }

        binding.btnLogin.setOnClickListener {
            val serverUrl = binding.etServerUrl.text.toString().trim()
            val protocol = binding.spProtocol.selectedItem.toString()
            val port = binding.etPort.text.toString().trim()
            val path = binding.etPath.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (serverUrl.isEmpty() || username.isEmpty() || password.isEmpty() || port.isEmpty()) {
                Toast.makeText(this, "请填写所有必填字段", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 构建完整的服务器URL
            val fullUrl = buildServerUrl(protocol, serverUrl, port, path)
            login(fullUrl, username, password)
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun buildServerUrl(protocol: String, serverUrl: String, port: String, path: String): String {
        var url = "$protocol://$serverUrl:$port"
        if (path.isNotEmpty()) {
            url += "/$path"
        }
        return url
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
                    Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, ServerListActivity::class.java))
                    finish()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "登录失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
