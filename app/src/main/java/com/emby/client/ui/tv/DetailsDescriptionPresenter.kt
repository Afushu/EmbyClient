package com.emby.client.ui.tv

import androidx.leanback.widget.AbstractDetailsDescriptionPresenter
import com.emby.client.data.BaseItemDto

class DetailsDescriptionPresenter : AbstractDetailsDescriptionPresenter() {
    override fun onBindDescription(viewHolder: ViewHolder, item: Any) {
        val dto = item as BaseItemDto
        viewHolder.title.text = dto.Name
        viewHolder.subtitle.text = "${dto.ProductionYear ?: ""} | Rating: ${dto.CommunityRating ?: "N/A"}"
        viewHolder.body.text = dto.Overview ?: "No description available."
    }
}
