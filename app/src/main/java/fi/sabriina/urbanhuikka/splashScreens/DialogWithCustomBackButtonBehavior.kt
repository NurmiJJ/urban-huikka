package fi.sabriina.urbanhuikka.splashScreens

import android.content.Context
import android.view.KeyEvent

class DialogWithCustomBackButtonBehavior(context: Context, onBackButton: () -> Unit) :
    CustomDialog(context) {
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