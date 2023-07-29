package fi.sabriina.urbanhuikka.repository

import fi.sabriina.urbanhuikka.roomdb.ScoreboardEntry
import fi.sabriina.urbanhuikka.roomdb.GameState
import fi.sabriina.urbanhuikka.roomdb.Player
import fi.sabriina.urbanhuikka.roomdb.PlayerAndScore
import fi.sabriina.urbanhuikka.roomdb.dao.GameStateDao


class GameStateRepository(private val gameStateDao: GameStateDao) : GameStateRepositoryInterface {

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
}