package fi.sabriina.urbanhuikka.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import fi.sabriina.urbanhuikka.DARE_DECK
import fi.sabriina.urbanhuikka.TAG
import fi.sabriina.urbanhuikka.TRUTH_DECK
import fi.sabriina.urbanhuikka.card.Card
import fi.sabriina.urbanhuikka.helpers.DbConstants
import fi.sabriina.urbanhuikka.roomdb.GameState
import fi.sabriina.urbanhuikka.roomdb.ScoreboardEntry
import fi.sabriina.urbanhuikka.roomdb.Player
import fi.sabriina.urbanhuikka.repository.GameStateRepository
import fi.sabriina.urbanhuikka.repository.GameStateRepositoryInterface
import fi.sabriina.urbanhuikka.roomdb.CardCategory
import fi.sabriina.urbanhuikka.roomdb.PlayerAndScore

class GameStateViewModel (private val repository: GameStateRepositoryInterface): ViewModel() {
    private var truthCards = mutableListOf<Card>()
    private var dareCards = mutableListOf<Card>()
    private var playerList = listOf<Player>()

    private var truthCardIndex = 0
    private var dareCardIndex = 0

    private var currentPlayerIndex = 0
    private var _currentPlayer = MutableLiveData<Player>()
    var currentPlayer: LiveData<Player> = _currentPlayer

    // this hardcoded value is never used, but lateinit is not allowed
    private var pointsToWin: Int = 30

    suspend fun initializeDatabase() {
        Log.d(TAG,"Initializing the database")
        for (category in DbConstants.DARE_CATEGORIES) {
            insertCardCategory(category)
        }
        for (category in DbConstants.TRUTH_CATEGORIES) {
            insertCardCategory(category)
        }
    }

    suspend fun checkInitialization() {
        val count = repository.getGameCount()
        Log.d(TAG, count.toString())
        if (count != 1 || getCurrentGame().status !in arrayOf("INITIALIZED", "ONGOING", "SAVED"))  {
            initializeDatabase()
            deleteAllGames()
            insertGameState(GameState(0,"INITIALIZED"))
        }
    }

    suspend fun startNewGame() {
        deleteAllGames()
        deleteAllPlayersFromScoreboard()
        insertGameState(GameState(0,"PLAYER_SELECT"))
    }

    private suspend fun insertCardCategory(name: String) {
        repository.insertCardCategory(CardCategory(0, name, true))
    }

    suspend fun startGame() {
        playerList = repository.getPlayers()
        currentPlayerIndex = repository.getCurrentPlayerIndex()
        _currentPlayer.value = playerList[currentPlayerIndex]
        updateDatabase()
        Log.d(TAG, playerList.toString())
        pointsToWin = repository.getPointsToWin()
        updateGameStatus("ONGOING")

        Log.d("Huikkasofta", "at the end of startGame()")

    }

    suspend fun continueGame() {
        startGame()
    }

    private suspend fun updateDatabase() {
        val enabledCategories = repository.getEnabledCardCategories()
        val (truthCardList, dareCardList) = repository.updateDatabase(enabledCategories)
        truthCards = truthCardList
        dareCards = dareCardList
        Log.d(TAG, "Truth cards: ${truthCards.size}")
        Log.d(TAG, "Dare cards: ${dareCards.size}")
        shuffleCards(TRUTH_DECK)
        shuffleCards(DARE_DECK)
    }

    private fun checkRemainingCards() {
        if (truthCardIndex == truthCards.size) {
            shuffleCards(TRUTH_DECK)
        }

        if (dareCardIndex == dareCards.size) {
            shuffleCards(DARE_DECK)
        }
    }

    private fun shuffleCards(deck: String) {
        if (deck == TRUTH_DECK) {
            if (truthCards.size < 1) {
                Log.w(TAG, "No $deck cards to shuffle")
                return
            }
            truthCardIndex = 0
            truthCards.shuffle()
        }
        else if (deck == DARE_DECK) {
            if (dareCards.size < 1) {
                Log.w(TAG, "No $deck cards to shuffle")
                return
            }
            dareCardIndex = 0
            dareCards.shuffle()
        }
        Log.d(TAG, "Shuffled $deck deck")
    }

    suspend fun getNextCard(deck: String) : Card? {
        var selectedCard: Card? = null
        if (deck == TRUTH_DECK) {
            truthCardIndex += 1
            selectedCard = truthCards[truthCardIndex-1]
        }
        else if (deck == DARE_DECK) {
            dareCardIndex += 1
            selectedCard = dareCards[dareCardIndex-1]
        }
        updateSelectedCard(selectedCard)
        return selectedCard
    }

    suspend fun endTurn() {
        checkRemainingCards()
        updateCurrentPlayer()
        updateSelectedCard(null)
    }

    suspend fun addPoints(playerId: Int = playerList[currentPlayerIndex].id, amount: Int) {
        var score = repository.getPlayerScore(playerId)
        score += amount
        repository.updatePlayerScore(playerId, score)
    }

    suspend fun checkWinner(): Player? {
        for (entry in getAllScores()) {
            if (entry.score >= pointsToWin) {
                updateGameStatus("ENDED")
                return entry.player
            }
        }
        return null
    }

    suspend fun getAllScores() : List<PlayerAndScore> {
        return repository.getAllScores()
    }

    private suspend fun insertGameState(gameState: GameState) {
        repository.insertGameState(gameState)
    }

    suspend fun getCurrentGame() : GameState {
        return repository.getCurrentGame()
    }

    suspend fun getSelectedCard() : Card? {
        return repository.getSelectedCard()
    }

    suspend fun updateSelectedCard(card: Card?) {
        repository.updateSelectedCard(card)
    }

    suspend fun checkSavedGameExists(): Boolean {
        if (getCurrentGame().status == "SAVED") {
            return true
        }
        return false
    }

    suspend fun updateGameStatus(status: String) {
        repository.updateGameState(status)

        Log.d(TAG, "Updated game status to: $status")
    }

    suspend fun insertPlayerToScoreboard(scoreboardEntry: ScoreboardEntry) {
        repository.insertPlayerToScoreboard(scoreboardEntry)
    }

    private suspend fun updateCurrentPlayer() {
        if (currentPlayerIndex < playerList.size - 1) {
            currentPlayerIndex += 1
        }
        else {
            currentPlayerIndex = 0
        }
        repository.updateCurrentPlayerIndex(currentPlayerIndex)
        _currentPlayer.value = playerList[currentPlayerIndex]
    }

    private suspend fun deleteAllGames() {
        repository.deleteAllGames()
    }

    private suspend fun deleteAllPlayersFromScoreboard() {
        repository.deleteAllPlayersFromScoreboard()
    }

    suspend fun setEnabledCardCategories(enabledCategories: List<String>) {
        val allCategories = DbConstants.DARE_CATEGORIES + DbConstants.TRUTH_CATEGORIES
        for (category in allCategories) {
            if (category in enabledCategories) {
                repository.setCardCategoryEnabled(category, true)
            } else {
                repository.setCardCategoryEnabled(category, false)
            }
        }
    }

    suspend fun setPointsToWin(points: Int) {
        repository.setPointsToWin(points)
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