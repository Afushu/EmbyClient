package com.emby.client.ui.tv

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.app.DetailsSupportFragmentBackgroundController
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.emby.client.R
import com.emby.client.data.AuthManager
import com.emby.client.data.BaseItemDto
import com.emby.client.network.RetrofitClient
import com.emby.client.player.PlayerActivity
import kotlinx.coroutines.launch

class TvDetailsFragment : DetailsSupportFragment() {

    private lateinit var mAdapter: ArrayObjectAdapter
    private lateinit var mPresenterSelector: ClassPresenterSelector
    private lateinit var mDetailsBackground: DetailsSupportFragmentBackgroundController

    private var itemId: String? = null
    private var parentId: String? = null
    private var itemType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mDetailsBackground = DetailsSupportFragmentBackgroundController(this)

        itemId = arguments?.getString("ITEM_ID")
        itemType = arguments?.getString("ITEM_TYPE")
        parentId = arguments?.getString("PARENT_ID")

        setupPresenter()
        loadData()
        setupEventListeners()
    }

    private fun setupPresenter() {
        mPresenterSelector = ClassPresenterSelector()

        val detailsPresenter = FullWidthDetailsOverviewRowPresenter(DetailsDescriptionPresenter())
        detailsPresenter.backgroundColor = ContextCompat.getColor(requireContext(), R.color.primary)
        detailsPresenter.initialState = FullWidthDetailsOverviewRowPresenter.STATE_HALF
        
        detailsPresenter.setOnActionClickedListener { action ->
            if (action.id == 1L) {
                val server = AuthManager.getActiveServer(requireContext())
                if (server != null && itemId != null) {
                    val streamUrl = "${server.url}/Videos/$itemId/stream.mp4?Static=true&api_key=${server.token}"
                    val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
                        putExtra("VIDEO_URL", streamUrl)
                        putExtra("ITEM_ID", itemId)
                        putExtra("SERVER_URL", server.url)
                        putExtra("TOKEN", server.token)
                        putExtra("USER_ID", server.userId)
                    }
                    startActivity(intent)
                }
            } else if (action.id == 2L) {
                // Browse Episodes Action - We can handle showing rows below
            }
        }

        mPresenterSelector.addClassPresenter(DetailsOverviewRow::class.java, detailsPresenter)
        mPresenterSelector.addClassPresenter(ListRow::class.java, ListRowPresenter())
        mAdapter = ArrayObjectAdapter(mPresenterSelector)
        adapter = mAdapter
    }

    private fun loadData() {
        val server = AuthManager.getActiveServer(requireContext()) ?: return
        val api = RetrofitClient.getClient(server.url)
        val authHeader = "MediaBrowser Client=\"EmbyClient\", Device=\"Android\", DeviceId=\"12345\", Version=\"1.0.0\", Token=\"${server.token}\""

        lifecycleScope.launch {
            try {
                // Fetch details
                val response = api.getItems(
                    userId = server.userId,
                    parentId = parentId,
                    recursive = true,
                    authHeader = authHeader
                )
                val item = response.Items.find { it.Id == itemId }
                if (item != null) {
                    val row = DetailsOverviewRow(item)
                    
                    val primaryTag = item.ImageTags?.get("Primary")
                    if (primaryTag != null) {
                        Glide.with(requireContext())
                            .asBitmap()
                            .load("${server.url}/Items/${item.Id}/Images/Primary?tag=$primaryTag&maxWidth=300")
                            .into(object : SimpleTarget<Bitmap>() {
                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    row.setImageBitmap(requireContext(), resource)
                                }
                            })
                    }

                    val backdropTag = item.ImageTags?.get("Backdrop")
                    if (backdropTag != null) {
                        Glide.with(requireContext())
                            .asBitmap()
                            .load("${server.url}/Items/${item.Id}/Images/Backdrop?tag=$backdropTag&maxWidth=1920")
                            .into(object : SimpleTarget<Bitmap>() {
                                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                    mDetailsBackground.coverBitmap = resource
                                    mDetailsBackground.enableParallax()
                                }
                            })
                    }

                    val actionAdapter = ArrayObjectAdapter()
                    if (item.IsFolder || item.Type == "Series" || item.Type == "Season") {
                        actionAdapter.add(Action(2L, "Browse Episodes"))
                        loadEpisodes(item.Id, server.url, server.token, server.userId)
                    } else {
                        actionAdapter.add(Action(1L, "Play"))
                    }
                    row.actionsAdapter = actionAdapter
                    mAdapter.add(row)
                }

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading details", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadEpisodes(parentId: String, url: String, token: String, userId: String) {
        val api = RetrofitClient.getClient(url)
        val authHeader = "MediaBrowser Client=\"EmbyClient\", Device=\"Android\", DeviceId=\"12345\", Version=\"1.0.0\", Token=\"$token\""

        lifecycleScope.launch {
            try {
                val response = api.getItems(
                    userId = userId,
                    parentId = parentId,
                    authHeader = authHeader
                )
                if (response.Items.isNotEmpty()) {
                    val listRowAdapter = ArrayObjectAdapter(CardPresenter(url))
                    response.Items.forEach { listRowAdapter.add(it) }
                    val header = HeaderItem(0, "Episodes")
                    mAdapter.add(ListRow(header, listRowAdapter))
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun setupEventListeners() {
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            if (item is BaseItemDto) {
                if (item.IsFolder || item.Type == "Season") {
                    val intent = Intent(requireContext(), TvDetailsActivity::class.java).apply {
                        putExtra("ITEM_ID", item.Id)
                        putExtra("ITEM_TYPE", item.Type)
                        putExtra("PARENT_ID", item.ParentIndexNumber?.toString() ?: "")
                    }
                    startActivity(intent)
                } else {
                    val server = AuthManager.getActiveServer(requireContext())
                    if (server != null) {
                        val streamUrl = "${server.url}/Videos/${item.Id}/stream.mp4?Static=true&api_key=${server.token}"
                        val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
                            putExtra("VIDEO_URL", streamUrl)
                            putExtra("ITEM_ID", item.Id)
                            putExtra("SERVER_URL", server.url)
                            putExtra("TOKEN", server.token)
                            putExtra("USER_ID", server.userId)
                        }
                        startActivity(intent)
                    }
                }
            }
        }
    }
}
