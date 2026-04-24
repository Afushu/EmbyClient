package com.emby.client.ui.mobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emby.client.data.BaseItemDto

class MediaAdapter(
    private var items: List<BaseItemDto>,
    private val onItemClick: (BaseItemDto) -> Unit
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    fun updateItems(newItems: List<BaseItemDto>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.ivPoster)
        private val titleView: TextView = itemView.findViewById(R.id.tvTitle)

        fun bind(item: BaseItemDto) {
            titleView.text = item.Name
            // Load poster image using Glide
            // imageView would be loaded from Emby server
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
