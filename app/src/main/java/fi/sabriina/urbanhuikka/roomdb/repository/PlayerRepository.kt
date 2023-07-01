package fi.sabriina.urbanhuikka.roomdb.repository

import androidx.annotation.WorkerThread
import fi.sabriina.urbanhuikka.roomdb.Player
import fi.sabriina.urbanhuikka.roomdb.dao.PlayerDao
import kotlinx.coroutines.flow.Flow

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class PlayerRepository(private val playerDao: PlayerDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allPlayers: Flow<List<Player>> = playerDao.getAlphabetizedPlayers()
    val selectedPlayers: Flow<List<Player>> = playerDao.getSelectedPlayers()


    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(player: Player) {
        playerDao.insert(player)
    }

    suspend fun delete(player: Player) {
        playerDao.deletePlayer(player)
    }

    suspend fun updatePlayer(player: Player) {
        playerDao.updatePlayer(player)
    }

}