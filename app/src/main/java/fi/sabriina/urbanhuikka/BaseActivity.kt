package fi.sabriina.urbanhuikka

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import fi.sabriina.urbanhuikka.helpers.DisplaySafezones

abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Adjust layout for notches and navigation bar
        val contentView = findViewById<View>(android.R.id.content)
        val displaySafezones = DisplaySafezones(this)
        displaySafezones.configureLayout(contentView)
    }
}