package fi.sabriina.urbanhuikka

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import fi.sabriina.urbanhuikka.roomdb.*
import fi.sabriina.urbanhuikka.roomdb.HuikkaApplication
import fi.sabriina.urbanhuikka.splashScreens.SplashScreenManager
import fi.sabriina.urbanhuikka.viewmodel.PlayerViewModel
import fi.sabriina.urbanhuikka.viewmodel.PlayerViewModelFactory

class ManagePlayersActivity : AppCompatActivity() {
    private lateinit var fab: FloatingActionButton
    private lateinit var deleteButton: Button
    private lateinit var title: TextView

    private lateinit var adapter: PlayerListAdapter

    private val playerViewModel: PlayerViewModel by viewModels {
        PlayerViewModelFactory((application as HuikkaApplication).playerRepository)
    }

    private lateinit var splashScreenManager : SplashScreenManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_players)

        splashScreenManager = SplashScreenManager(this)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview_leaderboard)
        deleteButton = findViewById(R.id.buttonDelete)
        adapter = PlayerListAdapter(this, "MANAGE", deleteButton)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        fab = findViewById(R.id.floatingActionButton)
        title = findViewById(R.id.managePlayersTitle)

        playerViewModel.allPlayers.observe(this) { players ->
            players?.let { adapter.submitList(it) }
            if (players.isEmpty()) {
                title.text = getString(R.string.no_players)
            } else {
                title.text = getString(R.string.all_players)
            }

        }

        fab.setOnClickListener {
            val intent = Intent(this@ManagePlayersActivity, AddPlayerActivity::class.java)
            startActivity(intent)
        }

        deleteButton.setOnClickListener {
            val playersToDelete = adapter.getSelected()
            for (player in playersToDelete) {
                playerViewModel.delete(player)
            }
            adapter.emptySelected()
        }
    }
}