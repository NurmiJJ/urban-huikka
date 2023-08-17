package fi.sabriina.urbanhuikka

import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import fi.sabriina.urbanhuikka.card.Card
import fi.sabriina.urbanhuikka.helpers.SfxPlayer
import fi.sabriina.urbanhuikka.roomdb.HuikkaApplication
import fi.sabriina.urbanhuikka.roomdb.Player
import fi.sabriina.urbanhuikka.splashScreens.SplashScreenManager
import fi.sabriina.urbanhuikka.viewmodel.GameStateViewModel
import fi.sabriina.urbanhuikka.viewmodel.GameStateViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


const val TAG = "Huikkasofta"
const val TRUTH_DECK = "truth"
const val DARE_DECK = "dare"
const val DUAL_CARD_TYPE = "Sinä ja minä"

class MainActivity : BaseActivity(), OnCardSwipeListener {

    private lateinit var playerName: TextView
    private lateinit var playerPicture: ImageView
    private lateinit var cardView: CustomCard

    private lateinit var selectionButtons: FrameLayout

    private lateinit var swipeButton: SeekBar
    private lateinit var guideCheckmark: ImageView
    private lateinit var guideCross: ImageView
    private lateinit var guideBar: ImageView
    private lateinit var guideCompleteText: TextView
    private lateinit var guideSkipText: TextView

    private lateinit var titleText: TextView

    private lateinit var leaderboardButton: ImageButton

    private lateinit var drawablePint : Drawable
    private lateinit var drawableNextPlayer : Drawable
    private lateinit var drawableAddPoints : Drawable
    private lateinit var drawableWin : Drawable
    private lateinit var drawableBeer: Drawable
    private lateinit var splashScreenManager : SplashScreenManager
    private lateinit var currentPlayer: Player
    private lateinit var currentPlayerPicture: Drawable
    private var currentCard: Card? = null

    private val sfxPlayer = SfxPlayer(this)

    private val gameStateViewModel: GameStateViewModel by viewModels {
        GameStateViewModelFactory((application as HuikkaApplication).gameStateRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        drawablePint = ContextCompat.getDrawable(this, R.drawable.glass_mug_variant)!!
        drawableNextPlayer = ContextCompat.getDrawable(this, R.drawable.account_arrow_right)!!
        drawableAddPoints = ContextCompat.getDrawable(this, R.drawable.counter)!!
        drawableWin = ContextCompat.getDrawable(this, R.drawable.podium_gold)!!
        drawableBeer = ContextCompat.getDrawable(this, R.drawable.beer)!!
        splashScreenManager = SplashScreenManager(this)

        setContentView(R.layout.activity_main)

        playerName = findViewById(R.id.textViewPlayer)
        playerPicture = findViewById(R.id.playerPicture)

        //Buttons
        leaderboardButton = findViewById(R.id.leaderboardButton)

        cardView = findViewById(R.id.cardView)
        cardView.setOnCardSwipeListener(this)
        selectionButtons = findViewById(R.id.swipeSelectorLayout)

        swipeButton = findViewById(R.id.swipeSelector)
        guideBar = findViewById(R.id.guideBar)
        guideCheckmark = findViewById(R.id.guideCheckmark)
        guideCross = findViewById(R.id.guideCross)
        guideSkipText = findViewById(R.id.guideSkipText)
        guideCompleteText = findViewById(R.id.guideCompleteText)

        titleText = findViewById(R.id.titleText)

        swipeButton.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            var feedbackGiven = false
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                guideBar.visibility = View.INVISIBLE
                guideCross.visibility = View.INVISIBLE
                guideCheckmark.visibility = View.INVISIBLE
                if ((progress < 15 || progress > 85) && !feedbackGiven) {
                    // make haptic feedback stronger
                    for (i in 1..2) {
                        seekBar.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    }
                    feedbackGiven = true
                } else if (progress in 15..85) {
                    feedbackGiven = false
                }
                if (progress < 50) {
                    seekBar.progressDrawable =
                        ContextCompat.getDrawable(applicationContext, R.drawable.swipe_bar_red)
                    guideSkipText.visibility = View.VISIBLE
                    guideCompleteText.visibility = View.INVISIBLE
                } else if (progress > 50) {
                    seekBar.progressDrawable =
                        ContextCompat.getDrawable(applicationContext, R.drawable.swipe_bar_green)
                    guideCompleteText.visibility = View.VISIBLE
                    guideSkipText.visibility = View.INVISIBLE
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                var action = false
                if (seekBar.progress < 15) {
                    cardSkipped()
                    action = true

                } else if (seekBar.progress > 85) {
                    cardCompleted()
                    action = true
                }
                CoroutineScope(Dispatchers.Main).launch {
                    // prevent view from updating before splash screen is showing
                    if (action) {
                        delay(200)
                    }
                    seekBar.progressDrawable = null
                    seekBar.background = null
                    seekBar.progress = 50
                    guideBar.visibility = View.VISIBLE
                    guideCheckmark.visibility = View.VISIBLE
                    guideCross.visibility = View.VISIBLE
                    guideSkipText.visibility = View.INVISIBLE
                    guideCompleteText.visibility = View.INVISIBLE
                }
            }
        })

        leaderboardButton.setOnClickListener {
            val intent = Intent(this@MainActivity, LeaderboardActivity::class.java)
            startActivity(intent)
        }

        CoroutineScope(Dispatchers.Main).launch {
            val gameStatus = gameStateViewModel.getCurrentGame().status
            if (gameStatus == "PLAYER_SELECT") {
                gameStateViewModel.startGame()
            } else if (gameStatus == "SAVED") {
                gameStateViewModel.continueGame()
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            val selectedCard = gameStateViewModel.getSelectedCard()
            if (selectedCard != null) {
                currentCard = selectedCard

                var assistingPlayer = ""
                if (currentCard!!.category == DUAL_CARD_TYPE) {
                    assistingPlayer = gameStateViewModel.drawAssistingPlayer().name
                }

                cardView.setCard(selectedCard, assistingPlayer)
                cardView.setCardSide(false)

                ObjectAnimator.ofFloat(selectionButtons, View.ALPHA, 1f).start()
                selectionButtons.visibility = View.VISIBLE

            } else {
                ObjectAnimator.ofFloat(titleText, View.ALPHA, 1f).start()
                titleText.visibility = View.VISIBLE
            }
        }

        gameStateViewModel.currentPlayer.observe(this) { player ->
            currentPlayer = player
            playerName.text = currentPlayer.name

            currentPlayerPicture = ContextCompat.getDrawable(this, currentPlayer.pictureResId)!!
            playerPicture.setImageDrawable(currentPlayerPicture)

            splashScreenManager.showSplashScreen(
                currentPlayer.name,
                currentPlayerPicture,
                "Seuraavana vuorossa ${currentPlayer.name}",
                drawableNextPlayer
            )
        }

        onBackPressedDispatcher.addCallback(this) {
            splashScreenManager.showPauseDialog { confirmed ->
                if (confirmed) {
                    CoroutineScope(Dispatchers.Main).launch {
                        gameStateViewModel.updateGameStatus("SAVED")
                        finish()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        CoroutineScope(Dispatchers.Main).launch {
            if (gameStateViewModel.getCurrentGame().status == "SAVED") {
                gameStateViewModel.updateGameStatus("ONGOING")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        splashScreenManager.dismissAllSplashScreens()

    }

    override fun onStop() {
        super.onStop()
        CoroutineScope(Dispatchers.Main).launch {
            if (gameStateViewModel.getCurrentGame().status == "ONGOING") {
                gameStateViewModel.updateGameStatus("SAVED")
            }
        }

    }

    override fun onSwipeRight() {
        sfxPlayer.playCardDrawSound()
        drawCard(TRUTH_DECK)
    }

    override fun onSwipeLeft() {
        sfxPlayer.playCardDrawSound()
        drawCard(DARE_DECK)
    }

    private fun drawCard(deck: String) {
        CoroutineScope(Dispatchers.Main).launch {
            currentCard = gameStateViewModel.getNextCard(deck)
            if (currentCard != null) {

                var assistingPlayer = ""
                if (currentCard!!.category == DUAL_CARD_TYPE) {
                    assistingPlayer = gameStateViewModel.drawAssistingPlayer().name
                }

                cardView.setCard(currentCard!!, assistingPlayer)
                ObjectAnimator.ofFloat(titleText, View.ALPHA, 0f).start()
                delay(250)
                ObjectAnimator.ofFloat(selectionButtons, View.ALPHA, 1f).start()
                selectionButtons.visibility = View.VISIBLE
            }
            if (currentCard?.time != 0) {
                sfxPlayer.playTimerTickingSound()
                splashScreenManager.showCountdownDialog {
                    cardView.startCountdownTimer {
                        sfxPlayer.playTimesUpSound()
                        splashScreenManager.showConfirmDialog(
                            getString(R.string.times_up),
                            drawableBeer,
                            okText = getString(R.string.continue_),
                            cancelText = ""
                        ) {}
                    }
                }
            }
        }
    }

    private fun cardSkipped() {
        CoroutineScope(Dispatchers.Main).launch {
            val assistingPlayer = gameStateViewModel.getAssistingPlayer()
            val assistingPlayerPicture = ContextCompat.getDrawable(this@MainActivity, currentPlayer.pictureResId)!!
            sfxPlayer.playSkipCardSound()
            splashScreenManager.showSplashScreen(
            currentPlayer.name,
            currentPlayerPicture,
            "Ota ${currentCard!!.points} huikkaa!",
            drawableBeer
            )

            if (currentCard!!.category == DUAL_CARD_TYPE) {
                splashScreenManager.showSplashScreen(
                    assistingPlayer.name,
                    assistingPlayerPicture,
                    "Ota ${currentCard!!.points} huikkaa!",
                    drawableBeer
                )
            }

            endTurn()
        }
    }

    private fun cardCompleted() {
        CoroutineScope(Dispatchers.Main).launch {
            val assistingPlayer = gameStateViewModel.getAssistingPlayer()
            val assistingPlayerPicture = ContextCompat.getDrawable(this@MainActivity, currentPlayer.pictureResId)!!
            sfxPlayer.playCompleteCardSound()
            gameStateViewModel.addPoints(amount = currentCard!!.points)

            val winner = gameStateViewModel.checkWinner()
            if (winner != null) {
                sfxPlayer.playVictorySound()
                currentPlayerPicture = ContextCompat.getDrawable(applicationContext, winner.pictureResId)!!
                splashScreenManager.showConfirmDialog("${winner.name} voitti pelin!", drawableWin, okText = "Poistu päävalikkoon", cancelText = "") {

                    finish()
                }
            } else {
                val message : String = if (currentCard!!.points > 1) {
                    "Sait ${currentCard!!.points} pistettä!"
                } else {
                    "Sait ${currentCard!!.points} pisteen!"
                }
                splashScreenManager.showSplashScreen(
                    currentPlayer.name,
                    currentPlayerPicture,
                    message,
                    drawableAddPoints
                )
                
                if (currentCard!!.category == DUAL_CARD_TYPE) {
                gameStateViewModel.addPoints(assistingPlayer.id, currentCard!!.points)
                splashScreenManager.showSplashScreen(
                    assistingPlayer.name,
                    assistingPlayerPicture,
                    "Sait ${currentCard!!.points} pistettä!",
                    drawableAddPoints
                )
                }
                endTurn()
            }
        }
    }

    private fun endTurn() {
        CoroutineScope(Dispatchers.Main).launch {
            gameStateViewModel.endTurn()
            // prevent view from updating before splash screen is showing
            delay(200)
            cardView.setCardSide(true)
            selectionButtons.visibility = View.INVISIBLE
            ObjectAnimator.ofFloat(titleText, View.ALPHA, 1f).start()
            ObjectAnimator.ofFloat(selectionButtons, View.ALPHA, 0f).start()
            gameStateViewModel.updateSelectedCard(null)
        }
    }
}