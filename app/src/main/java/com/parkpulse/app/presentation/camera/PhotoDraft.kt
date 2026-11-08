package com.parkpulse.app.presentation.camera

/** A photo that is saved on disk (under its survey's folder) but not yet attached to a saved survey. */
data class PhotoDraft(
    val id: String,
    val surveyId: String,
    /** Relative path inside the photo directory: "{surveyId}/{photoId}.jpg". */
    val fileName: String,
    val takenAtMillis: Long,
    val latitude: Double,
    val longitude: Double,
    val accuracyM: Float,
) {
    /** Compact string so it can travel through SavedStateHandle / navigation results safely. */
    fun encode(): String =
        listOf(id, surveyId, fileName, takenAtMillis, latitude, longitude, accuracyM).joinToString("|")

    companion object {
        fun decode(value: String): PhotoDraft? {
            val p = value.split("|")
            if (p.size != 7) return null
            return PhotoDraft(
                id = p[0],
                surveyId = p[1],
                fileName = p[2],
                takenAtMillis = p[3].toLongOrNull() ?: return null,
                latitude = p[4].toDoubleOrNull() ?: return null,
                longitude = p[5].toDoubleOrNull() ?: return null,
                accuracyM = p[6].toFloatOrNull() ?: return null,
            )
        }
    }
}
