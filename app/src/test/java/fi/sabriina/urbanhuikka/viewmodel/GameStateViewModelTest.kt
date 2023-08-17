package fi.sabriina.urbanhuikka.viewmodel

import android.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import fi.sabriina.urbanhuikka.card.Card
import fi.sabriina.urbanhuikka.getOrAwaitValue
import fi.sabriina.urbanhuikka.helpers.DbConstants
import fi.sabriina.urbanhuikka.repository.FakeGameStateRepository
import fi.sabriina.urbanhuikka.repository.GameStateRepositoryInterface
import fi.sabriina.urbanhuikka.roomdb.Player
import fi.sabriina.urbanhuikka.roomdb.ScoreboardEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class GameStateViewModelTest {

    private lateinit var viewModel : GameStateViewModel
    private lateinit var repository: GameStateRepositoryInterface

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        repository = FakeGameStateRepository()
        viewModel = GameStateViewModel(repository)
        Dispatchers.setMain(testDispatcher)
        initGameWithTestDefaults()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun initGameWithTestDefaults() = runTest {
        viewModel.initializeDatabase()
        viewModel.startNewGame()
        for (i in 1 .. repository.getPlayers().size) {
            viewModel.insertPlayerToScoreboard(ScoreboardEntry(0,i))
        }
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun `cards and current player exist after starting game`() = runTest {
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        val truthCard = viewModel.getNextCard("truth")
        val dareCard = viewModel.getNextCard("dare")
        assertThat(truthCard).isNotNull()
        assertThat(dareCard).isNotNull()
        val currentPlayer = viewModel.currentPlayer.getOrAwaitValue()
        assertThat(currentPlayer).isEqualTo(Player(1,"Eetu",123))
    }

    @Test
    fun `getting next card returns different cards`() = runTest {
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        val cards = mutableListOf<Card>()
        for (i in 1..3) {
            val currentCard = viewModel.getNextCard("truth")
            if (currentCard != null) {
                cards.add(currentCard)
            }
            viewModel.endTurn()
        }
        assertThat(cards).containsNoDuplicates()
    }

    @Test
    fun `end turn changes current player`() = runTest {
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.endTurn()
        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(repository.getCurrentPlayerIndex()).isEqualTo(1)

        for (i in 1 until repository.getPlayers().size) {
            viewModel.endTurn()
            testDispatcher.scheduler.advanceUntilIdle()
        }
        assertThat(repository.getCurrentPlayerIndex()).isEqualTo(0)
    }

    @Test
    fun `add points`() = runTest {
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.addPoints(amount = 2)
        viewModel.addPoints(3, 5)

        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(repository.getPlayerScore(1)).isEqualTo(2)
        assertThat(repository.getPlayerScore(3)).isEqualTo(5)
    }

    @Test
    fun `update and check game status`() = runTest {
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.checkSavedGameExists()).isEqualTo(false)

        viewModel.updateGameStatus("SAVED")

        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.checkSavedGameExists()).isEqualTo(true)
    }

    @Test
    fun `enable only one card category`() = runTest {
        viewModel.setEnabledCardCategories(listOf(DbConstants.TRUTH_CATEGORIES[1]))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()

        for (i in 1..5) {
            val currentCard = viewModel.getNextCard("truth")
            testDispatcher.scheduler.advanceUntilIdle()
            if (currentCard != null) {
                assertThat(currentCard.category).isEqualTo(DbConstants.TRUTH_CATEGORIES[1])
            }
            viewModel.endTurn()
        }
    }

    @Test
    fun `game ends with correct player winning`() = runTest {
        viewModel.setPointsToWin(10)
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.addPoints(amount = 5)
        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.checkWinner()).isNull()

        viewModel.endTurn()
        testDispatcher.scheduler.advanceUntilIdle()

        val playerToWin = viewModel.currentPlayer.value
        viewModel.addPoints(amount = 10)
        assertThat(viewModel.checkWinner()).isEqualTo(playerToWin)
        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(viewModel.getCurrentGame().status).isEqualTo("ENDED")
    }

    @Test
    fun `player card selection`() = runTest {
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()
        val card = Card("Haaveet ja unelmat","Milloin itkit viimeksi?",1)
        viewModel.updateSelectedCard(card)

        testDispatcher.scheduler.advanceUntilIdle()
        val testCard = viewModel.getSelectedCard()

        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(testCard).isEqualTo(card)
    }

    @Test
    fun `Draw random player`() = runTest {
        viewModel.startGame()
        testDispatcher.scheduler.advanceUntilIdle()

        val currentPlayer = viewModel.currentPlayer.value
        var assistant = viewModel.drawAssistingPlayer()

        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(currentPlayer).isNotEqualTo(assistant)
        assistant = viewModel.drawAssistingPlayer()

        testDispatcher.scheduler.advanceUntilIdle()
        assertThat(currentPlayer).isNotEqualTo(assistant)
    }

}