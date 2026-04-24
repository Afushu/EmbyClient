package com.emby.client.ui.tv

import android.os.Bundle
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.ArrayObjectAdapter
import androidx.leanback.widget.HeaderItem
import androidx.leanback.widget.ListRow
import androidx.leanback.widget.ListRowPresenter
import androidx.lifecycle.ViewModelProvider
import com.emby.client.viewmodel.MainViewModel

class TvHomeFragment : BrowseSupportFragment() {
    private lateinit var viewModel: MainViewModel
    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        title = "Emby Client"
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true

        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        // Observe data
        viewModel.recentlyAdded.observe(viewLifecycleOwner) {
            it?.let {
                val header = HeaderItem(0, "Recently Added")
                val adapter = ArrayObjectAdapter(TvMediaItemPresenter())
                it.Items.forEach { item ->
                    adapter.add(item)
                }
                rowsAdapter.add(ListRow(header, adapter))
                adapter = rowsAdapter
            }
        }

        viewModel.resumeItems.observe(viewLifecycleOwner) {
            it?.let {
                val header = HeaderItem(1, "Resume Watching")
                val adapter = ArrayObjectAdapter(TvMediaItemPresenter())
                it.Items.forEach { item ->
                    adapter.add(item)
                }
                rowsAdapter.add(ListRow(header, adapter))
                adapter = rowsAdapter
            }
        }
    }
}
