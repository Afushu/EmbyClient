package com.emby.client.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.emby.client.data.AuthManager
import com.emby.client.data.ServerProfile
import com.emby.client.databinding.ActivityServerListBinding

class ServerListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityServerListBinding
    private lateinit var serverAdapter: ServerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        serverAdapter = ServerAdapter(
            AuthManager.getServers(this),
            AuthManager.getActiveServerId(this),
            {
                AuthManager.setActiveServerId(this, it.id)
                Toast.makeText(this, "Server switched", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, com.emby.client.MainActivity::class.java))
                finish()
            },
            {
                // Edit server
                val intent = Intent(this, LoginActivity::class.java)
                intent.putExtra("server", it)
                startActivity(intent)
            },
            {
                // Remove server
                AuthManager.removeServer(this, it.id)
                serverAdapter.updateServers(AuthManager.getServers(this), AuthManager.getActiveServerId(this))
                Toast.makeText(this, "Server removed", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvServers.apply {
            layoutManager = LinearLayoutManager(this@ServerListActivity)
            adapter = serverAdapter
        }

        binding.btnAddServer.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        serverAdapter.updateServers(AuthManager.getServers(this), AuthManager.getActiveServerId(this))
    }
}
