package com.emby.client.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
            this,
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

        binding.lvServers.adapter = serverAdapter

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

    class ServerAdapter(
        private val context: ServerListActivity,
        private var servers: List<ServerProfile>,
        private var activeServerId: String?,
        private val onServerSelect: (ServerProfile) -> Unit,
        private val onServerEdit: (ServerProfile) -> Unit,
        private val onServerRemove: (ServerProfile) -> Unit
    ) : ArrayAdapter<ServerProfile>(context, android.R.layout.simple_list_item_2, servers) {

        fun updateServers(newServers: List<ServerProfile>, newActiveServerId: String?) {
            servers = newServers
            activeServerId = newActiveServerId
            clear()
            addAll(servers)
            notifyDataSetChanged()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: android.view.LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false)
            val server = servers[position]
            val title = view.findViewById<TextView>(android.R.id.text1)
            val subtitle = view.findViewById<TextView>(android.R.id.text2)

            title.text = server.url
            subtitle.text = if (server.id == activeServerId) "Active" else ""

            view.setOnClickListener {
                onServerSelect(server)
            }

            view.setOnLongClickListener {
                onServerEdit(server)
                true
            }

            return view
        }
    }
}
