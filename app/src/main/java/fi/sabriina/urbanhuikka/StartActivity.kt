package fi.sabriina.urbanhuikka

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import fi.sabriina.urbanhuikka.roomdb.GameState
import fi.sabriina.urbanhuikka.roomdb.HuikkaApplication
import fi.sabriina.urbanhuikka.roomdb.viewmodel.GameStateViewModel
import fi.sabriina.urbanhuikka.roomdb.viewmodel.GameStateViewModelFactory

class StartActivity : AppCompatActivity() {

    private lateinit var continueButton : Button

    private val gameStateViewModel: GameStateViewModel by viewModels {
        GameStateViewModelFactory((application as HuikkaApplication).gameStateRepository)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        continueButton = findViewById(R.id.continueGameButton)
        continueButton.setOnClickListener {
            val intent = Intent(this@StartActivity, MainActivity::class.java)
            startActivity(intent)
        }

        val newGameButton = findViewById<Button>(R.id.newGameButton)
        newGameButton.setOnClickListener {
            val intent = Intent(this@StartActivity, SetPlayersActivity::class.java)
            resultLauncher.launch(intent)
        }
    }

    private var resultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = Intent(this@StartActivity, MainActivity::class.java)
            startActivity(intent)

        }
    }

    override fun onStart() {
        super.onStart()

        val gameStatusObserver = Observer<GameState> { gameState ->
            continueButton.isEnabled = gameState.status == "ONGOING"
        }
        gameStateViewModel.gameStatus.observe(this, gameStatusObserver)
    }
}