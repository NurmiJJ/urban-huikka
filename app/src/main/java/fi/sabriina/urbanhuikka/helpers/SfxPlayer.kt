package fi.sabriina.urbanhuikka.helpers

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import fi.sabriina.urbanhuikka.R

class SfxPlayer(val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun playButtonClickSound() {
        initPlayer()
        val soundUri =
            Uri.parse("android.resource://${context.packageName}/${R.raw.button_click}")
        mediaPlayer?.setDataSource(context, soundUri)
        mediaPlayer?.prepare()
        mediaPlayer?.start()
    }

    fun playCardDrawSound() {
        initPlayer()
        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.draw_card}")
        mediaPlayer?.setDataSource(context, soundUri)
        mediaPlayer?.prepare()
        mediaPlayer?.start()
    }

    fun playSkipCardSound() {
        initPlayer()
        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.skip_card}")
        mediaPlayer?.setDataSource(context, soundUri)
        mediaPlayer?.prepare()
        mediaPlayer?.start()
    }

    fun playCompleteCardSound() {
        initPlayer()
        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.complete_card}")
        mediaPlayer?.setDataSource(context, soundUri)
        mediaPlayer?.prepare()
        mediaPlayer?.start()
    }

    fun playVictorySound() {
        initPlayer()
        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.victory}")
        mediaPlayer?.setDataSource(context, soundUri)
        mediaPlayer?.prepare()
        mediaPlayer?.start()
    }

    fun playTimerTickingSound() {
        initPlayer()
        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.timer_ticking}")
        mediaPlayer?.setDataSource(context, soundUri)
        mediaPlayer?.prepare()
        mediaPlayer?.start()
    }

    fun playTimesUpSound() {
        initPlayer()
        val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.times_up}")
        mediaPlayer?.setDataSource(context, soundUri)
        mediaPlayer?.prepare()
        mediaPlayer?.start()
    }

    private fun initPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
        } else {
            mediaPlayer?.reset()
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}