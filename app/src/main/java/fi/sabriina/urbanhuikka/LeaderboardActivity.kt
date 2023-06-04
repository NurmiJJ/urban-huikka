package fi.sabriina.urbanhuikka

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fi.sabriina.urbanhuikka.player.Player
import fi.sabriina.urbanhuikka.player.PlayerViewModel
import fi.sabriina.urbanhuikka.player.PlayerViewModelFactory
import fi.sabriina.urbanhuikka.player.PlayersApplication
import java.util.Collections

class LeaderboardActivity : AppCompatActivity() {

    private lateinit var adapter: LeaderboardListAdapter

    private val model: PlayerViewModel by viewModels {
        PlayerViewModelFactory((application as PlayersApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview_leaderboard)
        adapter = LeaderboardListAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        model.allPlayers.observe(this) { players ->
            val sortedlist: List<Player> = players.sortedWith(compareBy({it.currentPoints}, {it.name})).reversed()
            sortedlist.let { adapter.submitList(it) }
        }
    }
}