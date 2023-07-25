package fi.sabriina.urbanhuikka.repository

import fi.sabriina.urbanhuikka.roomdb.GameState
import fi.sabriina.urbanhuikka.roomdb.Player
import fi.sabriina.urbanhuikka.roomdb.ScoreboardEntry

interface GameStateRepositoryInterface {

    suspend fun insertGameState(gameState: GameState)

    suspend fun insertPlayerToScoreboard(scoreboardEntry: ScoreboardEntry)

    suspend fun updateGameState(status: String)

    suspend fun getCurrentGame() : GameState

    suspend fun getGameCount() : Int

    suspend fun getPlayers(): List<Player>

    suspend fun getCurrentPlayerIndex(): Int

    suspend fun deleteAllGames()

    suspend fun deleteAllPlayersFromScoreboard()

    suspend fun updateCurrentPlayerIndex(index: Int)
}