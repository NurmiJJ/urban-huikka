package fi.sabriina.urbanhuikka.splashScreens

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import fi.sabriina.urbanhuikka.R
import fi.sabriina.urbanhuikka.helpers.SfxPlayer


class SplashScreenManager(private val context: Context) {
    private val notificationQueue = ArrayDeque<Map<String, Any?>>()

    private var currentNotification: SplashNotification? = null
    private var isShowingNotification = false
    private var confirmed = false
    private var loadingDialog = LoadingDialog()

    private val sfxPlayer = SfxPlayer(context)

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
            val dialog = DialogWithCustomBackButtonBehavior(context) { handleNextNotification() }
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
                sfxPlayer.playButtonClickSound()
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
        private var dialog: CustomDialog
        private val okButton: Button
        private val cancelButton: Button

        init {
            val dialog = CustomDialog(context)
            dialog.setContentView(R.layout.pause_dialog)

            okButton = dialog.findViewById(R.id.quitButton)
            cancelButton = dialog.findViewById(R.id.continueButton)

            okButton.setOnClickListener {
                sfxPlayer.playButtonClickSound()
                notificationQueue.clear()
                currentNotification = null
                confirmed = true
                dialog.dismiss()
            }

            cancelButton.setOnClickListener {
                sfxPlayer.playButtonClickSound()
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
        private var dialog: CustomDialog
        private val okButton: Button
        private val cancelButton: Button
        private val content: TextView
        private val icon: ImageView
        private val mainLayout: ConstraintLayout

        init {
            val dialog = CustomDialog(context)
            dialog.setContentView(R.layout.confirm_dialog)

            okButton = dialog.findViewById(R.id.okButton)
            cancelButton = dialog.findViewById(R.id.cancelButton)
            cancelButton.visibility = View.INVISIBLE
            content = dialog.findViewById(R.id.notifContent)
            icon = dialog.findViewById(R.id.notifIcon)
            mainLayout = dialog.findViewById(R.id.mainLayout)

            okButton.text = okText
            okButton.setOnClickListener {
                sfxPlayer.playButtonClickSound()
                notificationQueue.clear()
                currentNotification = null
                confirmed = true
                dialog.dismiss()
            }

            if (cancelText != "") {
                cancelButton.visibility = View.VISIBLE
                cancelButton.text = cancelText
                cancelButton.setOnClickListener {
                    sfxPlayer.playButtonClickSound()
                    confirmed = false
                    dialog.dismiss()
                }
            } else {
                val constraintSet = ConstraintSet()
                constraintSet.clone(mainLayout)
                constraintSet.connect(R.id.okButton, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 32.toDp())
                constraintSet.applyTo(mainLayout)
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
        private var dialog: CustomDialog

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


    fun Int.toDp(): Int {
        val density = Resources.getSystem().displayMetrics.density
        return (this * density).toInt()
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