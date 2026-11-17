package fr.lucentis.intruder.data.repository

import fr.lucentis.intruder.domain.event.PhotoStorage
import java.io.File
import java.io.IOException

// Photos live in the app's private storage (context.filesDir): other apps cannot read it
// and Android removes it when the app is uninstalled.
class FilesystemPhotoStorage(private val directory: File) : PhotoStorage {

    override fun delete(path: String): Boolean {
        val file = File(path)

        // Safety net: a corrupted or tampered path must never delete anything outside the
        // photos directory.
        if (!isInsideDirectory(file)) return false

        return !file.exists() || file.delete()
    }

    private fun isInsideDirectory(file: File): Boolean =
        try {
            // canonicalFile resolves "..", "." and symbolic links before comparing.
            file.canonicalFile.startsWith(directory.canonicalFile)
        } catch (exception: IOException) {
            false
        }
}