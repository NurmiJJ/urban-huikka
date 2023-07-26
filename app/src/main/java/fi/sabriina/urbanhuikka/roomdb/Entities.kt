package fi.sabriina.urbanhuikka.roomdb

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_table")
data class Player(
                  @PrimaryKey (autoGenerate = true) @ColumnInfo(name = "id") val id: Int,
                  @ColumnInfo(name = "player") val name: String,
                  @ColumnInfo(name = "points") var currentPoints: Int = 0
)

@Entity(tableName = "game_state")
data class GameState (
    @PrimaryKey (autoGenerate = true) @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "status") var status: String,
    @ColumnInfo(name = "currentPlayerIndex") var currentPlayerIndex: Int = 0
)

@Entity(tableName = "scoreboard")
data class ScoreboardEntry (
    @PrimaryKey (autoGenerate = true) @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "playerId") var playerId: Int,
    @ColumnInfo(name = "score") var score: Int = 0
)

data class PlayerAndScore (
    @Embedded val player: Player,
    @ColumnInfo(name = "score") val score: Int
)