package fi.sabriina.urbanhuikka.repository

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import fi.sabriina.urbanhuikka.TAG
import fi.sabriina.urbanhuikka.card.Card
import fi.sabriina.urbanhuikka.roomdb.CardCategory
import fi.sabriina.urbanhuikka.roomdb.ScoreboardEntry
import fi.sabriina.urbanhuikka.roomdb.GameState
import fi.sabriina.urbanhuikka.roomdb.Player
import fi.sabriina.urbanhuikka.roomdb.PlayerAndScore
import fi.sabriina.urbanhuikka.roomdb.dao.GameStateDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

const val DareCollection = "DareCards"
const val TruthCollection = "TruthCards"

class GameStateRepository(private val gameStateDao: GameStateDao) : GameStateRepositoryInterface {

    private val database = Firebase.firestore

    override suspend fun updateDatabase(enabledCategories: List<String>) : Pair<MutableList<Card>, MutableList<Card>> {
        return suspendCoroutine { continuation ->
            val truthCardList = mutableListOf<Card>()
            val dareCardList = mutableListOf<Card>()

            val scope = CoroutineScope(Dispatchers.Main) // Use the appropriate dispatcher

            val truthJob = scope.launch {
                database.collection(TruthCollection)
                    .addSnapshotListener { value, e ->
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e)
                            continuation.resumeWithException(e)
                            return@addSnapshotListener
                        }
                        truthCardList.clear()
                        for (doc in value!!) {
                            val card: Card = doc.toObject(Card::class.java)
                            if (card.category in enabledCategories) {
                                truthCardList.add(card)
                            }
                        }
                        Log.d(TAG, "Truth cards update complete")
                        if (dareCardList.isNotEmpty()) {
                            continuation.resume(Pair(truthCardList, dareCardList))
                        }
                    }
            }

            val dareJob = scope.launch {
                database.collection(DareCollection)
                    .addSnapshotListener { value, e ->
                        if (e != null) {
                            Log.w(TAG, "Listen failed.", e)
                            continuation.resumeWithException(e)
                            return@addSnapshotListener
                        }
                        dareCardList.clear()
                        for (doc in value!!) {
                            val card: Card = doc.toObject(Card::class.java)
                            if (card.category in enabledCategories) {
                                dareCardList.add(card)
                            }
                        }
                        Log.d(TAG, "Dare cards update complete")
                        if (truthCardList.isNotEmpty()) {
                            continuation.resume(Pair(truthCardList, dareCardList))
                        }
                    }
            }

            // Combine both jobs to wait for them to complete
            scope.launch {
                truthJob.join()
                dareJob.join()
            }
        }
    }

    override suspend fun insertGameState(gameState: GameState) {
        gameStateDao.insertGameState(gameState)
    }

    override suspend fun insertPlayerToScoreboard(scoreboardEntry: ScoreboardEntry) {
        gameStateDao.insertPlayerToScoreboard(scoreboardEntry)
    }

    override suspend fun updateGameState(status: String) {
        gameStateDao.updateGameStatus(status)
    }

    override suspend fun updateCurrentPlayerIndex(index: Int) {
        gameStateDao.updateCurrentPlayerIndex(index)
    }

    override suspend fun updateAssistingPlayerIndex(index: Int) {
        gameStateDao.updateAssistingPlayerIndex(index)
    }

    override suspend fun updateSelectedCard(card: Card?) {
        gameStateDao.updateSelectedCard(card)
    }

    override suspend fun getCurrentGame() : GameState {
        return gameStateDao.getCurrentGame()
    }
    override suspend fun getGameCount() : Int {
        return gameStateDao.getGameCount()
    }

    override suspend fun getPlayers(): List<Player> {
        return gameStateDao.getPlayers()
    }

    override suspend fun getPlayerScore(playerId: Int) : Int {
        return gameStateDao.getPlayerScore(playerId)
    }

    override suspend fun updatePlayerScore(playerId: Int, score: Int) {
        gameStateDao.updatePlayerScore(playerId, score)
    }

    override suspend fun getAllScores(): List<PlayerAndScore> {
        return gameStateDao.getAllScores()
    }

    override suspend fun getCurrentPlayerIndex(): Int {
        return gameStateDao.getCurrentPlayerIndex()
    }

    override suspend fun getAssistingPlayerIndex(): Int {
        return gameStateDao.getAssistingPlayerIndex()
    }

    override suspend fun getSelectedCard(): Card? {
        return gameStateDao.getSelectedCard()
    }

    override suspend fun deleteAllGames() {
        gameStateDao.deleteAllGames()
    }

    override suspend fun deleteAllPlayersFromScoreboard() {
        gameStateDao.deleteAllPlayersFromScoreboard()
    }

    override suspend fun insertCardCategory(cardCategory: CardCategory) {
        gameStateDao.insertCardCategory(cardCategory)
    }

    override suspend fun setCardCategoryEnabled(name: String, enabled: Boolean) {
        gameStateDao.setCardCategoryEnabled(name, enabled)
    }

    override suspend fun getEnabledCardCategories(): List<String> {
        return gameStateDao.getEnabledCardCategories()
    }

    override suspend fun setPointsToWin(points: Int) {
        gameStateDao.setPointsToWin(points)
    }

    override suspend fun getPointsToWin(): Int {
        return gameStateDao.getPointsToWin()
    }
}