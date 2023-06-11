package fi.sabriina.urbanhuikka.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class PlayerViewModel(private val repository: PlayerRepository) : ViewModel() {

    // Using LiveData and caching what allWords returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allPlayers: LiveData<List<Player>> = repository.allPlayers.asLiveData()
    val gameStatus: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(player: Player) = viewModelScope.launch {
        repository.insert(player)
    }

    fun delete(player: Player) = viewModelScope.launch {
        repository.delete(player)
    }

    fun updatePoints(player: Player, pointsToAdd: Int) {
        val newPlayer = player.copy(currentPoints = player.currentPoints + pointsToAdd)
        updatePlayer(newPlayer)
    }

    fun resetPlayerPoints(player: Player){
        val newPlayer = player.copy(currentPoints = 0)
        updatePlayer(newPlayer)
    }

    fun insertGameState(gameState: GameState) = viewModelScope.launch {
        repository.insertGameState(gameState)
    }

    fun deleteGameState(gameState: GameState) = viewModelScope.launch {
        repository.deleteGameState(gameState)
    }

    fun getGameStatus(gameId: Int) {
        viewModelScope.launch {
            gameStatus.postValue(repository.getGameStatus(gameId))
        }
    }

    private fun updatePlayer(player: Player) = viewModelScope.launch {
        repository.updatePlayer(player)
    }

    fun updateGameStatus(id: Int, status: String) = viewModelScope.launch {
        repository.updateGameStatus(id, status)
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