package fi.sabriina.urbanhuikka.roomdb

import android.app.Application
import fi.sabriina.urbanhuikka.repository.GameStateRepository
import fi.sabriina.urbanhuikka.repository.PlayerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class HuikkaApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    private val database by lazy { HuikkaDb.getDatabase(this, applicationScope) }
    val playerRepository by lazy { PlayerRepository(database.playerDao()) }
    val gameStateRepository by lazy { GameStateRepository(database.gameStateDao()) }
}