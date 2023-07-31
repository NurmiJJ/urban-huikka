package fi.sabriina.urbanhuikka

import android.animation.Animator
import android.animation.AnimatorInflater
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import fi.sabriina.urbanhuikka.card.Card
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

interface OnCardSwipeListener {
    fun onSwipeRight()
    fun onSwipeLeft()
}

class CustomCard : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet) : super(context, attr)
    constructor(context: Context, attr: AttributeSet, defStyleAttr : Int) : super(context, attr, defStyleAttr )

    private var initialX = 0f
    private var swipeListener: OnCardSwipeListener? = null

    private var backside = true

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_card, this, true)
        init()
    }
     fun setCard(card: Card){
         val titleView = findViewById<TextView>(R.id.textViewHeader)
         val descriptionView = findViewById<TextView>(R.id.textViewDescription)
         val pointsView = findViewById<TextView>(R.id.textViewPoints)

         titleView.text = card.category
         descriptionView.text = card.description
         val points = "Pisteet: ${card.points}"
         pointsView.text = points
     }

    // Setup views
    private fun init() {
    }

    // Helper function to toggle content visibility
    fun setCardSide(showBackside: Boolean) {
        backside = showBackside
        val frontView = findViewById<ConstraintLayout>(R.id.frontLayout)
        val backView = findViewById<ImageView>(R.id.cardBackside)

        if (backside) {
            Log.d(TAG, "backside")
        }
        else {
            Log.d(TAG, "front")
        }
        frontView.visibility = if (backside) View.GONE else View.VISIBLE
        backView.visibility = if (backside) View.VISIBLE else View.GONE
    }

    fun setOnCardSwipeListener(listener: OnCardSwipeListener) {
        this.swipeListener = listener
    }


    private fun flipCard(direction: String) {
        val frontView = findViewById<ConstraintLayout>(R.id.frontLayout)
        val backView = findViewById<ImageView>(R.id.cardBackside)

        val scale = context.resources.displayMetrics.density
        val cameraDistance = 8000 * scale

        val flipInAnimator : Animator
        val flipOutAnimator : Animator
        val moveInAnimator : Animator
        val moveOutAnimator : Animator
        if (direction == "left") {
            flipOutAnimator = AnimatorInflater.loadAnimator(context, R.animator.card_flip_out_right)
            flipInAnimator = AnimatorInflater.loadAnimator(context, R.animator.card_flip_in_right)
            moveInAnimator = AnimatorInflater.loadAnimator(context, R.animator.card_move_in_left)
            moveOutAnimator = AnimatorInflater.loadAnimator(context, R.animator.card_move_out_left)
        }
        else {
            flipOutAnimator = AnimatorInflater.loadAnimator(context, R.animator.card_flip_out_left)
            flipInAnimator = AnimatorInflater.loadAnimator(context, R.animator.card_flip_in_left)
            moveInAnimator = AnimatorInflater.loadAnimator(context, R.animator.card_move_in_right)
            moveOutAnimator = AnimatorInflater.loadAnimator(context, R.animator.card_move_out_right)
        }

        flipOutAnimator.setTarget(backView)
        flipInAnimator.setTarget(frontView)
        moveOutAnimator.setTarget(backView)
        moveInAnimator.setTarget(frontView)

        frontView.cameraDistance = cameraDistance
        backView.cameraDistance = cameraDistance

        flipOutAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                setCardSide(false)
                flipInAnimator.start()
                moveInAnimator.start()
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        flipInAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                // reset backside to initial position
                moveInAnimator.setTarget(backView)
                flipInAnimator.setTarget(backView)

                // prevent infinite looping of animation
                moveInAnimator.removeAllListeners()
                flipInAnimator.removeAllListeners()

                moveInAnimator.start()
                flipInAnimator.start()
            }
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        moveOutAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                CoroutineScope(Dispatchers.Main).launch {
                    // start rotating shortly after card moves
                    delay(50)
                    flipOutAnimator.start()
                }
            }
            override fun onAnimationEnd(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        // start first animation
        moveOutAnimator.start()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = event.x
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (backside) {
                    val deltaX = event.x - initialX
                    if (deltaX > SWIPE_THRESHOLD) {
                        swipeListener?.onSwipeRight()
                        flipCard("right")
                    } else if (deltaX < -SWIPE_THRESHOLD) {
                        swipeListener?.onSwipeLeft()
                        flipCard("left")
                    }
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    companion object {
        private const val SWIPE_THRESHOLD = 100
    }
}