package com.emby.client.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.emby.client.R
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
    private lateinit var etServerUrl: EditText
    private lateinit var spProtocol: Spinner
    private lateinit var etPort: EditText
    private lateinit var etPath: EditText
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnCancel: Button
    private var editingServer: ServerProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 初始化视图
        etServerUrl = findViewById(R.id.etServerUrl)
        spProtocol = findViewById(R.id.spProtocol)
        etPort = findViewById(R.id.etPort)
        etPath = findViewById(R.id.etPath)
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnCancel = findViewById(R.id.btnCancel)

        // 初始化协议选择器
        val protocols = arrayOf("HTTPS", "HTTP")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, protocols)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spProtocol.adapter = adapter

        // 初始化端口输入框
        etPort.setText("443")

        editingServer = intent.getSerializableExtra("server") as? ServerProfile
        if (editingServer != null) {
            etServerUrl.setText(editingServer!!.url)
            etUsername.setText(editingServer!!.username)
        }

        btnLogin.setOnClickListener {
            val serverUrl = etServerUrl.text.toString().trim()
            val protocol = spProtocol.selectedItem.toString()
            val port = etPort.text.toString().trim()
            val path = etPath.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (serverUrl.isEmpty() || username.isEmpty() || password.isEmpty() || port.isEmpty()) {
                Toast.makeText(this, "请填写所有必填字段", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 构建完整的服务器URL
            val fullUrl = buildServerUrl(protocol, serverUrl, port, path)
            login(fullUrl, username, password)
        }

        btnCancel.setOnClickListener {
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
