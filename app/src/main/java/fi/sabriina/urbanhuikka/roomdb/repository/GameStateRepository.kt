package fi.sabriina.urbanhuikka.roomdb.repository

import fi.sabriina.urbanhuikka.roomdb.ScoreboardEntry
import fi.sabriina.urbanhuikka.roomdb.GameState
import fi.sabriina.urbanhuikka.roomdb.Player
import fi.sabriina.urbanhuikka.roomdb.dao.GameStateDao


class GameStateRepository(private val gameStateDao: GameStateDao) {

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
    suspend fun getGameCount() : Int {
        return gameStateDao.getGameCount()
    }

    suspend fun getPlayers(): List<Player> {
        return gameStateDao.getPlayers()
    }

    suspend fun getCurrentPlayerIndex(): Int {
        return gameStateDao.getCurrentPlayerIndex()
    }

    suspend fun deleteAllGames() {
        gameStateDao.deleteAllGames()
    }

    suspend fun deleteAllPlayersFromScoreboard() {
        gameStateDao.deleteAllPlayersFromScoreboard()
    }
}