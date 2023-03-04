package fi.sabriina.urbanhuikka

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater

import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import fi.sabriina.urbanhuikka.card.Card

 class CustomCard : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet) : super(context, attr)
    constructor(context: Context, attr: AttributeSet, defStyleAttr : Int) : super(context, attr, defStyleAttr )

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_card, this, true)
        init()
    }
     fun setCard(card: Card){
         val titleView = findViewById<TextView>(R.id.textViewHeader)
         val chapterView = findViewById<TextView>(R.id.textViewChapter)
         val addInfoView = findViewById<TextView>(R.id.textViewAddInfo)

         titleView.text = card.title
         chapterView.text = card.chapter
         addInfoView.text = card.addInfo
     }

     fun setBackside(){
         val titleView = findViewById<TextView>(R.id.textViewHeader)
         val chapterView = findViewById<TextView>(R.id.textViewChapter)
         val addInfoView = findViewById<TextView>(R.id.textViewAddInfo)

         titleView.text = null
         chapterView.text = null
         addInfoView.text = null
     }

    // Setup views
    private fun init() {
        setBackside()
    }
}