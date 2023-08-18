package fi.sabriina.urbanhuikka.roomdb.dao

import androidx.room.*
import fi.sabriina.urbanhuikka.card.Card
import fi.sabriina.urbanhuikka.roomdb.CardCategory
import fi.sabriina.urbanhuikka.roomdb.ScoreboardEntry
import fi.sabriina.urbanhuikka.roomdb.GameState
import fi.sabriina.urbanhuikka.roomdb.Player
import fi.sabriina.urbanhuikka.roomdb.PlayerAndScore

@Dao
interface GameStateDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGameState(gameState: GameState)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlayerToScoreboard(scoreboardEntry: ScoreboardEntry)

    @Query("UPDATE game_state SET status = :status")
    suspend fun updateGameStatus(status: String)

    @Query("UPDATE game_state SET currentPlayerIndex = :index")
    suspend fun updateCurrentPlayerIndex(index: Int)

    @Query("UPDATE game_state SET assistingPlayerIndex = :index")
    suspend fun updateAssistingPlayerIndex(index: Int)

    @Query("UPDATE game_state SET selectedCard = :card")
    suspend fun updateSelectedCard(card: Card?)

    @Query("SELECT score FROM scoreboard WHERE playerId = :playerId")
    suspend fun getPlayerScore(playerId: Int): Int

    @Query("SELECT player_table.*, scoreboard.score FROM scoreboard INNER JOIN player_table ON player_table.id = scoreboard.playerId")
    suspend fun getAllScores(): List<PlayerAndScore>

    @Query("UPDATE scoreboard SET score = :score WHERE playerId = :playerId")
    suspend fun updatePlayerScore(playerId: Int, score: Int)

    @Query("SELECT COUNT(id) FROM game_state")
    suspend fun getGameCount(): Int

    @Query("DELETE FROM game_state")
    suspend fun deleteAllGames()

    @Query("DELETE FROM scoreboard")
    suspend fun deleteAllPlayersFromScoreboard()

    @Query("SELECT * FROM game_state LIMIT 1")
    suspend fun getCurrentGame(): GameState

    @Query("SELECT currentPlayerIndex FROM game_state")
    suspend fun getCurrentPlayerIndex(): Int

    @Query("SELECT assistingPlayerIndex FROM game_state")
    suspend fun getAssistingPlayerIndex(): Int

    @Query("SELECT selectedCard FROM game_state")
    suspend fun getSelectedCard(): Card?

    @Query("SELECT player_table.* FROM player_table INNER JOIN scoreboard ON player_table.id = scoreboard.playerId")
    suspend fun getPlayers(): List<Player>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCardCategory(cardCategory: CardCategory)

    @Query("UPDATE card_category SET enabled = :enabled WHERE name = :name")
    suspend fun setCardCategoryEnabled(name: String, enabled: Boolean)

    @Query("SELECT name FROM card_category WHERE enabled")
    suspend fun getEnabledCardCategories(): List<String>

    @Query("SELECT pointsToWin FROM game_state")
    suspend fun getPointsToWin(): Int

    @Query("UPDATE game_state SET pointsToWin = :points")
    suspend fun setPointsToWin(points: Int)
}