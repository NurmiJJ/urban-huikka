package fi.sabriina.urbanhuikka.roomdb

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fi.sabriina.urbanhuikka.R

class PlayerListAdapter : ListAdapter<Player, PlayerViewHolder>(PlayersComparator()) {
    var selectedPlayers = mutableListOf<Player>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        return PlayerViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current.name)

        holder.itemView.setOnClickListener{
            if (!selectedPlayers.remove(current)){
                selectedPlayers.add(current)
            }
            holder.playerClicked(current)
        }

    }
    fun getSelected():MutableList<Player> {
        return selectedPlayers
    }
}

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerItemView: TextView = itemView.findViewById(R.id.textViewPlayerName)
        private val playerSelectImage : ImageView = itemView.findViewById(R.id.selectImageView)

        fun bind(text: String?) {
            playerItemView.text = text

        }

        fun playerClicked(player: Player) {
            // Is there better way to do this??
            if (player.selected) {
                playerSelectImage.visibility = View.INVISIBLE
            } else {
                playerSelectImage.visibility = View.VISIBLE
            }
            player.selected = !player.selected
        }

        companion object {
            fun create(parent: ViewGroup): PlayerViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_player, parent, false)
                return PlayerViewHolder(view)
            }
        }
    }





    class PlayersComparator : DiffUtil.ItemCallback<Player>() {
        override fun areItemsTheSame(oldItem: Player, newItem: Player): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Player, newItem: Player): Boolean {
            return oldItem.name == newItem.name
        }
    }


