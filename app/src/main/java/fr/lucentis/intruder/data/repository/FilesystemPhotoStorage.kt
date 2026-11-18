
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

        return try {
            !file.exists() || file.delete()
        } catch (exception: SecurityException) {
            false
        }
    }

    private fun isInsideDirectory(file: File): Boolean =
        try {
            // Canonical paths resolve "..", "." and symbolic links before comparing.
            // Path.startsWith compares directory components, not just text prefixes.
            val canonicalDirectory = directory.canonicalFile
            val canonicalFile = file.canonicalFile

            canonicalFile != canonicalDirectory &&
                canonicalFile.toPath().startsWith(canonicalDirectory.toPath())

        } catch (exception: IOException) {
            false
        } catch (exception: SecurityException) {
            false
        }
}
