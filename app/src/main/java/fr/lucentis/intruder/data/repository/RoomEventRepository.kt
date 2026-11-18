
package fr.lucentis.intruder.data.repository

import android.database.sqlite.SQLiteException
import fr.lucentis.intruder.data.database.EventDao
import fr.lucentis.intruder.domain.event.DeleteResult
import fr.lucentis.intruder.domain.event.EventPersistenceException
import fr.lucentis.intruder.domain.event.EventRepository
import fr.lucentis.intruder.domain.event.PhotoStorage
import fr.lucentis.intruder.domain.model.Event
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomEventRepository(
    private val eventDao: EventDao,
    private val photoStorage: PhotoStorage
) : EventRepository {

    override suspend fun insert(event: Event): Long =
        persistence { eventDao.insert(event.toEntity()) }

    override suspend fun getById(id: Long): Event? =
        persistence { eventDao.getById(id)?.toDomain() }

    override fun observeCount(): Flow<Int> = eventDao.observeCount()

    override fun observeLatest(): Flow<Event?> = eventDao.observeLatest().map { it?.toDomain() }

    override suspend fun deleteEvents(ids: List<Long>): DeleteResult {
        var deletedEvents = 0
        var photoDeletionFailures = 0

        // SQLite limits the number of variables in one statement, so large selections
        // are processed in chunks.
        ids.distinct().chunked(MAX_IDS_PER_QUERY).forEach { chunk ->
            val entities = persistence { eventDao.getByIds(chunk) }
            deletedEvents += persistence { eventDao.deleteByIds(chunk) }

            // The files and the database are two separate systems: no transaction covers
            // both. Rows are deleted first and a file failure never undoes that.
            entities.forEach { entity ->
                val path = entity.photoPath

                if (path != null) {
                    // A file deletion failure must not prevent other photos from being deleted.
                    val deleted = try {
                        photoStorage.delete(path)
                    } catch (exception: Exception) {
                        false
                    }

                    if (!deleted) {
                        photoDeletionFailures++
                    }
                }
            }
        }

        return DeleteResult(deletedEvents, photoDeletionFailures)
    }

    // Converts SQLite failures (disk full, corrupted file, ...) into the domain's own
    // exception type.
    private suspend fun <T> persistence(block: suspend () -> T): T =
        try {
            block()
        } catch (exception: SQLiteException) {
            throw EventPersistenceException(exception)
        }

    private companion object {
        const val MAX_IDS_PER_QUERY = 500
    }
}
