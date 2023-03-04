package fi.sabriina.urbanhuikka

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import fi.sabriina.urbanhuikka.card.Card
import fi.sabriina.urbanhuikka.player.*

class MainActivity : AppCompatActivity() {
    private var playerList = mutableListOf<Player>()
    private lateinit var playerName : TextView
    private lateinit var cardView : CustomCard

    private lateinit var truthButton : Button
    private lateinit var dareButton : Button


    private val playerViewModel: PlayerViewModel by viewModels {
        PlayerViewModelFactory((application as PlayersApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val adapter = PlayerListAdapter()
        playerName = findViewById(R.id.textViewPlayer)
        truthButton = findViewById(R.id.truthButton)
        dareButton = findViewById(R.id.dareButton)
        cardView = findViewById(R.id.cardView)

        playerViewModel.allPlayers.observe(this, Observer { players ->
            playerList = players.toMutableList()
            players?.let { adapter.submitList(it) }
            playerName.text = playerList[0].name
        })

        truthButton.setOnClickListener {
            val card = getCard("truth")
            cardView.setCard(card)
        }

        dareButton.setOnClickListener {
            val card = getCard("dare")
            cardView.setCard(card)
        }

    }

    override fun onStart() {
        super.onStart()

    }
}