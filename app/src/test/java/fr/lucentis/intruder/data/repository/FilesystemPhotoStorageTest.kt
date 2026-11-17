package fr.lucentis.intruder.data.repository

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class FilesystemPhotoStorageTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var directory: File
    private lateinit var storage: FilesystemPhotoStorage

    @Before
    fun setUp() {
        directory = temporaryFolder.newFolder("captures")
        storage = FilesystemPhotoStorage(directory)
    }

    @Test
    fun deletesAnExistingPhoto() {
        val photo = File(directory, "a.jpg").apply { writeText("x") }

        assertTrue(storage.delete(photo.absolutePath))
        assertFalse(photo.exists())
    }

    @Test
    fun treatsAnAbsentPhotoAsDeleted() {
        assertTrue(storage.delete(File(directory, "missing.jpg").absolutePath))
    }

    @Test
    fun refusesFilesOutsideTheDirectory() {
        val outside = temporaryFolder.newFile("outside.jpg")

        assertFalse(storage.delete(outside.absolutePath))
        assertTrue(outside.exists())
    }

    @Test
    fun refusesPathsThatEscapeTheDirectoryWithDotDot() {
        val outside = temporaryFolder.newFile("escaped.jpg")
        val sneakyPath = File(directory, "../escaped.jpg").path

        assertFalse(storage.delete(sneakyPath))
        assertTrue(outside.exists())
    }
}