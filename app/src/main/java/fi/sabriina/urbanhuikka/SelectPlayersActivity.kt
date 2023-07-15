package fi.sabriina.urbanhuikka

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fi.sabriina.urbanhuikka.roomdb.*
import fi.sabriina.urbanhuikka.roomdb.HuikkaApplication
import fi.sabriina.urbanhuikka.roomdb.viewmodel.GameStateViewModel
import fi.sabriina.urbanhuikka.roomdb.viewmodel.GameStateViewModelFactory
import fi.sabriina.urbanhuikka.roomdb.viewmodel.PlayerViewModel
import fi.sabriina.urbanhuikka.roomdb.viewmodel.PlayerViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SelectPlayersActivity : AppCompatActivity() {
    private lateinit var fab: FloatingActionButton
    private lateinit var nextButton: Button

    private val gameStateViewModel: GameStateViewModel by viewModels {
        GameStateViewModelFactory((application as HuikkaApplication).gameStateRepository)
    }

    private lateinit var adapter: PlayerListAdapter

    private val playerViewModel: PlayerViewModel by viewModels {
        PlayerViewModelFactory((application as HuikkaApplication).playerRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_players)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview_leaderboard)
        nextButton = findViewById(R.id.buttonNext)
        adapter = PlayerListAdapter(nextButton)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        fab = findViewById(R.id.floatingActionButton)
        nextButton.setOnClickListener {
            val replyIntent = Intent()
            setResult(Activity.RESULT_OK, replyIntent)
            for (player in adapter.currentList) {
                playerViewModel.resetPlayerPoints(player)
            }

            gameStateViewModel.initializeDatabase()
            CoroutineScope(Dispatchers.Main).launch {
                gameStateViewModel.updateGameStatus("PLAYER_SELECT", System.currentTimeMillis())
            }
            for (player in adapter.getSelected()) {
                gameStateViewModel.insertPlayerToScoreboard(ScoreboardEntry(0, player.id))
            }
            // Close the activity
            finish()
        }

        playerViewModel.allPlayers.observe(this) { players ->
            // Update the cached copy of the words in the adapter.
            players?.let { adapter.submitList(it) }
        }


        fab.setOnClickListener {
            val intent = Intent(this@SelectPlayersActivity, AddPlayerActivity::class.java)
            startActivity(intent)
        }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // this method is called
                // when the item is moved.
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                // below line is to get the position
                // of the item at that position.
                val position = viewHolder.adapterPosition

                // this method is called when we swipe our item to right direction.
                // on below line we are getting the item at a particular position.
                adapter.getItemId(position)

                val list = adapter.currentList
                val pelaaja = list[position]

                if (adapter.currentList.size == 1){
                    nextButton.isEnabled = false
                }

                // this method is called when item is swiped.
                // below line is to remove item from our array list.
                playerViewModel.delete(pelaaja)
            }
            // at last we are adding this
            // to our recycler view.
        }).attachToRecyclerView(recyclerView)
    }

}