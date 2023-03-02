package fi.sabriina.urbanhuikka

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        var newGameButton = findViewById<Button>(R.id.newGameButton)
        newGameButton.setOnClickListener{
            val intent = Intent(this@StartActivity, SetPlayersActivity::class.java)
            startActivity(intent)
        }
    }
}