package fi.sabriina.urbanhuikka.roomdb.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import fi.sabriina.urbanhuikka.card.Card
import fi.sabriina.urbanhuikka.helpers.DbConstants
import fi.sabriina.urbanhuikka.roomdb.CardCategory
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

    @Test
    fun updateCurrentPlayerIndex() = runTest {
        dao.insertGameState(GameState(0,"INITIALIZED",0))
        dao.updateCurrentPlayerIndex(1)
        assertThat(dao.getCurrentPlayerIndex()).isEqualTo(1)
        dao.updateCurrentPlayerIndex(2)
        assertThat(dao.getCurrentPlayerIndex()).isEqualTo(2)
    }

    @Test
    fun updatePlayerScore() = runTest {
        dao.insertPlayerToScoreboard(ScoreboardEntry(0,3,0))
        dao.insertPlayerToScoreboard(ScoreboardEntry(0,2,14))
        assertThat(dao.getPlayerScore(3)).isEqualTo(0)
        dao.updatePlayerScore(3,5)
        assertThat(dao.getPlayerScore(3)).isEqualTo(5)
        assertThat(dao.getPlayerScore(2)).isEqualTo(14)
    }

    @Test
    fun insertCardCategory() = runTest {
        dao.insertCardCategory(CardCategory(DbConstants.DARE_CATEGORIES[0], true))
        dao.insertCardCategory(CardCategory(DbConstants.TRUTH_CATEGORIES[1], true))
        val expectedList = listOf(DbConstants.DARE_CATEGORIES[0], DbConstants.TRUTH_CATEGORIES[1])
        assertThat(dao.getEnabledCardCategories()).containsExactlyElementsIn(expectedList)
    }

    @Test
    fun disableCardCategories() = runTest {
        dao.insertCardCategory(CardCategory(DbConstants.DARE_CATEGORIES[0], true))
        dao.insertCardCategory(CardCategory(DbConstants.TRUTH_CATEGORIES[1], true))
        dao.setCardCategoryEnabled(DbConstants.TRUTH_CATEGORIES[1], false)
        val expectedList = listOf(DbConstants.DARE_CATEGORIES[0])
        assertThat(dao.getEnabledCardCategories()).containsExactlyElementsIn(expectedList)

    }

    @Test
    fun getPointsToWin() = runTest {
        dao.insertGameState(GameState(0,"INITIALIZED",0))
        assertThat(dao.getPointsToWin()).isNotNull()
        dao.setPointsToWin(20)
        assertThat(dao.getPointsToWin()).isEqualTo(20)
    }

    @Test
    fun getCurrentCard() = runTest {
        dao.insertGameState(GameState(0,"INITIALIZED",0))
        assertThat(dao.getSelectedCard()).isNull()
        val card = Card("Haaveet ja unelmat","Milloin itkit viimeksi?",1)
        dao.updateSelectedCard(card)
        assertThat(dao.getSelectedCard()).isEqualTo(card)
    }
}