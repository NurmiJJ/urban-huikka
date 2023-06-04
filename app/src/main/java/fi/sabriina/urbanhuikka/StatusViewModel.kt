package fi.sabriina.urbanhuikka

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel for showing selected options at Main UI
 */
class StatusViewModel : ViewModel() {

    val currentPlayer: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }
    val liveScore: MutableLiveData<Map<Int, Int>> by lazy {
        MutableLiveData<Map<Int, Int>>()
    }
}