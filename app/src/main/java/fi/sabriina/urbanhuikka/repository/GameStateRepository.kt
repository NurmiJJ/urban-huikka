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

const val DareCollection = "DareCards"
const val TruthCollection = "TruthCards"

class GameStateRepository(private val gameStateDao: GameStateDao) : GameStateRepositoryInterface {

    private val database = Firebase.firestore

    override fun updateDatabase(enabledCategories: List<String>) : Pair<MutableList<Card>, MutableList<Card>> {
        val truthCardList = mutableListOf<Card>()
        val dareCardList = mutableListOf<Card>()
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
                    if (card.category in enabledCategories) {
                        truthCardList.add(card)
                        counter += 1
                    }
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
                    if (card.category in enabledCategories) {
                        dareCardList.add(card)
                        counter += 1
                    }
                }
                Log.d("Huikkasofta", "Added $counter dare cards")
            }
        return Pair(truthCardList, dareCardList)
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