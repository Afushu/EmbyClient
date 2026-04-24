package com.emby.client.ui.tv

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.leanback.widget.OnItemViewClickedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.emby.client.R
import com.emby.client.data.AuthManager
import com.emby.client.data.BaseItemDto
import com.emby.client.player.PlayerActivity
import com.emby.client.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class TvHomeFragment : BrowseSupportFragment() {
    private lateinit var viewModel: MainViewModel
    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = "Emby Client"
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        adapter = rowsAdapter

        // Observe data
        lifecycleScope.launch {
            viewModel.views.collect {
                it?.let { views ->
                    views.Items.forEachIndexed { index, viewDto ->
                        val header = HeaderItem(index.toLong(), viewDto.Name)
                        val adapter = ArrayObjectAdapter(TvMediaItemPresenter())
                        rowsAdapter.add(ListRow(header, adapter))
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.recentlyAdded.collect {
                it?.let { result ->
                    val header = HeaderItem(100, "Recently Added")
                    val adapter = ArrayObjectAdapter(TvMediaItemPresenter())
                    result.Items.forEach { item ->
                        adapter.add(item)
                    }
                    rowsAdapter.add(ListRow(header, adapter))
                }
            }
        }

        lifecycleScope.launch {
            viewModel.resumeItems.collect {
                it?.let { result ->
                    val header = HeaderItem(101, "Resume Watching")
                    val adapter = ArrayObjectAdapter(TvMediaItemPresenter())
                    result.Items.forEach { item ->
                        adapter.add(item)
                    }
                    rowsAdapter.add(ListRow(header, adapter))
                }
            }
        }

        lifecycleScope.launch {
            viewModel.favorites.collect {
                it?.let { result ->
                    val header = HeaderItem(102, "Favorites")
                    val adapter = ArrayObjectAdapter(TvMediaItemPresenter())
                    result.Items.forEach { item ->
                        adapter.add(item)
                    }
                    rowsAdapter.add(ListRow(header, adapter))
                }
            }
        }

        setupEventListeners()
    }

    private fun setupEventListeners() {
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            if (item is BaseItemDto) {
                if (item.IsFolder || item.Type == "Series" || item.Type == "Season") {
                    val intent = Intent(requireContext(), TvDetailsActivity::class.java).apply {
                        putExtra("ITEM_ID", item.Id)
                        putExtra("ITEM_TYPE", item.Type)
                    }
                    startActivity(intent)
                } else {
                    val activeServer = AuthManager.getActiveServer(requireContext())
                    if (activeServer != null) {
                        val streamUrl = "${activeServer.url}/Videos/${item.Id}/stream.mp4?Static=true&api_key=${activeServer.token}"
                        val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
                            putExtra("playbackUrl", streamUrl)
                            putExtra("itemId", item.Id)
                        }
                        startActivity(intent)
                    }
                }
            }
        }
    }
}
