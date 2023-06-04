package fi.sabriina.urbanhuikka

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fi.sabriina.urbanhuikka.player.Player

class LeaderboardListAdapter : ListAdapter<Player, PlayerViewHolder>(PlayersComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        return PlayerViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)

        holder.itemView.setOnClickListener{
        }

    }
}

class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val playerItemView: TextView = itemView.findViewById(R.id.playerName)
    private val pointsItemView: TextView = itemView.findViewById(R.id.points)

    fun bind(player: Player) {
        playerItemView.text = player.name
        pointsItemView.text = player.currentPoints.toString()

    }

    companion object {
        fun create(parent: ViewGroup): PlayerViewHolder {
            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_leaderboard, parent, false)
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


