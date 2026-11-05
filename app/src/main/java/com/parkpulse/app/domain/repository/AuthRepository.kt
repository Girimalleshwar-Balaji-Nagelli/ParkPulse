package com.parkpulse.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val isLoggedIn: Flow<Boolean>
    val rememberedEmail: Flow<String>

    /** Returns true when credentials are valid and the session was saved. */
    suspend fun login(email: String, password: String, rememberMe: Boolean): Boolean

    suspend fun logout()
}
