package fi.sabriina.urbanhuikka.roomdb

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import fi.sabriina.urbanhuikka.card.Card

@Entity(tableName = "player_table")
data class Player(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "pictureResId") var pictureResId: Int
)

@Entity(tableName = "game_state")
data class GameState(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "status") var status: String,
    @ColumnInfo(name = "currentPlayerIndex") var currentPlayerIndex: Int = 0,
    @ColumnInfo(name = "assistingPlayerIndex") var assistingPlayerIndex: Int = 0,
    @ColumnInfo(name = "selectedCard") var selectedCard: Card? = null,
    @ColumnInfo(name = "pointsToWin") var pointsToWin: Int = 30
)

@Entity(tableName = "card_category")
data class CardCategory(
    @PrimaryKey @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "enabled") var enabled: Boolean
)

@Entity(tableName = "scoreboard")
data class ScoreboardEntry(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "playerId") var playerId: Int,
    @ColumnInfo(name = "score") var score: Int = 0
)

data class PlayerAndScore(
    @Embedded val player: Player,
    @ColumnInfo(name = "score") val score: Int
)