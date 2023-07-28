package fi.sabriina.urbanhuikka

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import fi.sabriina.urbanhuikka.card.Card
import fi.sabriina.urbanhuikka.roomdb.*
import fi.sabriina.urbanhuikka.roomdb.HuikkaApplication
import fi.sabriina.urbanhuikka.splashScreens.SplashScreenManager
import fi.sabriina.urbanhuikka.viewmodel.GameStateViewModel
import fi.sabriina.urbanhuikka.viewmodel.GameStateViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


const val TAG = "Huikkasofta"
const val TRUTH_DECK = "truth"
const val DARE_DECK = "dare"

class MainActivity : AppCompatActivity() {

    private lateinit var playerName : TextView
    private lateinit var cardView : CustomCard

    private lateinit var selectionTaskButtons : LinearLayout
    private lateinit var selectionButtons : LinearLayout

    private lateinit var truthButton : Button
    private lateinit var dareButton : Button

    private lateinit var skipButton : Button
    private lateinit var completeButton : Button

    private lateinit var leaderboardButton : ImageButton

    private lateinit var drawableDrink : Drawable
    private lateinit var splashScreenManager : SplashScreenManager
    private lateinit var currentPlayer: Player
    private var currentCard: Card? = null

    private val gameStateViewModel: GameStateViewModel by viewModels {
        GameStateViewModelFactory((application as HuikkaApplication).gameStateRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        drawableDrink = ContextCompat.getDrawable(this, R.drawable.drink)!!
        splashScreenManager = SplashScreenManager(this)

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

        truthButton.setOnClickListener {
            drawCard(TRUTH_DECK)
        }

        dareButton.setOnClickListener {
            drawCard(DARE_DECK)
        }

        skipButton.setOnClickListener { cardSkipped() }
        completeButton.setOnClickListener { cardCompleted() }

        leaderboardButton.setOnClickListener {
            val intent = Intent(this@MainActivity, LeaderboardActivity::class.java)
            startActivity(intent)
        }

        CoroutineScope(Dispatchers.Main).launch {
            val gameStatus = gameStateViewModel.getCurrentGame().status
            if (gameStatus in arrayOf("PLAYER_SELECT", "ONGOING")) {
                gameStateViewModel.startGame()
            }
        }

        gameStateViewModel.currentPlayer.observe(this) { player ->
            currentPlayer = player
            playerName.text = currentPlayer.name
            splashScreenManager.showSplashScreen(currentPlayer.name,"Seuraavana vuorossa ${currentPlayer.name}", drawableDrink)
        }

        onBackPressedDispatcher.addCallback(this) {
            splashScreenManager.showPauseDialog { confirmed ->
                if (confirmed) {
                    finish()
                }
            }
        }
    }

    private fun drawCard(deck: String) {
        currentCard = gameStateViewModel.getNextCard(deck)
        if (currentCard != null) {
            cardView.setCard(currentCard!!)
            hideTaskButtons()
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

    private fun cardSkipped() {
        splashScreenManager.showSplashScreen(currentPlayer.name,"Ota ${currentCard!!.points} huikkaa!", drawableDrink)
        endTurn()
    }

    private fun cardCompleted() {
        CoroutineScope(Dispatchers.Main).launch {
            gameStateViewModel.addPoints(amount=currentCard!!.points)
            splashScreenManager.showSplashScreen(currentPlayer.name,"Sait ${currentCard!!.points} pistettä!", drawableDrink)
            endTurn()
        }
    }

    private fun endTurn()  {
        gameStateViewModel.endTurn()
        showTaskButtons()
    }
}