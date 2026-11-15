package com.example.data.repository

import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object SecurityHelper {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private val DEFAULT_IV = byteArrayOf(12, 34, 56, 78, 90, 21, 43, 65, 87, 98, 10, 32, 54, 76, 98, 23)
    private const val BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

    private fun deriveKey(pin: String): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    internal fun encodeBase64(input: ByteArray): String {
        if (input.isEmpty()) return ""
        val sb = StringBuilder(((input.size + 2) / 3) * 4)
        var i = 0
        while (i < input.size) {
            val b0 = input[i].toInt() and 0xFF
            val b1 = if (i + 1 < input.size) input[i + 1].toInt() and 0xFF else 0
            val b2 = if (i + 2 < input.size) input[i + 2].toInt() and 0xFF else 0

            val triple = (b0 shl 16) or (b1 shl 8) or b2
            sb.append(BASE64_ALPHABET[(triple ushr 18) and 0x3F])
            sb.append(BASE64_ALPHABET[(triple ushr 12) and 0x3F])
            sb.append(if (i + 1 < input.size) BASE64_ALPHABET[(triple ushr 6) and 0x3F] else '=')
            sb.append(if (i + 2 < input.size) BASE64_ALPHABET[triple and 0x3F] else '=')
            i += 3
        }
        return sb.toString()
    }

    internal fun decodeBase64(encoded: String): ByteArray {
        val clean = encoded.filterNot { it.isWhitespace() }
        if (clean.isEmpty()) return ByteArray(0)
        require(clean.length % 4 == 0) { "Invalid Base64 input length" }

        val out = ByteArrayOutputStream((clean.length * 3) / 4)
        var i = 0
        while (i < clean.length) {
            val c0 = BASE64_ALPHABET.indexOf(clean[i])
            val c1 = BASE64_ALPHABET.indexOf(clean[i + 1])
            val c2 = if (clean[i + 2] == '=') -1 else BASE64_ALPHABET.indexOf(clean[i + 2])
            val c3 = if (clean[i + 3] == '=') -1 else BASE64_ALPHABET.indexOf(clean[i + 3])
            require(c0 >= 0 && c1 >= 0 && (clean[i + 2] == '=' || c2 >= 0) && (clean[i + 3] == '=' || c3 >= 0)) {
                "Invalid Base64 character"
            }

            out.write((c0 shl 2) or (c1 ushr 4))
            if (c2 >= 0) {
                out.write(((c1 and 0x0F) shl 4) or (c2 ushr 2))
            }
            if (c3 >= 0) {
                out.write(((c2 and 0x03) shl 6) or c3)
            }
            i += 4
        }
        return out.toByteArray()
    }

    fun encrypt(plainText: String, pin: String): String {
        return try {
            val key = deriveKey(pin)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(DEFAULT_IV))
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            encodeBase64(encryptedBytes)
        } catch (e: Exception) {
            encodeBase64(plainText.toByteArray(Charsets.UTF_8))
        }
    }

    fun decrypt(cipherText: String, pin: String): String {
        return try {
            val key = deriveKey(pin)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(DEFAULT_IV))
            val decodedBytes = decodeBase64(cipherText)
            val decryptedBytes = cipher.doFinal(decodedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            throw IllegalArgumentException("PIN atau kata sandi tidak cocok!")
        }
    }
}
