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
import java.security.SecureRandom

val Context.pinSecurityDataStore: DataStore<Preferences> by preferencesDataStore(name = "myevo_pin_security_prefs")

/**
 * DataStore utility for storing salted SHA-256 hashes of user-defined PINs
 * (for sensitive modules such as the Daily Journal) and the private Admin password.
 * Raw PINs and passwords are never persisted or logged anywhere.
 */
class PinSecurityDataStore(private val context: Context) {

    companion object {
        private val KEY_MODULE_PIN_HASH = stringPreferencesKey("module_pin_hash")
        private val KEY_MODULE_PIN_SALT = stringPreferencesKey("module_pin_salt")
        private val KEY_JOURNAL_GATE_ENABLED = booleanPreferencesKey("journal_gate_enabled")

        private val KEY_ADMIN_PASSWORD_HASH = stringPreferencesKey("admin_password_hash")
        private val KEY_ADMIN_PASSWORD_SALT = stringPreferencesKey("admin_password_salt")

        private const val DOMAIN_PREFIX = "MyEvo_Sec_v2"

        fun generateSaltHex(byteCount: Int = 16): String {
            val bytes = ByteArray(byteCount)
            SecureRandom().nextBytes(bytes)
            return bytes.joinToString("") { "%02x".format(it) }
        }

        fun hashSecret(secret: String, saltHex: String): String {
            val input = "$DOMAIN_PREFIX:$saltHex:$secret"
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
            return hashBytes.joinToString("") { "%02x".format(it) }
        }

        fun constantTimeEquals(a: String, b: String): Boolean {
            if (a.length != b.length) return false
            var diff = 0
            for (i in a.indices) {
                diff = diff or (a[i].code xor b[i].code)
            }
            return diff == 0
        }
    }

    val hasConfiguredModulePin: Flow<Boolean> = context.pinSecurityDataStore.data.map { prefs ->
        !prefs[KEY_MODULE_PIN_HASH].isNullOrBlank() && !prefs[KEY_MODULE_PIN_SALT].isNullOrBlank()
    }

    val isJournalGateEnabled: Flow<Boolean> = context.pinSecurityDataStore.data.map { prefs ->
        prefs[KEY_JOURNAL_GATE_ENABLED] ?: true
    }

    val hasAdminPassword: Flow<Boolean> = context.pinSecurityDataStore.data.map { prefs ->
        !prefs[KEY_ADMIN_PASSWORD_HASH].isNullOrBlank() && !prefs[KEY_ADMIN_PASSWORD_SALT].isNullOrBlank()
    }

    suspend fun saveUserDefinedPin(pin: String) {
        require(pin.length in 4..8 && pin.all { it.isDigit() }) {
            "PIN harus terdiri dari 4 hingga 8 digit angka."
        }
        val salt = generateSaltHex()
        val hash = hashSecret(pin, salt)
        context.pinSecurityDataStore.edit { prefs ->
            prefs[KEY_MODULE_PIN_SALT] = salt
            prefs[KEY_MODULE_PIN_HASH] = hash
            prefs[KEY_JOURNAL_GATE_ENABLED] = true
        }
    }

    suspend fun verifyUserDefinedPin(enteredPin: String): Boolean {
        val prefs = context.pinSecurityDataStore.data.first()
        val storedHash = prefs[KEY_MODULE_PIN_HASH] ?: return false
        val storedSalt = prefs[KEY_MODULE_PIN_SALT] ?: return false
        val candidateHash = hashSecret(enteredPin, storedSalt)
        return constantTimeEquals(storedHash, candidateHash)
    }

    suspend fun setJournalGateEnabled(enabled: Boolean) {
        context.pinSecurityDataStore.edit { prefs ->
            prefs[KEY_JOURNAL_GATE_ENABLED] = enabled
        }
    }

    suspend fun clearUserDefinedPin() {
        context.pinSecurityDataStore.edit { prefs ->
            prefs.remove(KEY_MODULE_PIN_HASH)
            prefs.remove(KEY_MODULE_PIN_SALT)
            prefs[KEY_JOURNAL_GATE_ENABLED] = false
        }
    }

    suspend fun saveAdminPassword(password: String) {
        require(password.length >= 6) {
            "Kata sandi admin minimal 6 karakter."
        }
        val salt = generateSaltHex()
        val hash = hashSecret(password, salt)
        context.pinSecurityDataStore.edit { prefs ->
            prefs[KEY_ADMIN_PASSWORD_SALT] = salt
            prefs[KEY_ADMIN_PASSWORD_HASH] = hash
        }
    }

    suspend fun verifyAdminPassword(enteredPassword: String): Boolean {
        val prefs = context.pinSecurityDataStore.data.first()
        val storedHash = prefs[KEY_ADMIN_PASSWORD_HASH] ?: return false
        val storedSalt = prefs[KEY_ADMIN_PASSWORD_SALT] ?: return false
        val candidateHash = hashSecret(enteredPassword, storedSalt)
        return constantTimeEquals(storedHash, candidateHash)
    }

    suspend fun changeAdminPassword(currentPassword: String, newPassword: String): Boolean {
        if (!verifyAdminPassword(currentPassword)) return false
        saveAdminPassword(newPassword)
        return true
    }
}
