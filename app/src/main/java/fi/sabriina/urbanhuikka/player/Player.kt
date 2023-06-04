package fi.sabriina.urbanhuikka.player

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_table")
data class Player(@PrimaryKey @ColumnInfo(name = "player")
                  val name: String,
                  var currentPoints: Int = 0


                  ){

}
