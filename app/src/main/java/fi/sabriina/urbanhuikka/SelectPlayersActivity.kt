package fi.sabriina.urbanhuikka

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.GridView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fi.sabriina.urbanhuikka.helpers.DbConstants
import fi.sabriina.urbanhuikka.helpers.SfxPlayer
import fi.sabriina.urbanhuikka.roomdb.*
import fi.sabriina.urbanhuikka.roomdb.HuikkaApplication
import fi.sabriina.urbanhuikka.splashScreens.SplashScreenManager
import fi.sabriina.urbanhuikka.viewmodel.GameStateViewModel
import fi.sabriina.urbanhuikka.viewmodel.GameStateViewModelFactory
import fi.sabriina.urbanhuikka.viewmodel.PlayerViewModel
import fi.sabriina.urbanhuikka.viewmodel.PlayerViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SelectPlayersActivity : AppCompatActivity() {
    private lateinit var managePlayersButton: Button
    private lateinit var nextButton: Button

    private val sfxPlayer = SfxPlayer(this)

    private val gameStateViewModel: GameStateViewModel by viewModels {
        GameStateViewModelFactory((application as HuikkaApplication).gameStateRepository)
    }

    private lateinit var adapter: PlayerListAdapter

    private val playerViewModel: PlayerViewModel by viewModels {
        PlayerViewModelFactory((application as HuikkaApplication).playerRepository)
    }

    private lateinit var splashScreenManager: SplashScreenManager
    private lateinit var pointsToWinSelector: SeekBar
    private lateinit var pointsToWinValue: TextView
    private lateinit var categorySelection: GridView

    private val allCategories = DbConstants.DARE_CATEGORIES + DbConstants.TRUTH_CATEGORIES
    private var enabledCategories = mutableListOf<String>()
    private var gridItems = mutableListOf<GridItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_players)

        splashScreenManager = SplashScreenManager(this)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview_leaderboard)
        nextButton = findViewById(R.id.buttonDelete)
        adapter = PlayerListAdapter(this, "SELECT", nextButton)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        managePlayersButton = findViewById(R.id.managePlayersButton)

        for (category in allCategories) {
            gridItems.add(GridItem(category, true))
        }

        categorySelection = findViewById(R.id.categorySelection)
        categorySelection.adapter = CategorySelectionAdapter()

        pointsToWinSelector = findViewById(R.id.pointsToWinSelector)
        pointsToWinValue = findViewById(R.id.pointsToWinValue)
        pointsToWinValue.text = pointsToWinSelector.progress.toString()
        pointsToWinSelector.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                pointsToWinValue.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        nextButton.setOnClickListener {
            sfxPlayer.playButtonClickSound()
            val icon = ContextCompat.getDrawable(
                this,
                com.google.android.material.R.drawable.mtrl_ic_error
            )!!
            CoroutineScope(Dispatchers.Main).launch {
                if (gameStateViewModel.checkSavedGameExists()) {
                    splashScreenManager.showConfirmDialog(
                        "Uuden pelin aloittaminen korvaa edellisen keskeneräisen pelin",
                        icon,
                        getString(R.string.continue_),
                        getString(R.string.cancel)
                    ) { confirmed ->
                        if (confirmed) {
                            startGame()
                        } else {
                            finish()
                        }
                    }
                } else {
                    startGame()
                }
            }
        }

        playerViewModel.allPlayers.observe(this) { players ->
            // Update the cached copy of the words in the adapter.
            players?.let { adapter.submitList(it) }
        }

        managePlayersButton.setOnClickListener {
            sfxPlayer.playButtonClickSound()
            val intent = Intent(this, ManagePlayersActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkStartGameButtonStatus() {
        var truthCount = 0
        var dareCount = 0
        for (category in gridItems) {
            Log.d(TAG, "$truthCount $dareCount")
            if (category.name in DbConstants.TRUTH_CATEGORIES) {
                if (category.isChecked) {
                    truthCount += 1
                }
            } else {
                if (category.isChecked) {
                    dareCount += 1
                }
            }
            nextButton.isEnabled = truthCount > 0 && dareCount > 0 && adapter.getSelected().size > 1
            if (nextButton.isEnabled) {
                break
            }
        }
    }

    private fun startGame() {
        val replyIntent = Intent()
        setResult(Activity.RESULT_OK, replyIntent)

        for (category in gridItems) {
            if (category.isChecked) {
                enabledCategories.add(category.name)
            }
        }

        CoroutineScope(Dispatchers.Main).launch {
            splashScreenManager.showLoadingDialog(true)

            gameStateViewModel.startNewGame()
            gameStateViewModel.setPointsToWin(pointsToWinSelector.progress)
            gameStateViewModel.setEnabledCardCategories(enabledCategories)
            gameStateViewModel.updateGameStatus("PLAYER_SELECT")
            val selectedPlayers = adapter.getSelected()
            selectedPlayers.forEach { player ->
                Log.w(TAG, "Inserting $player to scoreboard")
                gameStateViewModel.insertPlayerToScoreboard(ScoreboardEntry(0, player.id))
            }

            finish()
        }

    }

    private inner class CategorySelectionAdapter : BaseAdapter() {
        override fun getCount(): Int {
            return gridItems.size
        }

        override fun getItem(position: Int): Any {
            return gridItems[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(applicationContext).inflate(R.layout.category_selector, parent, false)
            val categorySelector: CheckBox = view.findViewById(R.id.checkBox)
            val item = gridItems[position]

            categorySelector.setOnCheckedChangeListener { _, isChecked ->
                item.isChecked = isChecked
                checkStartGameButtonStatus()
            }

            categorySelector.text = item.name
            categorySelector.isChecked = item.isChecked
            return view
        }
    }

    data class GridItem(val name: String, var isChecked: Boolean)
}