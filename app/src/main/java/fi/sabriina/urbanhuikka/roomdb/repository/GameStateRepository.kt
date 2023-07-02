package fi.sabriina.urbanhuikka.roomdb.repository

import fi.sabriina.urbanhuikka.roomdb.ScoreboardEntry
import fi.sabriina.urbanhuikka.roomdb.GameState
import fi.sabriina.urbanhuikka.roomdb.Player
import fi.sabriina.urbanhuikka.roomdb.dao.GameStateDao
import kotlinx.coroutines.flow.Flow


class GameStateRepository(private val gameStateDao: GameStateDao) {

    val currentPlayerIndex: Flow<Int> = gameStateDao.getCurrentPlayerIndex()
    suspend fun insertGameState(gameState: GameState) {
        gameStateDao.insertGameState(gameState)
    }

    suspend fun insertPlayerToScoreboard(scoreboardEntry: ScoreboardEntry) {
        gameStateDao.insertPlayerToScoreboard(scoreboardEntry)
    }

    suspend fun updateGameState(gameState: GameState) {
        gameStateDao.updateGameState(gameState)
    }

    suspend fun getCurrentGame() : GameState {
        return gameStateDao.getCurrentGame()
    }
    suspend fun checkInitialization() : Int {
        return gameStateDao.checkInitialization()
    }

    suspend fun getPlayers(): List<Player> {
        return gameStateDao.getPlayers()
    }

    suspend fun deleteAllGames() {
        gameStateDao.deleteAllGames()
    }

    suspend fun deleteAllPlayersFromGames() {
        gameStateDao.deleteAllPlayersFromGames()
    }
}