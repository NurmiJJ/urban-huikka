package fi.sabriina.urbanhuikka.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fi.sabriina.urbanhuikka.TAG
import fi.sabriina.urbanhuikka.card.Card
import fi.sabriina.urbanhuikka.roomdb.GameState
import fi.sabriina.urbanhuikka.roomdb.ScoreboardEntry
import fi.sabriina.urbanhuikka.roomdb.Player
import fi.sabriina.urbanhuikka.repository.GameStateRepository
import fi.sabriina.urbanhuikka.repository.GameStateRepositoryInterface
import fi.sabriina.urbanhuikka.roomdb.PlayerAndScore
import kotlinx.coroutines.launch

const val DareCollection = "DareCards"
const val TruthCollection = "TruthCards"

class GameStateViewModel (private val repository: GameStateRepositoryInterface): ViewModel() {
    private var truthCards = mutableListOf<Card>()
    private var dareCards = mutableListOf<Card>()
    private var playerList = listOf<Player>()

    private var truthCardIndex = 0
    private var dareCardIndex = 0

    private var currentPlayerIndex = 0
    private var _currentPlayer = MutableLiveData<Player>()
    var currentPlayer: LiveData<Player> = _currentPlayer

    fun initializeDatabase() {
        deleteAllGames()
        deleteAllPlayersFromScoreboard()
        insertGameState(GameState(0,"INITIALIZED",0))
    }

    suspend fun checkInitialization() {
        val count = repository.getGameCount()
        if (count != 1 || getCurrentGame().status !in arrayOf("INITIALIZED", "ONGOING", "SAVED"))  {
            initializeDatabase()
        }
    }

    fun startGame() = viewModelScope.launch {
        updateDatabase()
        playerList = getPlayers()
        updateGameStatus("ONGOING")
        shuffleCards("truth")
        shuffleCards("dare")
        currentPlayerIndex = repository.getCurrentPlayerIndex()
        _currentPlayer.value = playerList[currentPlayerIndex]
        Log.d("Huikkasofta", "startGame()")
    }

    private suspend fun getPlayers() : List<Player> {
        return repository.getPlayers()
    }

    private fun updateDatabase() {
        val (truthCardList, dareCardList) = repository.updateDatabase()
        truthCards = truthCardList
        dareCards = dareCardList

    }

    private fun checkRemainingCards() {
        if (truthCardIndex == truthCards.size - 1) {
            truthCardIndex = 0
            truthCards.shuffle()
        }

        if (dareCardIndex == dareCards.size - 1) {
            dareCardIndex = 0
            dareCards.shuffle()
        }
    }

    private fun shuffleCards(deck: String) {
        if (deck == "truth") {
            truthCardIndex = 0
            truthCards.shuffle()
        }
        else if (deck == "dare") {
            dareCardIndex = 0
            dareCards.shuffle()
        }
    }

    fun getNextCard(deck: String) : Card? {
        if (deck == "truth") {
            truthCardIndex += 1
            return truthCards[truthCardIndex]
        }
        if (deck == "dare") {
            dareCardIndex += 1
            return dareCards[dareCardIndex]
        }
        return null
    }

    fun endTurn() {
        checkRemainingCards()
        updateCurrentPlayer()
    }

    suspend fun addPoints(playerId: Int = playerList[currentPlayerIndex].id, amount: Int) {
        var score = repository.getPlayerScore(playerId)
        score += amount
        repository.updatePlayerScore(playerId, score)
    }

    suspend fun getAllScores() : List<PlayerAndScore> {
        return repository.getAllScores()
    }

    private fun insertGameState(gameState: GameState) = viewModelScope.launch {
        repository.insertGameState(gameState)
    }

    suspend fun getCurrentGame() : GameState {
        return repository.getCurrentGame()
    }

    suspend fun checkSavedGameExists(): Boolean {
        if (getCurrentGame().status == "SAVED") {
            return true
        }
        return false
    }

    fun updateGameStatus(status: String) = viewModelScope.launch {
        repository.updateGameState(status)

        Log.d(TAG, "Updated game status to: $status")
    }

    fun insertPlayerToScoreboard(scoreboardEntry: ScoreboardEntry) = viewModelScope.launch {
        repository.insertPlayerToScoreboard(scoreboardEntry)
    }

    private fun updateCurrentPlayer() = viewModelScope.launch {
        if (currentPlayerIndex < playerList.size - 1) {
            currentPlayerIndex += 1
        }
        else {
            currentPlayerIndex = 0
        }
        repository.updateCurrentPlayerIndex(currentPlayerIndex)
        _currentPlayer.value = playerList[currentPlayerIndex]
    }

    private fun deleteAllGames() = viewModelScope.launch {
        repository.deleteAllGames()
    }

    private fun deleteAllPlayersFromScoreboard() = viewModelScope.launch {
        repository.deleteAllPlayersFromScoreboard()
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