package com.emby.client.ui.login

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emby.client.data.ServerProfile

class ServerAdapter(
    private var servers: List<ServerProfile>,
    private var activeServerId: String?,
    private val onServerSelect: (ServerProfile) -> Unit,
    private val onServerEdit: (ServerProfile) -> Unit,
    private val onServerRemove: (ServerProfile) -> Unit
) : RecyclerView.Adapter<ServerAdapter.ServerViewHolder>() {

    fun updateServers(newServers: List<ServerProfile>, newActiveServerId: String?) {
        servers = newServers
        activeServerId = newActiveServerId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return ServerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) {
        val server = servers[position]
        holder.bind(server, server.id == activeServerId)
    }

    override fun getItemCount(): Int = servers.size

    inner class ServerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(android.R.id.text1)
        private val subtitle = itemView.findViewById<TextView>(android.R.id.text2)

        fun bind(server: ServerProfile, isActive: Boolean) {
            title.text = server.url
            subtitle.text = if (isActive) "Active" else ""

            itemView.setOnClickListener {
                onServerSelect(server)
            }

            itemView.setOnLongClickListener {
                onServerEdit(server)
                true
            }
        }
    }
}
