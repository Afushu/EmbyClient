package com.emby.client.ui.mobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emby.client.R
import com.emby.client.data.AuthManager
import com.emby.client.data.ServerProfile
import com.emby.client.network.RetrofitClient
import com.emby.client.player.PlayerActivity
import kotlinx.coroutines.launch

class MobileDetailsActivity : AppCompatActivity() {

    private var itemId: String? = null
    private var itemType: String? = null
    private var activeServer: ServerProfile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mobile_details)

        itemId = intent.getStringExtra("ITEM_ID")
        itemType = intent.getStringExtra("ITEM_TYPE")
        activeServer = AuthManager.getActiveServer(this)

        if (itemId == null || activeServer == null) {
            finish()
            return
        }

        loadItemDetails()
    }

    private fun loadItemDetails() {
        val server = activeServer!!
        val api = RetrofitClient.getClient(server.url)
        val authHeader = "MediaBrowser Client=\"EmbyClient\", Device=\"Android\", DeviceId=\"12345\", Version=\"1.0.0\", Token=\"${server.token}\""

        lifecycleScope.launch {
            try {
                // Get the item details directly from API
                val response = api.getItems(
                    userId = server.userId,
                    parentId = intent.getStringExtra("PARENT_ID"),
                    recursive = true,
                    authHeader = authHeader
                )
                
                val item = response.Items.find { it.Id == itemId }
                if (item != null) {
                    val tvTitle = findViewById<TextView>(R.id.tvTitle)
                    val tvOverview = findViewById<TextView>(R.id.tvOverview)
                    val tvMeta = findViewById<TextView>(R.id.tvMeta)
                    val ivPoster = findViewById<ImageView>(R.id.ivPoster)
                    val ivBackdrop = findViewById<ImageView>(R.id.ivBackdrop)
                    val btnPlay = findViewById<Button>(R.id.btnPlay)

                    tvTitle.text = item.Name
                    tvOverview.text = item.Overview ?: "No description available."
                    tvMeta.text = "${item.ProductionYear ?: ""} | Rating: ${item.CommunityRating ?: "N/A"}"

                    val primaryTag = item.ImageTags?.get("Primary")
                    if (primaryTag != null) {
                        Glide.with(this@MobileDetailsActivity)
                            .load("${server.url}/Items/${item.Id}/Images/Primary?tag=$primaryTag&maxWidth=400")
                            .into(ivPoster)
                    }

                    val backdropTag = item.ImageTags?.get("Backdrop")
                    if (backdropTag != null) {
                        Glide.with(this@MobileDetailsActivity)
                            .load("${server.url}/Items/${item.Id}/Images/Backdrop?tag=$backdropTag&maxWidth=1920")
                            .into(ivBackdrop)
                    }

                    if (item.IsFolder || item.Type == "Series" || item.Type == "Season") {
                        btnPlay.text = "Browse Episodes"
                        btnPlay.setOnClickListener {
                            loadChildren(item.Id)
                        }
                    } else {
                        btnPlay.setOnClickListener {
                            playItem(item.Id)
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@MobileDetailsActivity, "Error loading details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadChildren(parentId: String) {
        val rvEpisodes = findViewById<RecyclerView>(R.id.rvEpisodes)
        rvEpisodes.visibility = View.VISIBLE
        rvEpisodes.layoutManager = LinearLayoutManager(this)

        val server = activeServer!!
        val api = RetrofitClient.getClient(server.url)
        val authHeader = "MediaBrowser Client=\"EmbyClient\", Device=\"Android\", DeviceId=\"12345\", Version=\"1.0.0\", Token=\"${server.token}\""

        lifecycleScope.launch {
            try {
                val response = api.getItems(
                    userId = server.userId,
                    parentId = parentId,
                    authHeader = authHeader
                )
                // Reusing MobileAdapter but forcing a linear layout look can be done,
                // or we can create a specific EpisodeAdapter. 
                // For MVP, we reuse MobileAdapter and map clicks.
                val adapter = MobileAdapter(server.url) { clickedItem ->
                    if (clickedItem.IsFolder || clickedItem.Type == "Season") {
                        loadChildren(clickedItem.Id)
                    } else {
                        playItem(clickedItem.Id)
                    }
                }
                rvEpisodes.adapter = adapter
                adapter.submitList(response.Items)
            } catch (e: Exception) {
                Toast.makeText(this@MobileDetailsActivity, "Failed to load episodes", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun playItem(id: String) {
        val server = activeServer!!
        val streamUrl = "${server.url}/Videos/$id/stream.mp4?Static=true&api_key=${server.token}"
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra("VIDEO_URL", streamUrl)
            putExtra("ITEM_ID", id)
            putExtra("SERVER_URL", server.url)
            putExtra("TOKEN", server.token)
            putExtra("USER_ID", server.userId)
        }
        startActivity(intent)
    }
}
