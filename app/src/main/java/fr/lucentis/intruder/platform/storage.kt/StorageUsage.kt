package fr.lucentis.intruder.platform.storage

import android.content.Context
import android.os.StatFs
import java.io.File

data class StorageUsage(
    val usedBytes: Long,
    val deviceTotalBytes: Long
)

// Blocking: walks the app's private files, so call it from a background dispatcher.
fun readStorageUsage(context: Context): StorageUsage {
    val appContext = context.applicationContext

    // filesDir holds the photos and the DataStore files. The Room database lives in a
    // sibling "databases" directory.
    val databasesDirectory = appContext.getDatabasePath("intruder").parentFile

    val usedBytes = directorySize(appContext.filesDir) +
        (databasesDirectory?.let { directorySize(it) } ?: 0L)

    return StorageUsage(
        usedBytes = usedBytes,
        deviceTotalBytes = StatFs(appContext.filesDir.path).totalBytes
    )
}

private fun directorySize(directory: File): Long =
    directory.walkTopDown().filter { it.isFile }.sumOf { it.length() }