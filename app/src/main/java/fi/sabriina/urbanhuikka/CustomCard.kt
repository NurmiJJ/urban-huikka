package fi.sabriina.urbanhuikka

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.CountDownTimer
import android.text.Html
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import fi.sabriina.urbanhuikka.card.Card

const val ASSISTING_PLACEHOLDER = "[pelaaja]"

interface OnCardSwipeListener {
    fun onSwipeRight()
    fun onSwipeLeft()
}

class CustomCard : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet) : super(context, attr)
    constructor(context: Context, attr: AttributeSet, defStyleAttr: Int) : super(
        context,
        attr,
        defStyleAttr
    )

    private var initialX = 0f
    private var cardX = 0f
    private var deltaX = 0f
    private var isMoving = false
    private var swipeListener: OnCardSwipeListener? = null

    private var backside = true
    private var feedbackGiven = false
    private var time = 0L
    private var timeView: TextView
    private lateinit var countdownTimer: CountDownTimer
    private lateinit var timerCallback: () -> Unit

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_card, this, true)
        timeView = findViewById(R.id.textViewTimer)
        init()
    }

    fun setCard(card: Card) {
        val titleView = findViewById<TextView>(R.id.textViewHeader)
        val descriptionView = findViewById<TextView>(R.id.textViewDescription)
        val pointsView = findViewById<TextView>(R.id.textViewPoints)
        
        var cardDescription = card.description
        cardDescription = cardDescription.replace(ASSISTING_PLACEHOLDER, "<b>$assistingPlayerName</b> ")

        titleView.text = card.category
        descriptionView.text = Html.fromHtml(cardDescription, 0)
        val points = "Pisteet: ${card.points}"
        pointsView.text = points
        time = card.time.toLong() * 1000
        timeView.text = ""

        countdownTimer = object : CountDownTimer(time, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                timeView.text = "$secondsRemaining"
            }

            override fun onFinish() {
                timeView.text = context.getString(R.string.times_up)
                timerCallback.invoke()
            }
        }
    }

    // Setup views
    private fun init() {
    }

    // Helper function to toggle content visibility
    fun setCardSide(showBackside: Boolean) {
        feedbackGiven = false
        backside = showBackside
        val frontView = findViewById<ConstraintLayout>(R.id.frontLayout)
        val backView = findViewById<ImageView>(R.id.cardBackside)
        frontView.visibility = if (backside) View.GONE else View.VISIBLE
        backView.visibility = if (backside) View.VISIBLE else View.GONE
    }

    fun startCountdownTimer(callback: () -> Unit) {
        timerCallback = callback
        countdownTimer.start()
    }

    fun setOnCardSwipeListener(listener: OnCardSwipeListener) {
        this.swipeListener = listener
    }


    private fun flipCard(direction: String) {
        val frontView = findViewById<ConstraintLayout>(R.id.frontLayout)
        val backView = findViewById<ImageView>(R.id.cardBackside)

        val scale = context.resources.displayMetrics.density
        val cameraDistance = 8000 * scale

        val flipInAnimator: Animator
        val flipOutAnimator: Animator

        if (direction == "left") {
            flipOutAnimator = ObjectAnimator.ofFloat(this, "rotationY", 90f)
            flipInAnimator = ObjectAnimator.ofFloat(this, "rotationY", -90f, 0f)
        } else {
            flipOutAnimator = ObjectAnimator.ofFloat(this, "rotationY", -90f)
            flipInAnimator = ObjectAnimator.ofFloat(this, "rotationY", 90f, 0f)
        }

        flipOutAnimator.target = backView
        flipInAnimator.target = frontView

        val returnAnimator = ObjectAnimator.ofFloat(this, "translationX", 0f)
        returnAnimator.interpolator = DecelerateInterpolator() // Add a decelerate effect

        frontView.cameraDistance = cameraDistance
        backView.cameraDistance = cameraDistance

        flipOutAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                setCardSide(false)
                flipInAnimator.start()
                returnAnimator.start()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        flipInAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                // reset backside to initial position
                returnAnimator.target = backView
                flipInAnimator.target = backView

                // prevent infinite looping of animation
                returnAnimator.removeAllListeners()
                flipInAnimator.removeAllListeners()

                returnAnimator.start()
                flipInAnimator.start()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        // start first animation
        flipOutAnimator.start()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (backside) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = event.rawX
                    cardX = x

                    // card was not moved
                    deltaX = 0F
                    isMoving = false

                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    // Calculate the distance moved by the finger
                    deltaX = event.rawX - initialX

                    // Update card position
                    x = cardX + deltaX

                    // Mark the card as moving to avoid conflicting with other touch events
                    isMoving = true

                    if ((deltaX > SWIPE_THRESHOLD || deltaX < -SWIPE_THRESHOLD) && !feedbackGiven) {
                        feedbackGiven = true
                        // make haptic feedback stronger
                        for (i in 1..2) {
                            performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                        }
                    } else if (deltaX < SWIPE_THRESHOLD && deltaX > -SWIPE_THRESHOLD) {
                        feedbackGiven = false
                    }

                    return true
                }

                MotionEvent.ACTION_UP -> {
                    if (deltaX > SWIPE_THRESHOLD) {
                        swipeListener?.onSwipeRight()
                        flipCard("right")
                    } else if (deltaX < -SWIPE_THRESHOLD) {
                        swipeListener?.onSwipeLeft()
                        flipCard("left")
                    } else {
                        // If the card was not moved enough, animate it back to the original position
                        val returnAnimator = ObjectAnimator.ofFloat(this, "translationX", 0f)
                        returnAnimator.interpolator =
                            DecelerateInterpolator() // Add a decelerate effect
                        returnAnimator.start()
                    }
                    isMoving = false
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    companion object {
        private const val SWIPE_THRESHOLD = 300
    }
}