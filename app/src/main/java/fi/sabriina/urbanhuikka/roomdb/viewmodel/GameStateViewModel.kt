package fi.sabriina.urbanhuikka.roomdb.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import fi.sabriina.urbanhuikka.roomdb.GameState
import fi.sabriina.urbanhuikka.roomdb.ScoreboardEntry
import fi.sabriina.urbanhuikka.roomdb.Player
import fi.sabriina.urbanhuikka.roomdb.repository.GameStateRepository
import kotlinx.coroutines.launch

class GameStateViewModel (private val repository: GameStateRepository): ViewModel() {

    var currentPlayerIndex: LiveData<Int> = repository.currentPlayerIndex.asLiveData()

    private fun insertGameState(gameState: GameState) = viewModelScope.launch {
        repository.insertGameState(gameState)
    }

    fun insertPlayerToScoreboard(scoreboardEntry: ScoreboardEntry) = viewModelScope.launch {
        repository.insertPlayerToScoreboard(scoreboardEntry)
    }

    suspend fun getCurrentGame() : GameState {
        return repository.getCurrentGame()
    }

    fun updateGameStatus(status: String, timestamp: Long?) = viewModelScope.launch {
        val gameStateObject: GameState = if (timestamp != null) {
            getCurrentGame().copy(status = status, timestamp = timestamp)
        } else {
            getCurrentGame().copy(status = status)
        }
        repository.updateGameState(gameStateObject)
        Log.d("Huikkasofta", "Updated game status to: $status")
    }

    suspend fun checkInitialization() {
        val count = repository.checkInitialization()
        if (count != 1 || getCurrentGame().status != "INITIALIZED" && getCurrentGame().status != "ONGOING" ) {
            initializeDatabase()
        }
    }

    fun initializeDatabase() {
        deleteAllGames()
        deleteAllPlayersFromGames()
        insertGameState(GameState(0,"INITIALIZED",0))
        Log.d("Huikkasofta", "Initialized database")
    }

    fun updateCurrentPlayerIndex() = viewModelScope.launch {
        val index = getCurrentGame().currentPlayerIndex
        val players = getPlayers()
        val gameStateObject = getCurrentGame()
        if (index < players.size - 1) {
            repository.updateGameState(gameStateObject.copy(currentPlayerIndex = index + 1))
        }
        else {
            repository.updateGameState(gameStateObject.copy(currentPlayerIndex = 0))
        }
    }

    suspend fun getPlayers() : List<Player> {
        return repository.getPlayers()
    }

    private fun deleteAllGames() = viewModelScope.launch {
        repository.deleteAllGames()
    }

    fun deleteAllPlayersFromGames() = viewModelScope.launch {
        repository.deleteAllPlayersFromGames()
    }
}

class GameStateViewModelFactory(private val repository: GameStateRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameStateViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GameStateViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}