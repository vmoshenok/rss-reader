package com.example.freshrssreader.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
        val KEY_SERVER_URL = stringPreferencesKey("server_url")
        val KEY_USERNAME = stringPreferencesKey("username")
    }

    suspend fun getAuthToken(): String? {
        return dataStore.data.map { it[KEY_AUTH_TOKEN] }.first()
    }

    suspend fun getServerUrl(): String? {
        return dataStore.data.map { it[KEY_SERVER_URL] }.first()
    }

    suspend fun saveLoginData(serverUrl: String, username: String, authToken: String) {
        dataStore.edit { prefs ->
            prefs[KEY_SERVER_URL] = serverUrl
            prefs[KEY_USERNAME] = username
            prefs[KEY_AUTH_TOKEN] = authToken
        }
    }

    suspend fun isLoggedIn(): Boolean {
        return getAuthToken() != null && getServerUrl() != null
    }

    suspend fun logout() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_AUTH_TOKEN)
            prefs.remove(KEY_USERNAME)
        }
    }
}
