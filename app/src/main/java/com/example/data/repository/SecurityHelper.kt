package com.example.data.repository

import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object SecurityHelper {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private val DEFAULT_IV = byteArrayOf(12, 34, 56, 78, 90, 21, 43, 65, 87, 98, 10, 32, 54, 76, 98, 23)

    private fun deriveKey(pin: String): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    fun encrypt(plainText: String, pin: String): String {
        return try {
            val key = deriveKey(pin)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(DEFAULT_IV))
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            Base64.getEncoder().encodeToString(encryptedBytes)
        } catch (e: Exception) {
            Base64.getEncoder().encodeToString(plainText.toByteArray(Charsets.UTF_8))
        }
    }

    fun decrypt(cipherText: String, pin: String): String {
        return try {
            val key = deriveKey(pin)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(DEFAULT_IV))
            val decodedBytes = Base64.getDecoder().decode(cipherText)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            throw IllegalArgumentException("PIN atau kata sandi tidak cocok!")
        }
    }
}
