package fr.lucentis.intruder.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// DAO (Data Access Object): you declare the operations, Room generates the implementation
// at build time and checks the SQL then: a typo in a query fails the build, not the app.
@Dao
interface EventDao {

    // suspend: Room runs the insert on a background thread and resumes the caller after.
    // Database access on the main thread is forbidden because it would freeze the UI.
    @Insert
    suspend fun insert(event: EventEntity): Long

    // A query returning Flow is observable: it emits the current value, then emits again
    // each time the events table changes. The UI updates itself without manual refresh.
    @Query("SELECT COUNT(*) FROM events")
    fun observeCount(): Flow<Int>
}