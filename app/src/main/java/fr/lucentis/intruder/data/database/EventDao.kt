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
    
    // Observes the most recently recorded event.
    @Query("SELECT * FROM events ORDER BY timestamp DESC, id DESC LIMIT 1")
    fun observeLatest(): Flow<EventEntity?>
    
    // Observes all events, newest first. Room emits again whenever the table changes.
    @Query("SELECT * FROM events ORDER BY timestamp DESC, id DESC")
    fun observeAll(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getById(id: Long): EventEntity?

    @Query("SELECT * FROM events WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<EventEntity>

    // Returns the number of deleted rows.
    @Query("DELETE FROM events WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>): Int
}