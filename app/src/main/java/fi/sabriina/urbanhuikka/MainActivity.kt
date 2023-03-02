package fi.sabriina.urbanhuikka

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import fi.sabriina.urbanhuikka.player.*

class MainActivity : AppCompatActivity() {
    private var playerList = mutableListOf<Player>()
    private lateinit var playerName : TextView



    private val playerViewModel: PlayerViewModel by viewModels {
        PlayerViewModelFactory((application as PlayersApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val adapter = PlayerListAdapter()
        playerName = findViewById(R.id.textViewPlayer)

        playerViewModel.allPlayers.observe(this, Observer { players ->
            playerList = players.toMutableList()
            players?.let { adapter.submitList(it) }
            playerName.text = playerList[0].name
        })



    }

    override fun onStart() {
        super.onStart()

    }
}