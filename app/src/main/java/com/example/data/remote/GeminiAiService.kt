package com.example.data.remote

import android.util.Log
import com.example.data.model.CrosswordPuzzle
import com.example.data.model.ExpenseCategory
import com.example.data.model.FinanceType
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout

object GeminiAiService {
    private const val TAG = "GeminiAiService"
    private const val MODEL_NAME = "gemini-3.8-flash"
    private const val REQUEST_TIMEOUT_MS = 60_000L

    private val crosswordModel by lazy {
        val clueSchema = Schema.obj(
            mapOf(
                "number" to Schema.integer("Clue number starting from 1"),
                "direction" to Schema.enumeration(
                    listOf("ACROSS", "DOWN"),
                    "Word orientation on the 7x7 crossword grid"
                ),
                "clue" to Schema.string("Challenging, clever, and engaging clue written in Indonesian"),
                "answer" to Schema.string("Single uppercase A-Z word, 3 to 7 letters long, no spaces or punctuation"),
                "startRow" to Schema.integer("0-indexed start row (0..6)"),
                "startCol" to Schema.integer("0-indexed start column (0..6)")
            )
        )
        val puzzleSchema = Schema.obj(
            mapOf(
                "title" to Schema.string("Creative Indonesian crossword puzzle title"),
                "topic" to Schema.string("Theme or topic name in Indonesian"),
                "clues" to Schema.array(clueSchema, minItems = 4, maxItems = 6)
            )
        )
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = puzzleSchema
                temperature = 0.85f
            }
        )
    }

    private val categorizationModel by lazy {
        val categorySchema = Schema.obj(
            mapOf(
                "category" to Schema.enumeration(
                    ExpenseCategory.values().map { it.name },
                    "Closest ExpenseCategory enum matching the transaction description"
                ),
                "confidence" to Schema.integer("Confidence percentage from 0 to 100"),
                "reasoning" to Schema.string("Short 1-sentence explanation in Indonesian for choosing this category")
            )
        )
        Firebase.ai.generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = categorySchema
                temperature = 0.2f
            }
        )
    }

    suspend fun generateDailyCrossword(dateStr: String, theme: String): CrosswordPuzzle {
        val effectiveTheme = theme.ifBlank { "Campuran Pintar Harian (Bahasa, Sains, & Teknologi)" }
        val prompt = """
            Buatkan satu Teka-Teki Silang (Crossword Puzzle) unik dan menantang untuk tanggal $dateStr dengan tema: "$effectiveTheme".
            Aturan grid 7x7 (baris 0..6, kolom 0..6):
            1. Hasilkan 5 petunjuk (kombinasi ACROSS dan DOWN) yang saling berpotongan secara logis jika memungkinkan.
            2. Setiap jawaban ("answer") HARUS berupa 1 kata tunggal huruf kapital A-Z tanpa spasi, panjang 3 hingga 7 huruf.
            3. Pastikan koordinat startRow + panjang jawaban <= 7 untuk DOWN, dan startCol + panjang jawaban <= 7 untuk ACROSS.
            4. Buat petunjuk ("clue") yang cerdas, variatif, dan menantang dalam Bahasa Indonesia (boleh menyisipkan istilah bahasa Inggris/global bila sesuai tema).
            5. Jadikan setiap tanggal menghasilkan susunan kata yang segar dan tidak membosankan.
        """.trimIndent()

        return try {
            val response = withTimeout(REQUEST_TIMEOUT_MS) {
                crosswordModel.generateContent(prompt)
            }
            val text = response.text
                ?: throw IllegalStateException("Gemini AI mengembalikan respons kosong.")
            CrosswordJsonParser.parseCrosswordJson(text, dayId = dateStr)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed generating daily crossword via Gemini", e)
            throw e
        }
    }

    suspend fun categorizeExpense(
        description: String,
        financeType: FinanceType
    ): ExpenseCategorizationResult {
        val typeLabel = if (financeType == FinanceType.INCOME) "Pemasukan" else "Pengeluaran"
        val prompt = """
            Analisis deskripsi transaksi keuangan berikut dan tetapkan ke salah satu kategori yang paling tepat.
            Tipe transaksi: $typeLabel
            Deskripsi transaksi: "$description"

            Pilihan kategori yang tersedia:
            - FOOD: Makanan, minuman, kopi, makan siang, restoran, bahan dapur.
            - TRANSPORT: Transportasi, bensin, tol, parkir, ojek online, tiket kereta/pesawat.
            - BILLS: Tagihan listrik PLN, air PDAM, internet, pulsa, cicilan, sewa, pajak, utilitas.
            - SHOPPING: Belanja pakaian, elektronik, e-commerce, perlengkapan rumah tangga.
            - HEALTH: Kesehatan, apotek, obat, dokter, rumah sakit, gym, vitamin.
            - ENTERTAINMENT: Hiburan, bioskop, streaming musik/film, game, konser, liburan.
            - SALARY: Gaji bulanan, bonus, komisi, pendapatan freelance, dividen.
            - OTHER: Lainnya yang tidak masuk kategori di atas.

            Berikan alasan singkat berbahasa Indonesia mengapa kategori tersebut paling cocok.
        """.trimIndent()

        return try {
            val response = withTimeout(REQUEST_TIMEOUT_MS) {
                categorizationModel.generateContent(prompt)
            }
            val text = response.text
                ?: throw IllegalStateException("Gemini AI tidak mengembalikan hasil kategorisasi.")
            CrosswordJsonParser.parseCategorizationJson(text)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed categorizing expense via Gemini", e)
            throw e
        }
    }
}
