package fi.sabriina.urbanhuikka.roomdb.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import fi.sabriina.urbanhuikka.roomdb.GameState
import fi.sabriina.urbanhuikka.roomdb.HuikkaDb
import fi.sabriina.urbanhuikka.roomdb.ScoreboardEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class GameStateDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: HuikkaDb
    private lateinit var dao: GameStateDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            HuikkaDb::class.java
        ).allowMainThreadQueries().build()
        dao = database.gameStateDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertGameState() = runTest {
        dao.insertGameState(GameState(0,"INITIALIZED",0))
        val currentGame = dao.getCurrentGame()
        assertThat(currentGame.status).isEqualTo("INITIALIZED")

    }

    @Test
    fun updateGameState() = runTest {
        dao.insertGameState(GameState(0,"INITIALIZED",0))
        dao.updateGameStatus("ONGOING")
        val currentGame = dao.getCurrentGame()
        assertThat(currentGame.status).isEqualTo("ONGOING")
    }

    @Test
    fun checkInitialization() = runTest {
        dao.insertGameState(GameState(0,"INITIALIZED",0))
        val gameCount = dao.getGameCount()
        assertThat(gameCount).isEqualTo(1)
    }

    @Test
    fun deleteAllGames() = runTest {
        dao.insertGameState(GameState(0,"INITIALIZED",0))
        dao.deleteAllGames()
        val gameCount = dao.getGameCount()
        assertThat(gameCount).isEqualTo(0)
    }

    @Test
    fun insertPlayerToScoreboard() = runTest {
        dao.insertPlayerToScoreboard(ScoreboardEntry(0,1,3))
        assertThat(dao.getPlayerScore(1)).isEqualTo(3)
    }

    @Test
    fun deleteAllPlayersFromScoreboard() = runTest {
        dao.deleteAllPlayersFromScoreboard()
        assertThat(dao.getAllScores()).isEmpty()
    }
}