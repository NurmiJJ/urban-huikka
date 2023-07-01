package fi.sabriina.urbanhuikka

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun showCustomDialog(context: Context, dialogTitle: String, dialogMessage: String, dialogDelay: Long = 2000 ){
    val title = TextView(context)
    title.text = dialogTitle
    title.setBackgroundColor(Color.DKGRAY)
    title.setPadding(10, 10, 10, 10)
    title.gravity = Gravity.CENTER
    title.setTextColor(Color.WHITE)
    title.textSize = 30f


    val dialog = AlertDialog.Builder(context)
        .setMessage(dialogMessage)
        .create()

    dialog.setCanceledOnTouchOutside(false)
    dialog.setCancelable(false)
    dialog.setCustomTitle(title)
    dialog.setOnShowListener {
        CoroutineScope(Dispatchers.Main).launch {
            delay(dialogDelay)
            dialog.dismiss()
        }
    }

    dialog.show()
}
