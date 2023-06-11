package fi.sabriina.urbanhuikka.roomdb.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fi.sabriina.urbanhuikka.roomdb.GameState
import fi.sabriina.urbanhuikka.roomdb.repository.GameStateRepository
import kotlinx.coroutines.launch

class GameStateViewModel (private val repository: GameStateRepository): ViewModel() {

    val gameStatus: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
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

    fun updateGameStatus(id: Int, status: String) = viewModelScope.launch {
        repository.updateGameStatus(id, status)
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