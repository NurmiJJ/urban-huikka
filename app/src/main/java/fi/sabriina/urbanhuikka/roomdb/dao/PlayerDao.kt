package fi.sabriina.urbanhuikka.roomdb.dao

import androidx.room.*
import fi.sabriina.urbanhuikka.roomdb.Player
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {

    @Query("SELECT * FROM player_table ORDER BY name ASC")
    fun getAlphabetizedPlayers(): Flow<List<Player>>

    @Update
    suspend fun updatePlayer(player: Player)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(player: Player)

    @Query("DELETE FROM player_table")
    suspend fun deleteAll()

    @Delete
    suspend fun deletePlayer(player: Player)
}