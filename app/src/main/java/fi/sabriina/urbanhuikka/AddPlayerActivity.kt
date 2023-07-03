package fi.sabriina.urbanhuikka

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.activity.viewModels
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.TextInputEditText
import fi.sabriina.urbanhuikka.roomdb.HuikkaApplication
import fi.sabriina.urbanhuikka.roomdb.Player
import fi.sabriina.urbanhuikka.roomdb.viewmodel.PlayerViewModel
import fi.sabriina.urbanhuikka.roomdb.viewmodel.PlayerViewModelFactory

class AddPlayerActivity : AppCompatActivity() {

    private lateinit var addPlayerButton: Button
    private lateinit var playerInput: TextInputEditText

    private val playerViewModel: PlayerViewModel by viewModels {
        PlayerViewModelFactory((application as HuikkaApplication).playerRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_player)

        addPlayerButton = findViewById(R.id.addPlayerButton)
        playerInput = findViewById(R.id.textInputPlayer)

        playerInput.doOnTextChanged { _, _, _, count ->
            addPlayerButton.isEnabled = count != 0
        }

        addPlayerButton.setOnClickListener {
            val name = playerInput.text.toString().replaceFirstChar { it.uppercase() }
            playerViewModel.insert(Player(0,name))
            finish()
        }
    }
}