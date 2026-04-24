package com.emby.client.ui.mobile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.emby.client.R
import com.emby.client.data.BaseItemDto

class MobileAdapter(
    private val serverUrl: String,
    private val onItemClick: (BaseItemDto) -> Unit
) : RecyclerView.Adapter<MobileAdapter.ViewHolder>() {

    private val items = mutableListOf<BaseItemDto>()

    fun submitList(newItems: List<BaseItemDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mobile_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, serverUrl)
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount() = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPoster: ImageView = itemView.findViewById(R.id.ivPoster)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)

        fun bind(item: BaseItemDto, serverUrl: String) {
            tvTitle.text = item.Name
            val tag = item.ImageTags?.get("Primary")
            if (tag != null) {
                val url = "$serverUrl/Items/${item.Id}/Images/Primary?tag=$tag&maxWidth=400"
                Glide.with(itemView.context).load(url).into(ivPoster)
            } else {
                ivPoster.setImageResource(R.drawable.ic_launcher)
            }
        }
    }
}
