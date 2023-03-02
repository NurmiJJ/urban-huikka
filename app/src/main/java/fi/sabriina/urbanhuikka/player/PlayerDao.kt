package fi.sabriina.urbanhuikka.player

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {

    @Query("SELECT * FROM player_table ORDER BY player ASC")
    fun getAlphabetizedPlayers(): Flow<List<Player>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(player: Player)

    @Query("DELETE FROM player_table")
    suspend fun deleteAll()

    @Delete
    suspend fun deletePlayer(player: Player)
}