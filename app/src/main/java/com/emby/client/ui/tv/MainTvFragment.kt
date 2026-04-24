package com.emby.client.ui.tv

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import com.emby.client.R
import com.emby.client.data.AuthManager
import com.emby.client.data.BaseItemDto
import com.emby.client.network.RetrofitClient
import com.emby.client.player.PlayerActivity
import kotlinx.coroutines.launch

class MainTvFragment : BrowseSupportFragment() {

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title = getString(R.string.title_tv)
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        brandColor = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.design_default_color_primary)
        
        loadData()
        setupEventListeners()
    }

    private fun loadData() {
        val activeServer = AuthManager.getActiveServer(requireContext()) ?: return
        val api = RetrofitClient.getClient(activeServer.url)
        val authHeader = "MediaBrowser Client=\"EmbyClient\", Device=\"Android\", DeviceId=\"12345\", Version=\"1.0.0\", Token=\"${activeServer.token}\""

        lifecycleScope.launch {
            try {
                val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
                val cardPresenter = CardPresenter(activeServer.url)

                val viewsResult = api.getViews(activeServer.userId, authHeader)
                
                for ((index, viewDto) in viewsResult.Items.withIndex()) {
                    val listRowAdapter = ArrayObjectAdapter(cardPresenter)
                    val header = HeaderItem(index.toLong(), viewDto.Name)
                    
                    val itemsResult = api.getItems(
                        userId = activeServer.userId,
                        parentId = viewDto.Id,
                        authHeader = authHeader
                    )
                    
                    itemsResult.Items.forEach { listRowAdapter.add(it) }
                    rowsAdapter.add(ListRow(header, listRowAdapter))
                }
                
                adapter = rowsAdapter
            } catch (e: Exception) {
                Log.e("MainTvFragment", "Error loading data", e)
                Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupEventListeners() {
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            if (item is BaseItemDto) {
                if (item.IsFolder || item.Type == "Series") {
                    val intent = Intent(requireContext(), TvDetailsActivity::class.java).apply {
                        putExtra("ITEM_ID", item.Id)
                        putExtra("ITEM_TYPE", item.Type)
                        putExtra("PARENT_ID", item.ParentIndexNumber?.toString() ?: "")
                    }
                    startActivity(intent)
                } else {
                    val activeServer = AuthManager.getActiveServer(requireContext())
                    if (activeServer != null) {
                        // Play video via Stream URL. We'll let PlayerActivity fetch playback info or just pass Stream URL.
                        // Emby stream URL pattern for direct play MVP:
                        val streamUrl = "${activeServer.url}/Videos/${item.Id}/stream.mp4?Static=true&api_key=${activeServer.token}"
                        
                        val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
                            putExtra("VIDEO_URL", streamUrl)
                            putExtra("ITEM_ID", item.Id)
                            putExtra("SERVER_URL", activeServer.url)
                            putExtra("TOKEN", activeServer.token)
                            putExtra("USER_ID", activeServer.userId)
                            item.UserData?.PlaybackPositionTicks?.let { ticks ->
                                putExtra("POSITION_TICKS", ticks)
                            }
                        }
                        startActivity(intent)
                    }
                }
            }
        }
    }
}
