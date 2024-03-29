package fi.sabriina.urbanhuikka.splashScreens

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import fi.sabriina.urbanhuikka.R
import fi.sabriina.urbanhuikka.helpers.DisplaySafezones

open class CustomDialog(context: Context) : Dialog(context, R.style.Theme_Huikka) {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Adjust layout for notches and navigation bar
        val contentView = findViewById<View>(android.R.id.content)
        val displaySafezones = DisplaySafezones(context)
        displaySafezones.configureLayout(contentView)
    }
}