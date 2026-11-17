package fr.lucentis.intruder.data.repository

import fr.lucentis.intruder.data.database.EventDao
import fr.lucentis.intruder.data.database.EventEntity
import fr.lucentis.intruder.domain.event.DeleteResult
import fr.lucentis.intruder.domain.model.CaptureFailureReason
import fr.lucentis.intruder.domain.model.CaptureStatus
import fr.lucentis.intruder.domain.model.Event
import fr.lucentis.intruder.domain.model.EventType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

// Runs on the JVM: the real Room database needs a device, so the DAO is replaced by an
// in-memory fake. What is tested here is the repository's own logic: mapping, chunking
// and the coordination between database rows and photo files (real files in a temp folder).
class RoomEventRepositoryTest {

    // JUnit rule: creates a temporary folder before each test and deletes it afterwards.
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var photosDirectory: File
    private val dao = FakeEventDao()
    private lateinit var repository: RoomEventRepository

    @Before
    fun setUp() {
        photosDirectory = temporaryFolder.newFolder("captures")
        repository = RoomEventRepository(dao, FilesystemPhotoStorage(photosDirectory))
    }

    private fun event(photoPath: String? = null) = Event(
        type = EventType.UNLOCK_FAILED,
        timestamp = 5L,
        photoPath = photoPath,
        captureStatus = if (photoPath == null) CaptureStatus.FAILED else CaptureStatus.SUCCESS,
        failureReason = if (photoPath == null) CaptureFailureReason.CAMERA_BUSY else null
    )

    @Test
    fun insertStoresTheEnumsByNameAndReturnsTheGeneratedId() = runBlocking<Unit> {
        val id = repository.insert(event())

        val stored = dao.rows.single()
        assertEquals(id, stored.id)
        assertEquals("UNLOCK_FAILED", stored.type)
        assertEquals("FAILED", stored.captureStatus)
        assertEquals("CAMERA_BUSY", stored.failureReason)
    }

    @Test
    fun getByIdRestoresTheDomainValues() = runBlocking<Unit> {
        val id = repository.insert(event())

        val loaded = repository.getById(id)

        assertEquals(event().copy(id = id), loaded)
    }

    @Test
    fun anUnknownFailureReasonIsReadAsNull() = runBlocking<Unit> {
        dao.rows += EventEntity(
            id = 7,
            type = "UNLOCK_FAILED",
            timestamp = 1L,
            photoPath = null,
            captureStatus = "FAILED",
            failureReason = "REASON_REMOVED_IN_A_LATER_VERSION"
        )

        val loaded = repository.getById(7)

        assertNotNull(loaded)
        assertNull(loaded!!.failureReason)
    }

    @Test
    fun observeLatestEmitsTheMostRecentEventAsADomainEvent() = runBlocking<Unit> {
        repository.insert(event().copy(timestamp = 10L))
        val newestId = repository.insert(event().copy(timestamp = 30L))
        repository.insert(event().copy(timestamp = 20L))

        val latest = repository.observeLatest().first()

        assertEquals(event().copy(id = newestId, timestamp = 30L), latest)
    }

    @Test
    fun observeLatestEmitsNullWhenThereIsNoEvent() = runBlocking<Unit> {
        assertNull(repository.observeLatest().first())
    }

    @Test
    fun deleteRemovesTheRowsAndThePhotoFiles() = runBlocking<Unit> {
        val photo = File(photosDirectory, "a.jpg").apply { writeText("x") }
        val id = repository.insert(event(photoPath = photo.absolutePath))

        val result = repository.deleteEvents(listOf(id))

        assertEquals(DeleteResult(deletedEvents = 1, photoDeletionFailures = 0), result)
        assertTrue(dao.rows.isEmpty())
        assertFalse(photo.exists())
    }

    @Test
    fun deleteStillSucceedsWhenThePhotoFileIsAlreadyMissing() = runBlocking<Unit> {
        val missing = File(photosDirectory, "missing.jpg")
        val id = repository.insert(event(photoPath = missing.absolutePath))

        val result = repository.deleteEvents(listOf(id))

        assertEquals(DeleteResult(deletedEvents = 1, photoDeletionFailures = 0), result)
        assertTrue(dao.rows.isEmpty())
    }

    @Test
    fun deleteRemovesTheRowButReportsAFileOutsideThePhotoDirectory() = runBlocking<Unit> {
        val outside = temporaryFolder.newFile("outside.jpg")
        val id = repository.insert(event(photoPath = outside.absolutePath))

        val result = repository.deleteEvents(listOf(id))

        assertEquals(DeleteResult(deletedEvents = 1, photoDeletionFailures = 1), result)
        assertTrue(dao.rows.isEmpty())
        assertTrue(outside.exists())
    }

    @Test
    fun deleteHandlesMoreIdsThanOneQueryCanTake() = runBlocking<Unit> {
        repeat(1_200) { repository.insert(event()) }
        val ids = dao.rows.map { it.id }

        val result = repository.deleteEvents(ids)

        assertEquals(1_200, result.deletedEvents)
        assertTrue(dao.rows.isEmpty())
    }
}

private class FakeEventDao : EventDao {
    val rows = mutableListOf<EventEntity>()
    private var nextId = 1L

    override suspend fun insert(event: EventEntity): Long {
        val id = if (event.id == 0L) nextId++ else event.id
        rows += event.copy(id = id)
        return id
    }

    override fun observeCount(): Flow<Int> = flowOf(rows.size)

    // Mirrors the real query: ORDER BY timestamp DESC, id DESC LIMIT 1.
    override fun observeLatest(): Flow<EventEntity?> =
        flowOf(rows.maxWithOrNull(compareBy<EventEntity>({ it.timestamp }, { it.id })))

    override suspend fun getById(id: Long): EventEntity? = rows.firstOrNull { it.id == id }

    override suspend fun getByIds(ids: List<Long>): List<EventEntity> {
        val wanted = ids.toSet()
        return rows.filter { it.id in wanted }
    }

    override suspend fun deleteByIds(ids: List<Long>): Int {
        val wanted = ids.toSet()
        val before = rows.size
        rows.removeAll { it.id in wanted }
        return before - rows.size
    }
}