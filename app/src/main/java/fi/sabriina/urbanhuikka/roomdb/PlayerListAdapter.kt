package fi.sabriina.urbanhuikka.roomdb

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fi.sabriina.urbanhuikka.R

class PlayerListAdapter(var nextButton: Button) : ListAdapter<Player, PlayerViewHolder>(PlayersComparator()) {
    private var selectedPlayers = mutableListOf<Player>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        return PlayerViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current.name)

        holder.itemView.setOnClickListener{
            holder.playerClicked()
            if (!selectedPlayers.remove(current)){
                nextButton.isEnabled = true
                selectedPlayers.add(current)
            }
            if (selectedPlayers.isEmpty()) {
                nextButton.isEnabled = false
            }
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

        fun playerClicked() {
            playerSelectImage.visibility = if (playerSelectImage.visibility == View.VISIBLE) View.INVISIBLE else View.VISIBLE
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


