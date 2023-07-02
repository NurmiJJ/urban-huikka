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
import kotlinx.coroutines.launch


const val TAG = "Huikkasofta"
const val DareCollection = "DareCards"
const val TruthCollection = "TruthCards"

class MainActivity : AppCompatActivity() {

    private val database = Firebase.firestore
    private var playerList = listOf<Player>()
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
    private var currentPlayerIndex = 0

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

        CoroutineScope(Dispatchers.Main).launch {
            val gameStatus = gameStateViewModel.getCurrentGame().status
            if (gameStatus == "PLAYER_SELECT") {
                truthCardList.shuffle()
                dareCardList.shuffle()
                gameStateViewModel.updateGameStatus("ONGOING", null)
            }
        }

        setContentView(R.layout.activity_main)
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

        CoroutineScope(Dispatchers.Main).launch {
            playerList = gameStateViewModel.getPlayers()
            playerName.text = playerList[0].name
        }

        gameStateViewModel.currentPlayerIndex.observe(this) { playerNo ->
            currentPlayerIndex = playerNo
            if (playerList.isNotEmpty()) {
                val name = getCurrentPlayer().name
                playerName.text = name
                splashScreenManager.showSplashScreen(name,"Seuraavana vuorossa $name", drawableDrink)
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
        // Points from card
        val points = 3

        splashScreenManager.showSplashScreen(getCurrentPlayer().name,"Ota $points huikkaa!", drawableDrink)
        checkRemainingCards()
        endTurn()
        showTaskButtons()
    }

    private fun cardCompleted() {
        addPoints()
        checkRemainingCards()
        endTurn()
        showTaskButtons()
    }

    private fun endTurn()  {
        gameStateViewModel.updateCurrentPlayerIndex()
    }
    private fun addPoints() {
        val player = getCurrentPlayer()
        // Points from card
        val points = 3

        playerViewModel.updatePoints(player, points)
        splashScreenManager.showSplashScreen(player.name,"Sait $points pistettä!", drawableDrink)
    }

    private fun getCurrentPlayer() : Player {
            return playerList[currentPlayerIndex]
    }

    private fun checkRemainingCards() {
        if (truthCardIndex > truthCardList.size - 1) {
            truthCardIndex = 0
            truthCardList.shuffle()
            splashScreenManager.showSplashScreen(getCurrentPlayer().name,"Totuuskortit pääsivät loppumaan. Voit jatkaa pelaamista, mutta uusia kortteja ei enää ole.", drawableDrink)
            return
        }

        if (dareCardIndex > dareCardList.size - 1) {
            dareCardIndex = 0
            dareCardList.shuffle()
            splashScreenManager.showSplashScreen(getCurrentPlayer().name,"Tehtäväkortit pääsivät loppumaan. Voit jatkaa pelaamista, mutta uusia kortteja ei enää ole.", drawableDrink)
            return
        }
    }
}