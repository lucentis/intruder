package fr.lucentis.intruder.domain.event

interface PhotoStorage {

    // True when the file no longer exists after the call: deleted now, or already absent.
    fun delete(path: String): Boolean
}