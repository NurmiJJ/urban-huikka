package fi.sabriina.urbanhuikka

import fi.sabriina.urbanhuikka.card.Card

fun getCard(type: String) : Card{
    return if (type == "truth") {
        Card("Totuuskortti")
    } else {
        Card("Tehtäväkortti")
    }

}