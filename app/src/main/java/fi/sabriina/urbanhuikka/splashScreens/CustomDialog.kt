package fi.sabriina.urbanhuikka.splashScreens

import android.app.Dialog
import android.content.Context
import android.view.KeyEvent
import fi.sabriina.urbanhuikka.R

class CustomDialog(context: Context, onBackButton: () -> Unit) : Dialog(context, R.style.Theme_Huikka) {
    init {

        // Set a custom OnKeyListener for the dialog
        setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                // Handle the back button press for the dialog here
                // For example, you can dismiss the dialog
                onBackButton.invoke()
                true // Return 'true' to indicate that the event has been handled
            } else {
                false // Return 'false' to indicate that the event should be processed normally
            }
        }
    }
}