package fi.sabriina.urbanhuikka

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import fi.sabriina.urbanhuikka.card.Card
import fi.sabriina.urbanhuikka.roomdb.*
import fi.sabriina.urbanhuikka.roomdb.HuikkaApplication
import fi.sabriina.urbanhuikka.roomdb.viewmodel.GameStateViewModel
import fi.sabriina.urbanhuikka.roomdb.viewmodel.GameStateViewModelFactory
import fi.sabriina.urbanhuikka.roomdb.viewmodel.PlayerViewModel
import fi.sabriina.urbanhuikka.roomdb.viewmodel.PlayerViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


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

    private lateinit var leaderboardButton : ImageButton

    private var truthCardList = mutableListOf<Card>()
    private var dareCardList = mutableListOf<Card>()

    private var truthCardIndex = 0
    private var dareCardIndex = 0

    private val model: StatusViewModel by viewModels()

    private val playerViewModel: PlayerViewModel by viewModels {
        PlayerViewModelFactory((application as HuikkaApplication).playerRepository)
    }

    private val gameStateViewModel: GameStateViewModel by viewModels {
        GameStateViewModelFactory((application as HuikkaApplication).gameStateRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gameStatusObserver = Observer<GameState> {gameState ->
            if (gameState.status == "STARTING") {

                truthCardList.shuffle()
                dareCardList.shuffle()
                gameStateViewModel.updateGameStatus("ONGOING")
            }
        }

        gameStateViewModel.gameStatus.observe(this, gameStatusObserver)

        setContentView(R.layout.activity_main)
        val adapter = PlayerListAdapter()
        playerName = findViewById(R.id.textViewPlayer)

        //Buttons
        truthButton = findViewById(R.id.truthButton)
        dareButton = findViewById(R.id.dareButton)
        skipButton = findViewById(R.id.skipButton)
        completeButton = findViewById(R.id.completeButton)
        leaderboardButton = findViewById(R.id.leaderboardButton)

        cardView = findViewById(R.id.cardView)
        selectionButtons = findViewById(R.id.selectionButtons)
        selectionTaskButtons = findViewById(R.id.selectionTaskButtons)

        updateDatabase()

        playerViewModel.allPlayers.observe(this) { players ->
            if (playerList.size == 0) {
                playerName.text = players[0].name
            }
            playerList = players.toMutableList()
            players?.let { adapter.submitList(it) }
        }

        model.currentPlayer.observe(this) { playerNo ->
            if (playerList.size > 0) {
                val name = playerList[playerNo].name
                playerName.text = name

                showDialog(this, name, getString(R.string.truthOrDate) )
            }
        }

        truthButton.setOnClickListener {
            val card = truthCardList[truthCardIndex]
            cardView.setCard(card)
            hideTaskButtons()
            truthCardIndex++
        }

        dareButton.setOnClickListener {
            val card = dareCardList[dareCardIndex]
            cardView.setCard(card)
            hideTaskButtons()
            dareCardIndex++
        }

        skipButton.setOnClickListener { cardSkipped() }
        completeButton.setOnClickListener { cardCompleted() }

        leaderboardButton.setOnClickListener {
            val intent = Intent(this@MainActivity, LeaderboardActivity::class.java)
            startActivity(intent)
        }
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
        val currentNo = model.currentPlayer.value!!
        val name = playerList[currentNo].name
        // Points from card
        val points = 3

        showDialog(this, name,"Ota $points huikkaa!")
        showTaskButtons()
        CoroutineScope(Dispatchers.Main).launch {
            delay(2500)
            checkRemainingCards()
        }
    }

    private fun cardCompleted() {
        addPoints()
        showTaskButtons()
        CoroutineScope(Dispatchers.Main).launch {
            delay(2500)
            checkRemainingCards()
        }
    }

    private fun endTurn()  {
        val currentNo = model.currentPlayer.value!!

        if (currentNo + 1 < playerList.size){
            model.currentPlayer.value = currentNo + 1

        } else {
            model.currentPlayer.value = 0
        }
    }
    private fun addPoints() {
        val currentNo = model.currentPlayer.value
        val player = playerList[currentNo!!]
        // Points from card
        val points = 3

        playerViewModel.updatePoints(player, points)
        showDialog(this, player.name,"Sait $points pistettä!" )
    }

    private fun checkRemainingCards() {
        if (truthCardIndex > truthCardList.size - 1) {
            truthCardIndex = 0
            truthCardList.shuffle()
            showDialog(this,"Sekoitetaan pakka","Totuuskortit pääsivät loppumaan. Voit jatkaa pelaamista, mutta uusia kortteja ei enää ole.", 7500)
            CoroutineScope(Dispatchers.Main).launch {
                delay(8000)
                endTurn()
            }
            return
        }

        if (dareCardIndex > dareCardList.size - 1) {
            dareCardIndex = 0
            dareCardList.shuffle()
            showDialog(this,"Sekoitetaan pakka","Tehtäväkortit pääsivät loppumaan. Voit jatkaa pelaamista, mutta uusia kortteja ei enää ole.", 7500)
            CoroutineScope(Dispatchers.Main).launch {
                delay(8000)
                endTurn()
            }
            return
        }
        endTurn()
    }
}