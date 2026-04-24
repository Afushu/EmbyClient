package com.emby.client.ui.tv

import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.emby.client.R
import com.emby.client.data.BaseItemDto

class CardPresenter(private val serverUrl: String) : Presenter() {
    private var defaultCardImage: Int = R.drawable.ic_launcher

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = object : ImageCardView(parent.context) {
            override fun setSelected(selected: Boolean) {
                super.setSelected(selected)
                updateCardBackgroundColor(this, selected)
            }
        }
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        updateCardBackgroundColor(cardView, false)
        return ViewHolder(cardView)
    }

    private fun updateCardBackgroundColor(view: ImageCardView, selected: Boolean) {
        val color = ContextCompat.getColor(view.context, if (selected) R.color.primary else R.color.neutral10)
        view.infoAreaBackground = null
        view.setBackgroundColor(color)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val dto = item as BaseItemDto
        val cardView = viewHolder.view as ImageCardView

        cardView.titleText = dto.Name
        cardView.contentText = dto.ProductionYear?.toString() ?: dto.Type
        cardView.setMainImageDimensions(300, 450)

        val imageTag = dto.ImageTags?.get("Primary")
        if (imageTag != null) {
            val imageUrl = "$serverUrl/Items/${dto.Id}/Images/Primary?tag=$imageTag&maxWidth=300"
            Glide.with(viewHolder.view.context)
                .load(imageUrl)
                .centerCrop()
                .error(defaultCardImage)
                .into(cardView.mainImageView)
        } else {
            cardView.mainImageView.setImageResource(defaultCardImage)
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        cardView.badgeImage = null
        cardView.mainImage = null
    }
}
