package com.emby.client.ui.mobile

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emby.client.R
import com.emby.client.data.AuthManager
import com.emby.client.data.ServerProfile
import com.emby.client.network.RetrofitClient
import com.emby.client.player.PlayerActivity
import com.emby.client.ui.login.LoginActivity
import kotlinx.coroutines.launch

class MobileActivity : AppCompatActivity() {
    private lateinit var adapter: MobileAdapter
    private var activeServer: ServerProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mobile)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        activeServer = AuthManager.getActiveServer(this)
        if (activeServer == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        supportActionBar?.title = "Emby Mobile - ${activeServer?.username}"

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        
        adapter = MobileAdapter(activeServer!!.url) { item ->
            if (item.IsFolder) {
                // Navigate into folder - simplified for MVP
                Toast.makeText(this, "Folder navigation not fully implemented in MVP", Toast.LENGTH_SHORT).show()
            } else {
                val streamUrl = "${activeServer!!.url}/Videos/${item.Id}/stream.mp4?Static=true&api_key=${activeServer!!.token}"
                val intent = Intent(this, PlayerActivity::class.java).apply {
                    putExtra("VIDEO_URL", streamUrl)
                    putExtra("ITEM_ID", item.Id)
                    putExtra("SERVER_URL", activeServer!!.url)
                    putExtra("TOKEN", activeServer!!.token)
                    putExtra("USER_ID", activeServer!!.userId)
                }
                startActivity(intent)
            }
        }
        recyclerView.adapter = adapter

        loadData()
    }

    private fun loadData() {
        val server = activeServer ?: return
        val api = RetrofitClient.getClient(server.url)
        val authHeader = "MediaBrowser Client=\"EmbyClient\", Device=\"Android\", DeviceId=\"12345\", Version=\"1.0.0\", Token=\"${server.token}\""

        lifecycleScope.launch {
            try {
                // Simplified: load first view's items directly
                val views = api.getViews(server.userId, authHeader)
                if (views.Items.isNotEmpty()) {
                    val viewId = views.Items.first().Id
                    val items = api.getItems(
                        userId = server.userId,
                        parentId = viewId,
                        authHeader = authHeader
                    )
                    adapter.submitList(items.Items)
                }
            } catch (e: Exception) {
                Toast.makeText(this@MobileActivity, "Error loading items", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add(0, 1, 0, "Logout")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 1) {
            AuthManager.logout(this)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
