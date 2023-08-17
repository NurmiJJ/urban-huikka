package fi.sabriina.urbanhuikka.repository

import fi.sabriina.urbanhuikka.roomdb.GameState
import fi.sabriina.urbanhuikka.card.Card
import fi.sabriina.urbanhuikka.roomdb.CardCategory
import fi.sabriina.urbanhuikka.roomdb.Player
import fi.sabriina.urbanhuikka.roomdb.PlayerAndScore
import fi.sabriina.urbanhuikka.roomdb.ScoreboardEntry

interface GameStateRepositoryInterface {

    suspend fun updateDatabase(enabledCategories: List<String>): Pair<MutableList<Card>, MutableList<Card>>

    suspend fun insertGameState(gameState: GameState)

    suspend fun insertPlayerToScoreboard(scoreboardEntry: ScoreboardEntry)

    suspend fun updateGameState(status: String)

    suspend fun updateCurrentPlayerIndex(index: Int)

    suspend fun updateAssistingPlayerIndex(index: Int)

    suspend fun updateSelectedCard(card: Card?)

    suspend fun getCurrentGame() : GameState

    suspend fun getGameCount() : Int

    suspend fun getPlayers(): List<Player>

    suspend fun getPlayerScore(playerId: Int) : Int

    suspend fun updatePlayerScore(playerId: Int, score: Int)

    suspend fun getAllScores() : List<PlayerAndScore>

    suspend fun getCurrentPlayerIndex(): Int

    suspend fun getAssistingPlayerIndex(): Int

    suspend fun getSelectedCard() : Card?

    suspend fun deleteAllGames()

    suspend fun deleteAllPlayersFromScoreboard()

    suspend fun insertCardCategory(cardCategory: CardCategory)

    suspend fun setCardCategoryEnabled(name: String, enabled: Boolean)

    suspend fun getEnabledCardCategories(): List<String>

    suspend fun setPointsToWin(points: Int)

    suspend fun getPointsToWin(): Int
}