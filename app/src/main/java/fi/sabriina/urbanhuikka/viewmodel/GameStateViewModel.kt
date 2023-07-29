package fi.sabriina.urbanhuikka.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import fi.sabriina.urbanhuikka.TAG
import fi.sabriina.urbanhuikka.card.Card
import fi.sabriina.urbanhuikka.roomdb.GameState
import fi.sabriina.urbanhuikka.roomdb.ScoreboardEntry
import fi.sabriina.urbanhuikka.roomdb.Player
import fi.sabriina.urbanhuikka.repository.GameStateRepository
import fi.sabriina.urbanhuikka.roomdb.PlayerAndScore
import kotlinx.coroutines.launch

const val DareCollection = "DareCards"
const val TruthCollection = "TruthCards"

class GameStateViewModel (private val repository: GameStateRepository): ViewModel() {
    private val database = Firebase.firestore
    private var truthCardList = mutableListOf<Card>()
    private var dareCardList = mutableListOf<Card>()
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
        Log.d("Huikkasofta", "Initialized database")
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
    }

    private suspend fun getPlayers() : List<Player> {
        return repository.getPlayers()
    }

    private fun updateDatabase() {
        database.collection(TruthCollection)
            .addSnapshotListener { value, e ->
                var counter = 0
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }
                truthCardList.clear()
                for (doc in value!!) {
                    val card: Card = doc.toObject(Card::class.java)
                    truthCardList.add(card)
                    counter += 1
                }
                Log.d("Huikkasofta", "Added $counter truth cards")
            }

        database.collection(DareCollection)
            .addSnapshotListener { value, e ->
                var counter = 0
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }
                dareCardList.clear()
                for (doc in value!!) {
                    val card : Card = doc.toObject(Card::class.java)
                    dareCardList.add(card)
                    counter += 1
                }
                Log.d("Huikkasofta", "Added $counter dare cards")
            }
        Log.d(TAG, truthCardList.toString())
        Log.d(TAG, dareCardList.toString())
    }

    private fun checkRemainingCards() {
        if (truthCardIndex == truthCardList.size - 1) {
            truthCardIndex = 0
            truthCardList.shuffle()
        }

        if (dareCardIndex == dareCardList.size - 1) {
            dareCardIndex = 0
            dareCardList.shuffle()
        }
    }

    private fun shuffleCards(deck: String) {
        if (deck == "truth") {
            truthCardIndex = 0
            truthCardList.shuffle()
        }
        else if (deck == "dare") {
            dareCardIndex = 0
            dareCardList.shuffle()
        }
    }

    fun getNextCard(deck: String) : Card? {
        if (deck == "truth") {
            truthCardIndex += 1
            return truthCardList[truthCardIndex]
        }
        if (deck == "dare") {
            dareCardIndex += 1
            return dareCardList[dareCardIndex]
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