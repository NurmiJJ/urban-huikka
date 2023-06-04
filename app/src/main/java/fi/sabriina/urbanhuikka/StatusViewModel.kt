package fi.sabriina.urbanhuikka

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import fi.sabriina.urbanhuikka.player.Player
import kotlin.properties.ReadWriteProperty

/**
 * ViewModel for showing selected options at Main UI
 */
class StatusViewModel : ViewModel() {

    val currentPlayer: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>(0)
    }


}