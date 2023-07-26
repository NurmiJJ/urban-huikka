package fi.sabriina.urbanhuikka

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fi.sabriina.urbanhuikka.roomdb.PlayerAndScore

class LeaderboardListAdapter : ListAdapter<PlayerAndScore, PlayerViewHolder>(PlayerScoreComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        return PlayerViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val result = getItem(position)
        holder.bind(result)

        holder.itemView.setOnClickListener{
        }

    }
}

class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val playerItemView: TextView = itemView.findViewById(R.id.playerName)
    private val pointsItemView: TextView = itemView.findViewById(R.id.points)

    fun bind(result: PlayerAndScore) {
        playerItemView.text = result.player.name
        pointsItemView.text = result.score.toString()

    }

    companion object {
        fun create(parent: ViewGroup): PlayerViewHolder {
            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_leaderboard, parent, false)
            return PlayerViewHolder(view)
        }
    }
}

class PlayerScoreComparator : DiffUtil.ItemCallback<PlayerAndScore>() {
    override fun areItemsTheSame(oldItem: PlayerAndScore, newItem: PlayerAndScore): Boolean {
        return oldItem.player.id == newItem.player.id
    }

    override fun areContentsTheSame(oldItem: PlayerAndScore, newItem: PlayerAndScore): Boolean {
        return oldItem == newItem
    }
}


