package fi.sabriina.urbanhuikka.splashScreens

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import fi.sabriina.urbanhuikka.R

class SplashScreenManager(private val context: Context) {
    private val notificationQueue = ArrayDeque<Map<String, Any?>>()

    private var currentNotification: SplashNotification? = null
    private var isShowingNotification = false
    private var confirmed = false
    private var loadingDialog = LoadingDialog()

    fun showSplashScreen(
        playerName: String,
        playerPicture: Drawable,
        dialogMessage: String,
        dialogIcon: Drawable,
        dialogDelay: Long = 6000
    ) {
        val notification = mapOf(
            "playerName" to playerName,
            "playerPicture" to playerPicture,
            "dialogMessage" to dialogMessage,
            "dialogIcon" to dialogIcon,
            "dialogDelay" to dialogDelay
        )
        notificationQueue.add(notification)
        if (!isShowingNotification) {
            handleNextNotification()
        }
    }

    fun dismissAllSplashScreens() {
        notificationQueue.clear()
        currentNotification?.dismiss()
    }

    private fun handleNextNotification() {
        if (notificationQueue.isEmpty()) {
            isShowingNotification = false
            currentNotification?.cancelTimer()
            currentNotification?.dismiss()
        } else {
            currentNotification?.cancelTimer()
            showNextNotification()
        }
    }

    private fun showNextNotification() {
        if (isShowingNotification) {
            val nextNotif = notificationQueue.removeFirst()
            currentNotification?.updateNotification(
                nextNotif["playerName"] as String,
                nextNotif["playerPicture"] as Drawable,
                nextNotif["dialogMessage"] as String,
                nextNotif["dialogIcon"] as Drawable,
            )
        } else {
            val nextNotif = notificationQueue.removeFirst()
            currentNotification = SplashNotification(
                nextNotif["playerName"] as String,
                nextNotif["playerPicture"] as Drawable,
                nextNotif["dialogMessage"] as String,
                nextNotif["dialogIcon"] as Drawable,
                nextNotif["dialogDelay"] as Long
            )
            isShowingNotification = true
        }

        currentNotification!!.show()
    }

    private inner class SplashNotification(
        pName: String,
        pPicture: Drawable,
        dialogMessage: String,
        dialogIcon: Drawable,
        dialogDelay: Long
    ) {
        private var dialog: Dialog
        private var countdownTimer: CountDownTimer
        private val playerName: TextView
        private val playerPicture: ImageView
        private val content: TextView
        private val icon: ImageView
        private val dismissButton: Button
        private val countdownTextView: TextView

        init {
            val dialog = CustomDialog(context) { handleNextNotification() }
            dialog.setContentView(R.layout.splash_notification)

            playerName = dialog.findViewById(R.id.notifPlayerName)
            playerPicture = dialog.findViewById(R.id.playerPicture)
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

            playerName.text = pName
            playerPicture.setImageDrawable(pPicture)
            content.text = dialogMessage
            icon.setImageDrawable(dialogIcon)

            dismissButton.setOnClickListener {
                handleNextNotification()
            }

            this.dialog = dialog
        }

        fun show() {
            dialog.show()
            countdownTimer.start()
        }

        fun updateNotification(
            pName: String,
            pPicture: Drawable,
            dialogMessage: String,
            dialogIcon: Drawable
        ) {
            playerName.text = pName
            playerPicture.setImageDrawable(pPicture)
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

    fun showConfirmDialog(
        message: String,
        icon: Drawable,
        okText: String,
        cancelText: String,
        callback: (Boolean) -> Unit
    ) {
        val dialog = ConfirmDialog(message, icon, okText, cancelText)
        dialog.show { callback(confirmed) }
    }

    private inner class ConfirmDialog(
        dialogMessage: String,
        dialogIcon: Drawable,
        okText: String,
        cancelText: String
    ) {
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
            cancelButton.visibility = View.INVISIBLE
            content = dialog.findViewById(R.id.notifContent)
            icon = dialog.findViewById(R.id.notifIcon)

            okButton.text = okText
            okButton.setOnClickListener {
                notificationQueue.clear()
                currentNotification = null
                confirmed = true
                dialog.dismiss()
            }

            if (cancelText != "") {
                cancelButton.visibility = View.VISIBLE
                cancelButton.text = cancelText
                cancelButton.setOnClickListener {
                    confirmed = false
                    dialog.dismiss()
                }
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

    fun showLoadingDialog(visible: Boolean) {
        if (visible) {
            loadingDialog.show()
        } else {
            loadingDialog.hide()
        }
    }

    private inner class LoadingDialog {
        private var dialog: Dialog

        init {
            val dialog = NonDismissableDialog(context)
            dialog.setContentView(R.layout.loading_dialog)

            this.dialog = dialog
        }

        fun show() {
            dialog.show()
        }

        fun hide() {
            dialog.dismiss()
        }
    }

    fun showCountdownDialog(
        time: Long = 4000,
        title: String = context.getString(R.string.timed_card),
        callback: () -> Unit
    ) {
        val dialog = CountdownDialog(time, title)
        dialog.show(callback)
    }

    private inner class CountdownDialog(time: Long, title: String) {
        private var dialog: Dialog
        private var countdownTime: TextView
        private var countdownTitle: TextView
        private var countdownTimer: CountDownTimer

        init {
            val dialog = NonDismissableDialog(context)
            dialog.setContentView(R.layout.countdown_dialog)
            countdownTime = dialog.findViewById(R.id.countdownTime)
            countdownTitle = dialog.findViewById(R.id.countdownTitle)

            countdownTitle.text = title

            countdownTimer = object : CountDownTimer(time, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val secondsRemaining = millisUntilFinished / 1000
                    countdownTime.text = "$secondsRemaining"
                }

                override fun onFinish() {
                    dialog.dismiss()
                }
            }

            this.dialog = dialog
        }

        fun show(onDismiss: () -> Unit) {
            dialog.setOnDismissListener { onDismiss.invoke() }
            dialog.show()
            countdownTimer.start()
        }
    }
}