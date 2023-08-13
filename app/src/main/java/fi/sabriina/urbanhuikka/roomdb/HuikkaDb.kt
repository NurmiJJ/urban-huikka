package fi.sabriina.urbanhuikka.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import fi.sabriina.urbanhuikka.roomdb.dao.GameStateDao
import fi.sabriina.urbanhuikka.roomdb.dao.PlayerDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Annotates class to be a Room Database with a table (entity) of the Player class
@Database(entities = [Player::class, GameState::class, CardCategory::class, ScoreboardEntry::class], version = 7, exportSchema = false)
@TypeConverters(Converters::class)
abstract class HuikkaDb : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun gameStateDao(): GameStateDao

    private class HuikkaDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    val playerDao = database.playerDao()

                    // Delete all content here.
                    playerDao.deleteAll()

                }
            }
        }
    }

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: HuikkaDb? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): HuikkaDb {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HuikkaDb::class.java,
                    "huikka_database"
                )
                    .addCallback(HuikkaDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}