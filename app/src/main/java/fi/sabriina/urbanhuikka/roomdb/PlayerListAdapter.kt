package fi.sabriina.urbanhuikka.roomdb

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fi.sabriina.urbanhuikka.R
import fi.sabriina.urbanhuikka.helpers.SfxPlayer

class PlayerListAdapter(context: Context, private val  listType : String, private val button: Button) : ListAdapter<Player, PlayerViewHolder>(PlayersComparator()) {
    private var selectedPlayers = mutableListOf<Player>()
    private var selecting = false
    private val sfxPlayer = SfxPlayer(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {

        return PlayerViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current.name, current.pictureResId)

        // Show selection
        holder.playerSelected(selectedPlayers.contains(current))

        if (listType == "SELECT") {
            holder.itemView.setOnClickListener{
                holder.playerClicked()
                clicked(current, 1)
            }
        }

        if (listType == "MANAGE") {
            holder.itemView.setOnLongClickListener {
                holder.playerClicked()
                clicked(current, 0)
                true
            }

            holder.itemView.setOnClickListener{
                if (selecting) {
                    holder.playerClicked()
                    clicked(current, 0)
                }
                else {
                    // ("Show player profile")
                }

            }
        }

    }
    fun getSelected():MutableList<Player> {
        return selectedPlayers
    }

    fun emptySelected() {
        selectedPlayers.clear()
        selecting = false
        button.isEnabled = false
    }

    private fun clicked(player: Player, disableSize: Int) {
        sfxPlayer.playButtonClickSound()
        if (!selectedPlayers.remove(player)){
            button.isEnabled = true
            selecting = true
            selectedPlayers.add(player)
        }
        if (selectedPlayers.size <= disableSize) {
            button.isEnabled = false
            selecting = false
        }
    }
}

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerItemView: TextView = itemView.findViewById(R.id.textViewPlayerName)
        private val playerProfilePicture : ImageView = itemView.findViewById(R.id.profilePicture)
        private val playerSelectImage : ImageView = itemView.findViewById(R.id.selectImageView)

        fun bind(text: String?, pictureResId: Int) {
            playerItemView.text = text
            val drawable = ContextCompat.getDrawable(itemView.context, pictureResId)!!
            playerProfilePicture.setImageDrawable(drawable)
        }

        fun playerClicked() {
             playerSelected(playerSelectImage.visibility == View.INVISIBLE)
        }

        fun playerSelected(boolean: Boolean) {
            if (boolean) {
                playerSelectImage.visibility = View.VISIBLE
                val theme = ContextCompat.getDrawable(itemView.context, R.drawable.player_list_theme_selected)
                playerItemView.background = theme
            }
            else {
                playerSelectImage.visibility = View.INVISIBLE
                val theme = ContextCompat.getDrawable(itemView.context, R.drawable.player_list_theme)
                playerItemView.background = theme
            }

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


