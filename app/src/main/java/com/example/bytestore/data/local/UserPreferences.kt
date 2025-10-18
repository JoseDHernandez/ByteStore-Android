package com.example.bytestore.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


val Context.dataStore by preferencesDataStore(name = "user_prefs")
class UserPreferences(private val context: Context) {
    companion object {
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_ROLE = stringPreferencesKey("user_role")
        private val USER_ADDRESS = stringPreferencesKey("user_address")
        private val USER_TOKEN = stringPreferencesKey("user_token")
    }

    suspend fun saveUserData(
        id: String,
        name: String,
        email: String,
        address: String,
        role: String,
        token: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[USER_ID] = id
            prefs[USER_NAME] = name
            prefs[USER_EMAIL] = email
            prefs[USER_ROLE] = role
            prefs[USER_ADDRESS] = address
            prefs[USER_TOKEN] = token
        }
    }

    suspend fun getToken(): String? {
        return context.dataStore.data.map { it[USER_TOKEN] }.first()
    }

    suspend fun getUser(): Map<String, String?> {
        return context.dataStore.data.map {
            mapOf(
                "id" to it[USER_ID],
                "name" to it[USER_NAME],
                "email" to it[USER_EMAIL],
                "role" to it[USER_ROLE],
                "address" to it[USER_ADDRESS],
                "token" to it[USER_TOKEN]
            )
        }.first()
    }

    suspend fun clearData() {
        context.dataStore.edit { it.clear() }
    }
}