package com.emby.client.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
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
    private lateinit var emptyState: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        emptyState = findViewById(R.id.empty_state)

        serverAdapter = ServerAdapter(
            this,
            AuthManager.getServers(this),
            AuthManager.getActiveServerId(this),
            {
                AuthManager.setActiveServerId(this, it.id)
                Toast.makeText(this, "服务器已切换", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, com.emby.client.MainActivity::class.java))
                finish()
            },
            {
                // 编辑服务器
                val intent = Intent(this, LoginActivity::class.java)
                intent.putExtra("server", it)
                startActivity(intent)
            },
            {
                // 删除服务器
                AuthManager.removeServer(this, it.id)
                serverAdapter.updateServers(AuthManager.getServers(this), AuthManager.getActiveServerId(this))
                updateEmptyState()
                Toast.makeText(this, "服务器已删除", Toast.LENGTH_SHORT).show()
            }
        )

        binding.lvServers.adapter = serverAdapter

        findViewById<ImageView>(R.id.fab_add_server).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        updateEmptyState()
    }

    override fun onResume() {
        super.onResume()
        serverAdapter.updateServers(AuthManager.getServers(this), AuthManager.getActiveServerId(this))
        updateEmptyState()
    }

    private fun updateEmptyState() {
        val servers = AuthManager.getServers(this)
        if (servers.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            binding.lvServers.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            binding.lvServers.visibility = View.VISIBLE
        }
    }

    class ServerAdapter(
        private val context: ServerListActivity,
        private var servers: List<ServerProfile>,
        private var activeServerId: String?,
        private val onServerSelect: (ServerProfile) -> Unit,
        private val onServerEdit: (ServerProfile) -> Unit,
        private val onServerRemove: (ServerProfile) -> Unit
    ) : ArrayAdapter<ServerProfile>(context, R.layout.item_server, servers) {

        fun updateServers(newServers: List<ServerProfile>, newActiveServerId: String?) {
            servers = newServers
            activeServerId = newActiveServerId
            clear()
            addAll(servers)
            notifyDataSetChanged()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: android.view.LayoutInflater.from(context).inflate(R.layout.item_server, parent, false)
            val server = servers[position]
            val title = view.findViewById<TextView>(R.id.tvServerName)

            title.text = server.url

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
