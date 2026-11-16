package fr.lucentis.intruder.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Room entity: this class maps to one SQLite table. Room reads the annotations at build
// time (through KSP) to generate the SQL and the mapping code.
@Entity(
    tableName = "events",
    // Index on the timestamp: the gallery sorts events chronologically.
    indices = [Index(value = ["timestamp"])]
)
data class EventEntity(
    // autoGenerate: SQLite assigns the id on insert when the value is 0.
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    // Plain strings for now; they will be mapped to domain enums in the data layer.
    val type: String,
    val timestamp: Long,
    val photoPath: String?,
    val captureStatus: String,
    val failureReason: String?
)