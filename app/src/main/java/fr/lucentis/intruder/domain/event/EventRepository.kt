package fr.lucentis.intruder.domain.event

import fr.lucentis.intruder.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {

    // Returns the generated id. Throws EventPersistenceException if nothing was stored.
    suspend fun insert(event: Event): Long

    suspend fun getById(id: Long): Event?

    fun observeCount(): Flow<Int>
    
    fun observeLatest(): Flow<Event?>

    // Deleting the database rows has priority: a photo file that cannot be removed never
    // prevents the deletion of its event (see DeleteResult).
    suspend fun deleteEvents(ids: List<Long>): DeleteResult
}

data class DeleteResult(
    val deletedEvents: Int,
    // Photos that still exist after the deletion, to be reported as a technical error.
    val photoDeletionFailures: Int
)

// Typed error raised by the data layer, so the domain does not depend on SQLite classes.
class EventPersistenceException(cause: Throwable) :
    Exception("The event could not be stored", cause)