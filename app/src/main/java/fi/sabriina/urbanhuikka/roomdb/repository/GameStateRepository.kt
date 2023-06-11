package fi.sabriina.urbanhuikka.roomdb.repository

import fi.sabriina.urbanhuikka.roomdb.GameState
import fi.sabriina.urbanhuikka.roomdb.dao.GameStateDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameStateRepository(private val gameStateDao: GameStateDao) {
    suspend fun insertGameState(gameState: GameState) {
        gameStateDao.insertGameState(gameState)
    }

    suspend fun updateGameStatus(id: Int, status: String) {
        return withContext(Dispatchers.IO) {
            val game = gameStateDao.getGameById(id)
            val newGame = game.copy(status = status)
            gameStateDao.updateGameState(newGame)
        }
    }

    suspend fun deleteGameState(gameState: GameState) {
        gameStateDao.deleteGameState(gameState)
    }

    suspend fun getGameStatus(gameId: Int): String {
        return withContext(Dispatchers.IO) {
            gameStateDao.getGameStatus(gameId)
        }
    }
}