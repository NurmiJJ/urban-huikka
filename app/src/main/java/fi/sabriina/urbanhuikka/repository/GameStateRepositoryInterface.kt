package fi.sabriina.urbanhuikka.repository

import fi.sabriina.urbanhuikka.roomdb.GameState
import fi.sabriina.urbanhuikka.card.Card
import fi.sabriina.urbanhuikka.roomdb.Player
import fi.sabriina.urbanhuikka.roomdb.PlayerAndScore
import fi.sabriina.urbanhuikka.roomdb.ScoreboardEntry

interface GameStateRepositoryInterface {

    fun updateDatabase(): Pair<MutableList<Card>, MutableList<Card>>

    suspend fun insertGameState(gameState: GameState)

    suspend fun insertPlayerToScoreboard(scoreboardEntry: ScoreboardEntry)

    suspend fun updateGameState(status: String)

    suspend fun updateCurrentPlayerIndex(index: Int)

    suspend fun getCurrentGame() : GameState

    suspend fun getGameCount() : Int

    suspend fun getPlayers(): List<Player>

    suspend fun getPlayerScore(playerId: Int) : Int

    suspend fun updatePlayerScore(playerId: Int, score: Int)

    suspend fun getAllScores() : List<PlayerAndScore>

    suspend fun getCurrentPlayerIndex(): Int

    suspend fun deleteAllGames()

    suspend fun deleteAllPlayersFromScoreboard()
}