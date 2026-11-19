package fr.lucentis.intruder.domain.gallery

import kotlinx.coroutines.flow.Flow

enum class GalleryDisplayMode {
    GRID,
    LIST
}

interface GalleryRepository {

    val displayMode: Flow<GalleryDisplayMode>

    suspend fun setDisplayMode(mode: GalleryDisplayMode)
}
