package fi.sabriina.urbanhuikka.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import fi.sabriina.urbanhuikka.roomdb.Player
import fi.sabriina.urbanhuikka.repository.PlayerRepository
import kotlinx.coroutines.launch


class PlayerViewModel(private val repository: PlayerRepository) : ViewModel() {

    // Using LiveData and caching what allWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allPlayers: LiveData<List<Player>> = repository.allPlayers.asLiveData()

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(player: Player) = viewModelScope.launch {
        repository.insert(player)
    }

    fun delete(player: Player) = viewModelScope.launch {
        repository.delete(player)
    }

    private fun updatePlayer(player: Player) = viewModelScope.launch {
        repository.updatePlayer(player)
    }
}

class PlayerViewModelFactory(private val repository: PlayerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlayerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}