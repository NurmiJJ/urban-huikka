package fi.sabriina.urbanhuikka.splashScreens

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import fi.sabriina.urbanhuikka.R

class SplashScreenManager(private val context: Context) {
    private val notificationQueue = ArrayDeque<Map<String, Any?>>()

    private var currentNotification: SplashNotification? = null
    private var isShowingNotification = false
    private var confirmed = false

    fun showSplashScreen(player: String, dialogMessage: String, dialogIcon: Drawable?, dialogDelay: Long = 5000) {
        val notification = mapOf(
            "player" to player,
            "dialogMessage" to dialogMessage,
            "dialogIcon" to dialogIcon,
            "dialogDelay" to dialogDelay
        )
        notificationQueue.add(notification)
        if (!isShowingNotification) {
            handleNextNotification()
        }
    }

    private fun handleNextNotification() {
        if (notificationQueue.isEmpty()) {
            isShowingNotification = false
            currentNotification?.cancelTimer()
            currentNotification?.dismiss()
        }
        else {
            currentNotification?.cancelTimer()
            showNextNotification()
        }
    }

    private fun showNextNotification() {
        if (isShowingNotification) {
            val nextNotif = notificationQueue.removeFirst()
            currentNotification?.updateNotification(
                nextNotif["player"] as String,
                nextNotif["dialogMessage"] as String, nextNotif["dialogIcon"] as Drawable?,
            )
        }
        else {
            val nextNotif = notificationQueue.removeFirst()
            currentNotification = SplashNotification(
                nextNotif["player"] as String,
                nextNotif["dialogMessage"] as String, nextNotif["dialogIcon"] as Drawable?,
                nextNotif["dialogDelay"] as Long
            )
            isShowingNotification = true
        }

        currentNotification!!.show()
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
            val dialog = CustomDialog(context) { handleNextNotification() }
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
                    handleNextNotification()
                }
            }

            playerName.text = player
            content.text = dialogMessage
            icon.setImageDrawable(dialogIcon)

            dismissButton.setOnClickListener {
                handleNextNotification()
            }

            this.dialog = dialog
        }

        fun show() {
            countdownTimer.start()
            dialog.show()
        }

        fun updateNotification(player: String, dialogMessage: String, dialogIcon: Drawable?) {
            playerName.text = player
            content.text = dialogMessage
            icon.setImageDrawable(dialogIcon)
        }

        fun dismiss() {
            dialog.dismiss()
        }

        fun cancelTimer() {
            countdownTimer.cancel()
        }
    }

    fun showPauseDialog(callback: (Boolean) -> Unit) {
        val dialog = PauseDialog()
        dialog.show { callback(confirmed) }
    }

    private inner class PauseDialog {
        private var dialog: Dialog
        private val okButton: Button
        private val cancelButton: Button

        init {
            val dialog = Dialog(context, R.style.Theme_Huikka)
            dialog.setContentView(R.layout.pause_dialog)

            okButton = dialog.findViewById(R.id.quitButton)
            cancelButton = dialog.findViewById(R.id.continueButton)

            okButton.setOnClickListener {
                notificationQueue.clear()
                currentNotification = null
                confirmed = true
                dialog.dismiss()
            }

            cancelButton.setOnClickListener {
                confirmed = false
                dialog.dismiss()
            }

            this.dialog = dialog
        }

        fun show(onDismiss: () -> Unit) {
            dialog.setOnDismissListener { onDismiss.invoke() }
            dialog.show()
        }
    }

    fun showConfirmDialog(message: String, icon: Drawable?, callback: (Boolean) -> Unit) {
        val dialog = ConfirmDialog(message, icon)
        dialog.show { callback(confirmed) }
    }

    private inner class ConfirmDialog(dialogMessage: String, dialogIcon: Drawable?) {
        private var dialog: Dialog
        private val okButton: Button
        private val cancelButton: Button
        private val content: TextView
        private val icon: ImageView

        init {
            val dialog = Dialog(context, R.style.Theme_Huikka)
            dialog.setContentView(R.layout.confirm_dialog)

            okButton = dialog.findViewById(R.id.okButton)
            cancelButton = dialog.findViewById(R.id.cancelButton)
            content = dialog.findViewById(R.id.notifContent)
            icon = dialog.findViewById(R.id.notifIcon)

            okButton.setOnClickListener {
                notificationQueue.clear()
                currentNotification = null
                confirmed = true
                dialog.dismiss()
            }

            cancelButton.setOnClickListener {
                confirmed = false
                dialog.dismiss()
            }

            content.text = dialogMessage
            icon.setImageDrawable(dialogIcon)

            this.dialog = dialog
        }

        fun show(onDismiss: () -> Unit) {
            dialog.setOnDismissListener { onDismiss.invoke() }
            dialog.show()
        }
    }
}