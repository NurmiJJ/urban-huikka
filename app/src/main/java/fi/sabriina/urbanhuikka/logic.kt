package fi.sabriina.urbanhuikka

import fi.sabriina.urbanhuikka.card.Card
import kotlin.random.Random

fun getCard(mutableList: MutableList<Card>) : Card{
    val randomIndex = Random.nextInt(mutableList.size)
    return mutableList[randomIndex]
}
