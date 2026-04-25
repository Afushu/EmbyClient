package com.emby.client.ui.mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.emby.client.R
import com.emby.client.data.BaseItemDto
import com.emby.client.viewmodel.MainViewModel
import com.emby.client.viewmodel.MainViewModelFactory
import kotlinx.coroutines.launch

class MobileHomeFragment : Fragment() {
    private lateinit var viewModel: MainViewModel
    private lateinit var listView: ListView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var tvEmpty: TextView
    private lateinit var mediaAdapter: MediaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mobile_home, container, false)
        listView = view.findViewById(R.id.lvMedia)
        progressBar = view.findViewById(R.id.progressBar)
        tvError = view.findViewById(R.id.tvError)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(
            requireActivity(),
            MainViewModelFactory(requireContext())
        ).get(MainViewModel::class.java)
        
        mediaAdapter = MediaAdapter(requireContext(), emptyList()) { item ->
            // Navigate to details
        }
        listView.adapter = mediaAdapter

        // Observe data
        lifecycleScope.launch {
            viewModel.recentlyAdded.collect {
                updateUI()
            }
        }

        // Observe loading state
        lifecycleScope.launch {
            viewModel.isLoading.collect {
                updateUI()
            }
        }

        // Observe error state
        lifecycleScope.launch {
            viewModel.error.collect {
                updateUI()
            }
        }
    }

    private fun updateUI() {
        val isLoading = viewModel.isLoading.value
        val error = viewModel.error.value
        val recentlyAdded = viewModel.recentlyAdded.value

        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            listView.visibility = View.GONE
            tvError.visibility = View.GONE
            tvEmpty.visibility = View.GONE
        } else if (error != null) {
            progressBar.visibility = View.GONE
            listView.visibility = View.GONE
            tvError.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
            tvError.text = "Error: $error"
        } else if (recentlyAdded?.Items.isNullOrEmpty()) {
            progressBar.visibility = View.GONE
            listView.visibility = View.GONE
            tvError.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
            listView.visibility = View.VISIBLE
            tvError.visibility = View.GONE
            tvEmpty.visibility = View.GONE
            mediaAdapter.updateItems(recentlyAdded!!.Items)
        }
    }

    class MediaAdapter(
        private val context: android.content.Context,
        private var items: List<BaseItemDto>,
        private val onItemClick: (BaseItemDto) -> Unit
    ) : ArrayAdapter<BaseItemDto>(context, R.layout.item_media, items) {

        fun updateItems(newItems: List<BaseItemDto>) {
            items = newItems
            clear()
            addAll(items)
            notifyDataSetChanged()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_media, parent, false)
            val item = items[position]
            val imageView: ImageView = view.findViewById(R.id.ivPoster)
            val titleView: TextView = view.findViewById(R.id.tvTitle)

            titleView.text = item.Name
            // Load poster image using Glide
            // imageView would be loaded from Emby server
            view.setOnClickListener {
                onItemClick(item)
            }

            return view
        }
    }
}
