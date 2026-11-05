package com.parkpulse.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "parkpulse_prefs")

/** Wraps Jetpack DataStore Preferences. Stores the login session and the remembered email. */
@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val REMEMBERED_EMAIL = stringPreferencesKey("remembered_email")
    }

    private val prefs = context.dataStore.data.catch { e ->
        if (e is IOException) emit(emptyPreferences()) else throw e
    }

    val isLoggedIn: Flow<Boolean> = prefs.map { it[Keys.IS_LOGGED_IN] ?: false }
    val rememberedEmail: Flow<String> = prefs.map { it[Keys.REMEMBERED_EMAIL] ?: "" }

    suspend fun saveSession(email: String, rememberMe: Boolean) {
        context.dataStore.edit { p ->
            p[Keys.IS_LOGGED_IN] = true
            p[Keys.USER_EMAIL] = email
            if (rememberMe) p[Keys.REMEMBERED_EMAIL] = email else p.remove(Keys.REMEMBERED_EMAIL)
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { p ->
            p[Keys.IS_LOGGED_IN] = false
            p.remove(Keys.USER_EMAIL)
        }
    }
}
