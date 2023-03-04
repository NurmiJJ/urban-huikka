package fi.sabriina.urbanhuikka.card

const val LOREM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Mauris ultricies mattis nulla eget fringilla. Integer facilisis leo nibh, sed viverra arcu condimentum nec. Pellentesque et consequat mi. In in mattis dui. Suspendisse elementum, ante sit amet fringilla rhoncus, nisl ante tempor mauris, sit amet sollicitudin magna nisl ornare lacus."


data class Card(
    val title: String = "",
    val chapter: String = LOREM,
    val addInfo: String = "",
    val points: Int = 1

)