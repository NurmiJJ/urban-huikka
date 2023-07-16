package fi.sabriina.urbanhuikka.repository

import fi.sabriina.urbanhuikka.roomdb.GameState
import fi.sabriina.urbanhuikka.roomdb.Player
import fi.sabriina.urbanhuikka.roomdb.ScoreboardEntry

class FakeGameStateRepository : GameStateRepositoryInterface {

    private var gameStateList = mutableListOf<GameState>()
    private var scoreboard = mutableListOf<ScoreboardEntry>()

    override suspend fun insertGameState(gameState: GameState) {
        gameStateList.add(gameState)
    }

    override suspend fun insertPlayerToScoreboard(scoreboardEntry: ScoreboardEntry) {
        scoreboard.add(scoreboardEntry)
    }

    override suspend fun updateGameState(gameState: GameState) {
        gameStateList[0] = gameState
    }

    override suspend fun getCurrentGame(): GameState {
        return gameStateList[0]
    }

    override suspend fun getGameCount(): Int {
        return gameStateList.size
    }

    override suspend fun getPlayers(): List<Player> {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrentPlayerIndex(): Int {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAllGames() {
        gameStateList.clear()
    }

    override suspend fun deleteAllPlayersFromScoreboard() {
        scoreboard.clear()
    }
}