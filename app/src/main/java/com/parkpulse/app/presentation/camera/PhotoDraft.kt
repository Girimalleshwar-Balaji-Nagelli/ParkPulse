package com.parkpulse.app.presentation.camera

/** A photo that is saved on disk but not yet attached to a saved survey. */
data class PhotoDraft(
    val id: String,
    val fileName: String,
    val takenAtMillis: Long,
    val latitude: Double,
    val longitude: Double,
    val accuracyM: Float,
) {
    /** Compact string so it can travel through SavedStateHandle / navigation results safely. */
    fun encode(): String = listOf(id, fileName, takenAtMillis, latitude, longitude, accuracyM).joinToString("|")

    companion object {
        fun decode(value: String): PhotoDraft? {
            val p = value.split("|")
            if (p.size != 6) return null
            return PhotoDraft(
                id = p[0],
                fileName = p[1],
                takenAtMillis = p[2].toLongOrNull() ?: return null,
                latitude = p[3].toDoubleOrNull() ?: return null,
                longitude = p[4].toDoubleOrNull() ?: return null,
                accuracyM = p[5].toFloatOrNull() ?: return null,
            )
        }
    }
}
