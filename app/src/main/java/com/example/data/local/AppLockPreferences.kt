package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

val Context.appLockDataStore: DataStore<Preferences> by preferencesDataStore(name = "myevo_app_lock_prefs")

/**
 * AppLockPreferences manages the encrypted PIN and application security lock state
 * using Jetpack DataStore Preferences.
 */
class AppLockPreferences(private val context: Context) {

    companion object {
        private val KEY_IS_APP_LOCK_ENABLED = booleanPreferencesKey("is_app_lock_enabled")
        private val KEY_ENCRYPTED_PIN_HASH = stringPreferencesKey("encrypted_pin_hash")
        private const val PIN_SALT = "MyEvo_Salt_Secure_Pin_2026"

        /**
         * Securely hashes the PIN using SHA-256 with application salt
         * so the raw PIN is never stored in plaintext.
         */
        fun hashPin(pin: String): String {
            val salted = "$PIN_SALT:$pin"
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(salted.toByteArray(Charsets.UTF_8))
            return hashBytes.joinToString("") { "%02x".format(it) }
        }
    }

    val isAppLockEnabled: Flow<Boolean> = context.appLockDataStore.data.map { preferences ->
        preferences[KEY_IS_APP_LOCK_ENABLED] ?: false
    }

    val hasConfiguredPin: Flow<Boolean> = context.appLockDataStore.data.map { preferences ->
        val hash = preferences[KEY_ENCRYPTED_PIN_HASH]
        !hash.isNullOrBlank()
    }

    suspend fun setPin(pin: String) {
        val encryptedHash = hashPin(pin)
        context.appLockDataStore.edit { preferences ->
            preferences[KEY_ENCRYPTED_PIN_HASH] = encryptedHash
            preferences[KEY_IS_APP_LOCK_ENABLED] = true
        }
    }

    suspend fun verifyPin(enteredPin: String): Boolean {
        val storedHash = context.appLockDataStore.data.first()[KEY_ENCRYPTED_PIN_HASH] ?: return false
        val enteredHash = hashPin(enteredPin)
        return storedHash == enteredHash
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.appLockDataStore.edit { preferences ->
            preferences[KEY_IS_APP_LOCK_ENABLED] = enabled
        }
    }

    suspend fun disableAppLock() {
        context.appLockDataStore.edit { preferences ->
            preferences[KEY_IS_APP_LOCK_ENABLED] = false
            preferences.remove(KEY_ENCRYPTED_PIN_HASH)
        }
    }
}
