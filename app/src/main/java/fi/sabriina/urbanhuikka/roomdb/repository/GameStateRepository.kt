package fi.sabriina.urbanhuikka.roomdb.repository

import fi.sabriina.urbanhuikka.roomdb.GameState
import fi.sabriina.urbanhuikka.roomdb.dao.GameStateDao
import kotlinx.coroutines.flow.Flow

class GameStateRepository(private val gameStateDao: GameStateDao) {


    val gameStatus: Flow<GameState> = gameStateDao.getCurrentGame()

    suspend fun insertGameState(gameState: GameState) {
        gameStateDao.insertGameState(gameState)
    }

    suspend fun deleteGameState(gameState: GameState) {
        gameStateDao.deleteGameState(gameState)
    }

    suspend fun updateGameState(gameState: GameState) {
        gameStateDao.updateGameState(gameState)
    }
}