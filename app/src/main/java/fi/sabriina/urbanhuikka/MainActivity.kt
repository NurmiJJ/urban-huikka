package fi.sabriina.urbanhuikka

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

    private val model: StatusViewModel by viewModels()


    private val playerViewModel: PlayerViewModel by viewModels {
        PlayerViewModelFactory((application as HuikkaApplication).playerRepository)
    }

    private val gameStateViewModel: GameStateViewModel by viewModels {
        GameStateViewModelFactory((application as HuikkaApplication).gameStateRepository)
    }

    private lateinit var drawableDrink : Drawable

    private lateinit var splashScreenManager : SplashScreenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        drawableDrink = ContextCompat.getDrawable(this, R.drawable.drink)!!

        splashScreenManager = SplashScreenManager(this)

        val gameStatusObserver = Observer<GameState> {gameState ->
            if (gameState.status == "STARTING") {

                truthCardList.shuffle()
                dareCardList.shuffle()
                gameStateViewModel.updateGameStatus( "ONGOING")
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
        // Points from card
        val points = 3

        splashScreenManager.showSplashScreen(getCurrentPlayer().name,"Ota $points huikkaa!", drawableDrink)
        showTaskButtons()
        endTurn()
    }

    private fun cardCompleted() {
        addPoints()
        endTurn()
        showTaskButtons()
    }

    private fun endTurn()  {
        val currentNo = model.currentPlayer.value!!

        if (currentNo + 1 < playerList.size){
            model.currentPlayer.value = currentNo + 1

        } else {
            model.currentPlayer.value = 0
        }
        val name = getCurrentPlayer().name
        playerName.text = name
        splashScreenManager.showSplashScreen(name,"Seuraavana vuorossa $name", drawableDrink)
    }
    private fun addPoints() {
        val player = getCurrentPlayer()
        // Points from card
        val points = 3

        playerViewModel.updatePoints(player, points)
        splashScreenManager.showSplashScreen(player.name,"Sait $points pistettä!", drawableDrink)
    }

    private fun getCurrentPlayer() : Player {
        val currentPlayerIndex = model.currentPlayer.value
        return playerList[currentPlayerIndex!!]
    }
}