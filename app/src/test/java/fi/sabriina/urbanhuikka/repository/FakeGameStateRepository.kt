package fi.sabriina.urbanhuikka.repository

import fi.sabriina.urbanhuikka.card.Card
import fi.sabriina.urbanhuikka.roomdb.GameState
import fi.sabriina.urbanhuikka.roomdb.Player
import fi.sabriina.urbanhuikka.roomdb.PlayerAndScore
import fi.sabriina.urbanhuikka.roomdb.ScoreboardEntry

class FakeGameStateRepository: GameStateRepositoryInterface {

    private var repoGameState : GameState? = null
    private var scoreboard = mutableListOf<ScoreboardEntry>()
    private var playerList = mutableListOf(Player(1,"Eetu",123), Player(2,"Matias", 1234), Player(3,"Krista", 123), Player(4, "Mikko", 123))
    private var pointsToWin = 30

    override fun updateDatabase(): Pair<MutableList<Card>, MutableList<Card>> {
        val truthCards = mutableListOf(Card("Haaveet ja unelmat","Milloin itkit viimeksi?",1), Card("Seksi","Miten vanhemmat otti kukkaset ja mehiläiset puheeksi?",1), Card("Kaverit", "Kuka on paras ystäväsi?", 1), Card("Opinnot", "Mikä on huonoin kouluarvosanasi?", 3))
        val dareCards = mutableListOf(Card("Ruoka ja juoma","Ota huikka!",1), Card("Onks pakko?","Ole lankku-asennossa yksi minuutti!",2,60))
        return Pair(truthCards, dareCards)
    }

    override suspend fun insertGameState(gameState: GameState) {
        repoGameState = gameState
    }

    override suspend fun insertPlayerToScoreboard(scoreboardEntry: ScoreboardEntry) {
        scoreboard.add(scoreboardEntry)
    }

    override suspend fun updateGameState(status: String) {
        repoGameState?.status = status
    }

    override suspend fun updateCurrentPlayerIndex(index: Int) {
        repoGameState?.currentPlayerIndex = index
    }

    override suspend fun getCurrentGame() : GameState {
        return repoGameState!!
    }

    override suspend fun getGameCount() : Int {
        if (repoGameState != null) {
            return 1
        }
        return 0
    }

    override suspend fun getPlayers(): List<Player> {
        return playerList
    }

    override suspend fun getPlayerScore(playerId: Int) : Int {
        for (player in scoreboard) {
            if (player.playerId == playerId) {
                return player.score
            }
        }
        return -1
    }

    override suspend fun updatePlayerScore(playerId: Int, score: Int) {
        for (player in scoreboard) {
            if (player.playerId == playerId) {
                player.score = score
            }
        }
    }

    override suspend fun getAllScores() : List<PlayerAndScore> {
        val leaderboardList = mutableListOf<PlayerAndScore>()
        for (score in scoreboard) {
            for (player in playerList) {
                if (player.id == score.playerId) {
                    leaderboardList.add(PlayerAndScore(player, score.score))
                }
            }
        }
        return leaderboardList
    }

    override suspend fun getCurrentPlayerIndex(): Int {
        return repoGameState!!.currentPlayerIndex
    }

    override suspend fun deleteAllGames() {
        repoGameState = null
    }

    override suspend fun deleteAllPlayersFromScoreboard() {
        scoreboard.clear()
    }

    override suspend fun setPointsToWin(points: Int) {
        pointsToWin = points
    }

    override suspend fun getPointsToWin(): Int {
        return pointsToWin
    }
}