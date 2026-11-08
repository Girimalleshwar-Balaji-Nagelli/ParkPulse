package com.parkpulse.app.data.repository

import com.parkpulse.app.data.local.dao.SurveyDao
import com.parkpulse.app.data.local.entity.toEntity
import com.parkpulse.app.domain.model.Survey
import com.parkpulse.app.domain.model.SurveyPhoto
import com.parkpulse.app.domain.repository.SurveyRepository
import javax.inject.Inject

class SurveyRepositoryImpl @Inject constructor(
    private val dao: SurveyDao,
) : SurveyRepository {
    override suspend fun saveSurvey(survey: Survey, photos: List<SurveyPhoto>) {
        dao.insertSurveyWithPhotos(survey.toEntity(), photos.map { it.toEntity() })
    }
}
