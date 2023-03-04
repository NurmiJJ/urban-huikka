package fi.sabriina.urbanhuikka

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import fi.sabriina.urbanhuikka.card.Card
import fi.sabriina.urbanhuikka.player.*

const val TAG = "Huikkasofta"
const val DareCollection = "DareCards"
const val TruthCollection = "TruthCards"

class MainActivity : AppCompatActivity() {

    private val database = Firebase.firestore
    private var playerList = mutableListOf<Player>()
    private lateinit var playerName : TextView
    private lateinit var cardView : CustomCard

    private lateinit var truthButton : Button
    private lateinit var dareButton : Button

    var truthCardList = mutableListOf<Card>()
    var dareCardList = mutableListOf<Card>()


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

        updateDatabase()

        playerViewModel.allPlayers.observe(this, Observer { players ->
            playerList = players.toMutableList()
            players?.let { adapter.submitList(it) }
            playerName.text = playerList[0].name
        })

        truthButton.setOnClickListener {
            val card = truthCardList.random()
            cardView.setCard(card)
        }

        dareButton.setOnClickListener {
            val card = dareCardList.random()
            cardView.setCard(card)
        }

    }

    override fun onStart() {
        super.onStart()

    }

    private fun updateDatabase() {
        database.collection(TruthCollection)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }
                truthCardList.clear()
                Log.d(TAG, "täällä ollaan")
                for (doc in value!!) {
                    val card : Card = doc.toObject(Card::class.java)
                    truthCardList.add(card)


                }
            }

        database.collection(DareCollection)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }
                dareCardList.clear()
                for (doc in value!!) {
                    val card : Card = doc.toObject(Card::class.java)
                    dareCardList.add(card)


                }
            }
    }

}