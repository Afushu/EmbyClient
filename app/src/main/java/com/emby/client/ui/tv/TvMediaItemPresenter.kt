package com.emby.client.ui.tv

import android.view.ViewGroup
import androidx.leanback.widget.Presenter
import com.emby.client.data.BaseItemDto
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class TvMediaItemPresenter : Presenter() {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tv_media, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val mediaItem = item as BaseItemDto
        val view = viewHolder.view
        val titleView = view.findViewById<TextView>(R.id.tvTitle)
        val imageView = view.findViewById<ImageView>(R.id.ivPoster)

        titleView.text = mediaItem.Name
        // Load poster image using Glide
        // imageView would be loaded from Emby server
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        // Cleanup resources
    }
}
