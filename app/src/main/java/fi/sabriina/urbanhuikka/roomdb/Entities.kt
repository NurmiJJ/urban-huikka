package fi.sabriina.urbanhuikka.roomdb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_table")
data class Player(@PrimaryKey @ColumnInfo(name = "player")
                  val name: String,
                  var currentPoints: Int = 0
)

@Entity(tableName = "game_state")
data class GameState (
    @PrimaryKey (autoGenerate = true) val id: Int,
    @ColumnInfo(name = "status") var status: String,
    @ColumnInfo(name = "timestamp") var timestamp: Long
)
