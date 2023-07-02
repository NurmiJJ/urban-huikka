package fi.sabriina.urbanhuikka.roomdb.dao

import androidx.room.*
import fi.sabriina.urbanhuikka.roomdb.ScoreboardEntry
import fi.sabriina.urbanhuikka.roomdb.GameState
import fi.sabriina.urbanhuikka.roomdb.Player
import kotlinx.coroutines.flow.Flow

@Dao
interface GameStateDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGameState(gameState: GameState)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPlayerToScoreboard(scoreboardEntry: ScoreboardEntry)

    @Update
    suspend fun updateGameState(gameState: GameState)

    @Query("SELECT COUNT(id) FROM game_state")
    suspend fun checkInitialization() : Int

    @Query("DELETE FROM game_state")
    suspend fun deleteAllGames()

    @Query("DELETE FROM scoreboard")
    suspend fun deleteAllPlayersFromGames()

    @Query("SELECT * FROM game_state LIMIT 1")
    suspend fun getCurrentGame() : GameState

    @Query("SELECT currentPlayerIndex FROM game_state")
    fun getCurrentPlayerIndex() : Flow<Int>

    @Query("SELECT player_table.* FROM player_table INNER JOIN scoreboard ON player_table.id = scoreboard.playerId")
    suspend fun getPlayers(): List<Player>
}