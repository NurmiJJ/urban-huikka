package fi.sabriina.urbanhuikka.player

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {

    @Query("SELECT * FROM player_table ORDER BY player ASC")
    fun getAlphabetizedPlayers(): Flow<List<Player>>

    @Update
    suspend fun updatePlayer(player: Player)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(player: Player)

    @Query("DELETE FROM player_table")
    suspend fun deleteAll()

    @Delete
    suspend fun deletePlayer(player: Player)

    // Game state
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGameState(gameState: GameState)

    @Update
    suspend fun updateGameState(gameState: GameState)

    @Delete
    suspend fun deleteGameState(gameState: GameState)

    @Query("SELECT status FROM game_state WHERE id=:gameId")
    fun getGameStatus(gameId: Int): String

    @Query("SELECT * FROM game_state WHERE id=:gameId")
    fun getGameById(gameId: Int): GameState
}