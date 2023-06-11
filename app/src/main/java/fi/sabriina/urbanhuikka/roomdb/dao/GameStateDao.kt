package fi.sabriina.urbanhuikka.roomdb.dao

import androidx.room.*
import fi.sabriina.urbanhuikka.roomdb.GameState

@Dao
interface GameStateDao {
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