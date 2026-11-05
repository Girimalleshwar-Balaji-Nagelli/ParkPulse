package com.parkpulse.app.data.repository

import com.parkpulse.app.data.local.DataStoreManager
import com.parkpulse.app.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** Mock authentication against hard-coded demo credentials. No network involved. */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val dataStore: DataStoreManager
) : AuthRepository {

    override val isLoggedIn: Flow<Boolean> = dataStore.isLoggedIn
    override val rememberedEmail: Flow<String> = dataStore.rememberedEmail

    override suspend fun login(email: String, password: String, rememberMe: Boolean): Boolean {
        delay(MOCK_LATENCY_MS) // simulate a network call
        val valid = email.trim().equals(DEMO_EMAIL, ignoreCase = true) && password == DEMO_PASSWORD
        if (valid) dataStore.saveSession(email.trim().lowercase(), rememberMe)
        return valid
    }

    override suspend fun logout() = dataStore.clearSession()

    private companion object {
        const val DEMO_EMAIL = "demo@parkpulse.com"
        const val DEMO_PASSWORD = "123456"
        const val MOCK_LATENCY_MS = 1200L
    }
}
