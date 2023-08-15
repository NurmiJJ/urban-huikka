package fi.sabriina.urbanhuikka

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import fi.sabriina.urbanhuikka.helpers.SfxPlayer
import fi.sabriina.urbanhuikka.roomdb.HuikkaApplication
import fi.sabriina.urbanhuikka.splashScreens.SplashScreenManager
import fi.sabriina.urbanhuikka.viewmodel.GameStateViewModel
import fi.sabriina.urbanhuikka.viewmodel.GameStateViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StartActivity : AppCompatActivity() {

    private lateinit var continueButton : Button

    private val gameStateViewModel: GameStateViewModel by viewModels {
        GameStateViewModelFactory((application as HuikkaApplication).gameStateRepository)
    }

    private lateinit var splashScreenManager : SplashScreenManager

    private val sfxPlayer = SfxPlayer(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_start)

        splashScreenManager = SplashScreenManager(this)
        continueButton = findViewById(R.id.continueGameButton)
        continueButton.setOnClickListener {
            sfxPlayer.playButtonClickSound()
            val intent = Intent(this@StartActivity, MainActivity::class.java)
            startActivity(intent)
        }

        val newGameButton = findViewById<Button>(R.id.newGameButton)
        newGameButton.setOnClickListener {
            sfxPlayer.playButtonClickSound()
            val intent = Intent(this@StartActivity, SelectPlayersActivity::class.java)
            resultLauncher.launch(intent)
        }

        val playersButton = findViewById<Button>(R.id.playersButton)
        playersButton.setOnClickListener {
            sfxPlayer.playButtonClickSound()
            val intent = Intent(this@StartActivity, ManagePlayersActivity::class.java)
            resultLauncher.launch(intent)
        }

        val icon = ContextCompat.getDrawable(this, R.drawable.ic_round_x)!!
        onBackPressedDispatcher.addCallback(this) {
            splashScreenManager.showConfirmDialog(getString(R.string.quit_confirm), icon, getString(R.string.yes), getString(R.string.no)) { confirmed ->
                if (confirmed) {
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        splashScreenManager.showLoadingDialog(true)
        initializeMainMenu()
    }

    private fun initializeMainMenu() {
        CoroutineScope(Dispatchers.Main).launch {
            gameStateViewModel.checkInitialization()
            continueButton.isEnabled = gameStateViewModel.checkSavedGameExists()
            splashScreenManager.showLoadingDialog(false)
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
}