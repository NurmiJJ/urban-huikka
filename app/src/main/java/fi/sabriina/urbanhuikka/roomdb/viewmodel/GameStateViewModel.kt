package fi.sabriina.urbanhuikka.roomdb.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import fi.sabriina.urbanhuikka.roomdb.GameState
import fi.sabriina.urbanhuikka.roomdb.repository.GameStateRepository
import kotlinx.coroutines.launch

class GameStateViewModel (private val repository: GameStateRepository): ViewModel() {

    var gameStatus: MutableLiveData<GameState> = repository.gameStatus.asLiveData() as MutableLiveData<GameState>

    fun insertGameState(gameState: GameState) = viewModelScope.launch {
        repository.insertGameState(gameState)
    }

    fun deleteGameState(gameState: GameState) = viewModelScope.launch {
        repository.deleteGameState(gameState)
    }

    fun updateGameStatus( status: String) = viewModelScope.launch {
        gameStatus.value?.status = status
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