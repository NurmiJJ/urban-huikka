package fi.sabriina.urbanhuikka.helpers

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DisplaySafezones(val context: Context) {

    fun configureLayout(contentView: View) {
        ViewCompat.setOnApplyWindowInsetsListener(contentView) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            val layoutParams = contentView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.leftMargin = insets.left
            layoutParams.bottomMargin = insets.bottom
            layoutParams.rightMargin = insets.right
            layoutParams.topMargin = getNotchHeight()
            contentView.layoutParams = layoutParams

            WindowInsetsCompat.CONSUMED
        }

    }

    private fun getNotchHeight(): Int {
        val display = context.display

        val insets = display?.cutout?.boundingRects

        if (!insets.isNullOrEmpty()) {
            // Get the height of the first bounding rectangle (usually the notch)
            return insets[0].height()
        }
        return 0
    }
}