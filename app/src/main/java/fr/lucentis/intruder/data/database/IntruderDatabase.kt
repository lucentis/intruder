package fr.lucentis.intruder.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Room generates the concrete subclass (IntruderDatabase_Impl). This is a heavy object:
// keep a single instance per process, which is what AppContainer does.
//
// version: must be increased with a Migration when the schema changes, otherwise Room
// refuses to open an existing database. During development, uninstalling the app is
// enough. exportSchema = false: no schema JSON files are written for now.
@Database(
    entities = [EventEntity::class],
    version = 1,
    exportSchema = false
)
abstract class IntruderDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao

    companion object {
        private const val DATABASE_NAME = "intruder.db"

        // The file lives in the app's private storage, unreadable by other apps.
        // build() does not open it: the file is created on the first query.
        fun create(context: Context): IntruderDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                IntruderDatabase::class.java,
                DATABASE_NAME
            ).build()
    }
}