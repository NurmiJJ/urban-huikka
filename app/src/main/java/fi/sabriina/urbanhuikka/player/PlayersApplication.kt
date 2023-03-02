package fi.sabriina.urbanhuikka.player

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class PlayersApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    private val database by lazy { PlayerRoomDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { PlayerRepository(database.playerDao()) }
}