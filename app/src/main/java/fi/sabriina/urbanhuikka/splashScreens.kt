package fi.sabriina.urbanhuikka

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class SplashScreenManager(private val context: Context) {
    private val notificationQueue = ArrayDeque<Map<String, Any?>>()

    private var currentNotification: SplashNotification? = null
    private var isShowingNotification = false

    fun showSplashScreen(player: String, dialogMessage: String, dialogIcon: Drawable?, dialogDelay: Long = 5000) {
        val notification = mapOf(
            "player" to player,
            "dialogMessage" to dialogMessage,
            "dialogIcon" to dialogIcon,
            "dialogDelay" to dialogDelay
        )
        notificationQueue.add(notification)
        if (!isShowingNotification) {
            showNextNotification()
        }
    }

    private fun showNextNotification() {
        if (notificationQueue.isNotEmpty()) {
            if (isShowingNotification) {
                val nextNotif = notificationQueue.removeFirst()
                currentNotification?.updateNotification(
                    nextNotif["player"] as String,
                    nextNotif["dialogMessage"] as String, nextNotif["dialogIcon"] as Drawable?,
                    nextNotif["dialogDelay"] as Long
                )
                currentNotification!!.show{
                    showNextNotification()
                }
            }
            else {
                val nextNotif = notificationQueue.removeFirst()
                currentNotification = SplashNotification(
                    nextNotif["player"] as String,
                    nextNotif["dialogMessage"] as String, nextNotif["dialogIcon"] as Drawable?,
                    nextNotif["dialogDelay"] as Long
                )
                isShowingNotification = true

                currentNotification!!.show {
                    showNextNotification()
                }
            }
        }
    }

    private inner class SplashNotification(player: String, dialogMessage: String, dialogIcon: Drawable?, dialogDelay: Long) {
        private var dialog: Dialog
        private var countdownTimer: CountDownTimer
        private val playerName: TextView
        private val content: TextView
        private val icon: ImageView
        private val dismissButton: Button
        private val countdownTextView: TextView

        init {
            val dialog = Dialog(context, android.R.style.Theme_Light_NoTitleBar_Fullscreen)
            dialog.setContentView(R.layout.splash_notification)

            playerName = dialog.findViewById(R.id.notifPlayerName)
            content = dialog.findViewById(R.id.notifContent)
            icon = dialog.findViewById(R.id.notifIcon)
            dismissButton = dialog.findViewById(R.id.notifDismissButton)
            countdownTextView = dialog.findViewById(R.id.notifCountdown)

            countdownTimer = object : CountDownTimer(dialogDelay, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val secondsRemaining = millisUntilFinished / 1000
                    countdownTextView.text = "$secondsRemaining"
                }

                override fun onFinish() {
                    if (notificationQueue.isEmpty()) {
                        isShowingNotification = false
                        dialog.dismiss()
                    }
                    else {
                        showNextNotification()
                    }
                }
            }

            playerName.text = player
            content.text = dialogMessage
            icon.setImageDrawable(dialogIcon)

            dismissButton.setOnClickListener {
                countdownTimer.cancel()
                if (notificationQueue.isEmpty()) {
                    isShowingNotification = false
                    dialog.dismiss()
                }
                else {
                    showNextNotification()
                }
            }

            this.dialog = dialog
        }

        fun show(onDismiss: () -> Unit) {
            countdownTimer.cancel()
            countdownTimer.start()
            dialog.setOnDismissListener { onDismiss.invoke() }
            dialog.show()
        }

        fun updateNotification(player: String, dialogMessage: String, dialogIcon: Drawable?, dialogDelay: Long) {
            playerName.text = player
            content.text = dialogMessage
            icon.setImageDrawable(dialogIcon)
        }
    }
}