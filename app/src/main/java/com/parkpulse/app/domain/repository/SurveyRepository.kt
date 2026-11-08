package com.parkpulse.app.domain.repository

import com.parkpulse.app.domain.model.Survey
import com.parkpulse.app.domain.model.SurveyPhoto

interface SurveyRepository {
    /**
     * Saves the survey and its photo records in ONE transaction, all locally.
     * Safe to call twice with the same ids (the second call is ignored).
     */
    suspend fun saveSurvey(survey: Survey, photos: List<SurveyPhoto>)
}
