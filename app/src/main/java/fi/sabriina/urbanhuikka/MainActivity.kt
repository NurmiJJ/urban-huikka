package fi.sabriina.urbanhuikka

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
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

    private lateinit var selectionTaskButtons : LinearLayout
    private lateinit var selectionButtons : LinearLayout


    private lateinit var truthButton : Button
    private lateinit var dareButton : Button

    private lateinit var skipButton : Button
    private lateinit var completeButton : Button

    var truthCardList = mutableListOf<Card>()
    var dareCardList = mutableListOf<Card>()

    private val model: StatusViewModel by viewModels()


    private val playerViewModel: PlayerViewModel by viewModels {
        PlayerViewModelFactory((application as PlayersApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val adapter = PlayerListAdapter()
        playerName = findViewById(R.id.textViewPlayer)

        //Buttons
        truthButton = findViewById(R.id.truthButton)
        dareButton = findViewById(R.id.dareButton)
        skipButton = findViewById(R.id.skipButton)
        completeButton = findViewById(R.id.completeButton)


        cardView = findViewById(R.id.cardView)
        selectionButtons = findViewById(R.id.selectionButtons)
        selectionTaskButtons = findViewById(R.id.selectionTaskButtons)

        updateDatabase()

        playerViewModel.allPlayers.observe(this, Observer { players ->
            playerList = players.toMutableList()
            players?.let { adapter.submitList(it) }
            model.currentPlayer.value = 0
        })

        model.currentPlayer.observe(this) { playerNo ->
            playerName.text = playerList[playerNo].name
        }


        truthButton.setOnClickListener {
            val card = truthCardList.random()
            cardView.setCard(card)
            hideTaskButtons()
        }

        dareButton.setOnClickListener {
            val card = dareCardList.random()
            cardView.setCard(card)
            hideTaskButtons()
        }

        skipButton.setOnClickListener { cardSkipped() }
        completeButton.setOnClickListener { cardCompleted() }

    }

    override fun onStart() {
        super.onStart()

    }

    private fun hideTaskButtons(){
        selectionTaskButtons.visibility = View.INVISIBLE
        selectionButtons.visibility = View.VISIBLE

    }

    private fun showTaskButtons(){
        cardView.setBackside()
        selectionTaskButtons.visibility = View.VISIBLE
        selectionButtons.visibility = View.INVISIBLE

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

    private fun cardSkipped() {
        showTaskButtons()
        endTurn()
    }

    private fun cardCompleted() {
        showTaskButtons()
        endTurn()
    }

    private fun endTurn() {
        val currentNo = model.currentPlayer.value

        if (currentNo != null) {
            if (currentNo + 1 == playerList.size){
                model.currentPlayer.value = 0
            } else {
                model.currentPlayer.value = currentNo + 1
            }

        }
    }



}