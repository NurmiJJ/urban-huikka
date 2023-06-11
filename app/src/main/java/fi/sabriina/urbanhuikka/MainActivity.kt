package fi.sabriina.urbanhuikka

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
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
import fi.sabriina.urbanhuikka.player.*
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

    var truthCardList = mutableListOf<Card>()
    var dareCardList = mutableListOf<Card>()

    private val model: StatusViewModel by viewModels()


    private val playerViewModel: PlayerViewModel by viewModels {
        PlayerViewModelFactory((application as PlayersApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gameStatusObserver = Observer<String> {status ->
            Log.d("STATUS", status)
            if (status == "INACTIVE") {
                Log.d("STATUS", "Here")
                truthCardList.shuffle()
                dareCardList.shuffle()
                playerViewModel.updateGameStatus(1, "ONGOING")
            }
        }

        playerViewModel.gameStatus.observe(this, gameStatusObserver)

        playerViewModel.getGameStatus(1)

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

        playerViewModel.allPlayers.observe(this, Observer { players ->
            if (playerList.size == 0) {
                playerName.text = players[0].name
            }
            playerList = players.toMutableList()
            players?.let { adapter.submitList(it) }
        })

        model.currentPlayer.observe(this) { playerNo ->
            if (playerList.size > 0) {
                val name = playerList[playerNo].name
                playerName.text = name
                showNextPlayerDialog(name)
            }
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
        val currentNo = model.currentPlayer.value!!
        val name = playerList[currentNo].name
        // Points from card
        val points = 3
        showTaskButtons()
        showHuikkaDialog(name, points)
        CoroutineScope(Dispatchers.Main).launch {
            delay(3000)
            endTurn()
        }
    }

    private fun cardCompleted() {
        addPoints()
        showTaskButtons()
        CoroutineScope(Dispatchers.Main).launch {
            delay(3000)
            endTurn()
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
        showPointsDialog(player.name, points)

    }

    private fun showPointsDialog(playerName: String, points: Int){
        val title = TextView(this)
        title.text = playerName
        title.setBackgroundColor(Color.DKGRAY)
        title.setPadding(10, 10, 10, 10)
        title.gravity = Gravity.CENTER
        title.setTextColor(Color.WHITE)
        title.textSize = 30f


        val dialog = AlertDialog.Builder(this)
            .setMessage("Sait $points pistettä!")
            .create()

        dialog.setCustomTitle(title)
        dialog.setOnShowListener {
            CoroutineScope(Dispatchers.Main).launch {
                delay(2000)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showNextPlayerDialog(playerName: String){
        val title = TextView(this)
        title.text = playerName
        title.setBackgroundColor(Color.DKGRAY)
        title.setPadding(10, 10, 10, 10)
        title.gravity = Gravity.CENTER
        title.setTextColor(Color.WHITE)
        title.textSize = 30f


        val dialog = AlertDialog.Builder(this)
            .setMessage("Totuus vai tehtävä?")
            .create()

        dialog.setCustomTitle(title)
        dialog.setOnShowListener {
            CoroutineScope(Dispatchers.Main).launch {
                delay(2000)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showHuikkaDialog(playerName: String, points: Int){
        val title = TextView(this)
        title.text = playerName
        title.setBackgroundColor(Color.DKGRAY)
        title.setPadding(10, 10, 10, 10)
        title.gravity = Gravity.CENTER
        title.setTextColor(Color.WHITE)
        title.textSize = 30f


        val dialog = AlertDialog.Builder(this)
            .setMessage("Ota $points huikkaa!")
            .create()

        dialog.setCustomTitle(title)
        dialog.setOnShowListener {
            CoroutineScope(Dispatchers.Main).launch {
                delay(2000)
                dialog.dismiss()
            }
        }

        dialog.show()
    }



}