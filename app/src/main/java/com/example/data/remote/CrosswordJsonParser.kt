package com.example.data.remote

import com.example.data.model.CrosswordClue
import com.example.data.model.CrosswordPuzzle
import com.example.data.model.ExpenseCategory
import org.json.JSONObject
import java.util.Locale

data class ExpenseCategorizationResult(
    val category: ExpenseCategory,
    val confidence: Int,
    val reasoning: String
)

object CrosswordJsonParser {

    fun parseCrosswordJson(rawJson: String, dayId: String, gridSize: Int = 7): CrosswordPuzzle {
        val cleaned = extractJsonObject(rawJson)
        val root = JSONObject(cleaned)
        val title = root.optString("title", "Teka-Teki Silang AI Harian").trim()
            .ifBlank { "Teka-Teki Silang AI Harian" }
        val topic = root.optString("topic", "Pengetahuan Umum & Bahasa").trim()
            .ifBlank { "Pengetahuan Umum & Bahasa" }

        val cluesArray = root.optJSONArray("clues")
            ?: throw IllegalStateException("Respons Gemini tidak menyertakan daftar petunjuk teka-teki.")

        val validClues = mutableListOf<CrosswordClue>()
        val usedNumbers = mutableSetOf<Int>()

        for (i in 0 until cluesArray.length()) {
            val obj = cluesArray.optJSONObject(i) ?: continue
            val rawNum = obj.optInt("number", i + 1)
            val number = if (rawNum > 0 && usedNumbers.add(rawNum)) rawNum else {
                var next = i + 1
                while (!usedNumbers.add(next)) next++
                next
            }
            val direction = obj.optString("direction", "ACROSS").uppercase(Locale.US).let {
                if (it == "DOWN") "DOWN" else "ACROSS"
            }
            val clueText = obj.optString("clue", "").trim()
            val sanitizedAnswer = obj.optString("answer", "")
                .uppercase(Locale.US)
                .filter { it in 'A'..'Z' }
                .take(gridSize)

            if (clueText.isBlank() || sanitizedAnswer.length < 2) continue

            val rawRow = obj.optInt("startRow", 0).coerceIn(0, gridSize - 1)
            val rawCol = obj.optInt("startCol", 0).coerceIn(0, gridSize - 1)

            val (row, col) = if (direction == "ACROSS") {
                val maxCol = (gridSize - sanitizedAnswer.length).coerceAtLeast(0)
                rawRow to rawCol.coerceAtMost(maxCol)
            } else {
                val maxRow = (gridSize - sanitizedAnswer.length).coerceAtLeast(0)
                rawRow.coerceAtMost(maxRow) to rawCol
            }

            validClues.add(
                CrosswordClue(
                    number = number,
                    direction = direction,
                    clue = clueText,
                    answer = sanitizedAnswer,
                    startRow = row,
                    startCol = col
                )
            )
        }

        if (validClues.isEmpty()) {
            throw IllegalStateException("Gemini menghasilkan petunjuk kosong yang tidak dapat dimainkan.")
        }

        return CrosswordPuzzle(
            dayId = dayId,
            title = title,
            topic = topic,
            gridSize = gridSize,
            clues = validClues.take(6),
            xpReward = 250
        )
    }

    fun parseCategorizationJson(rawJson: String): ExpenseCategorizationResult {
        val cleaned = extractJsonObject(rawJson)
        val root = JSONObject(cleaned)
        val rawCategory = root.optString("category", "OTHER").uppercase(Locale.US).trim()
        val category = ExpenseCategory.values().firstOrNull { it.name == rawCategory }
            ?: ExpenseCategory.OTHER
        val confidence = root.optInt("confidence", 85).coerceIn(0, 100)
        val reasoning = root.optString("reasoning", "Dikategorikan otomatis berdasarkan deskripsi transaksi.")
            .trim()
            .ifBlank { "Dikategorikan otomatis berdasarkan deskripsi transaksi." }

        return ExpenseCategorizationResult(
            category = category,
            confidence = confidence,
            reasoning = reasoning
        )
    }

    internal fun extractJsonObject(raw: String): String {
        val trimmed = raw.trim()
        val withoutFence = trimmed
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        val start = withoutFence.indexOf('{')
        val end = withoutFence.lastIndexOf('}')
        if (start >= 0 && end > start) {
            return withoutFence.substring(start, end + 1)
        }
        return withoutFence
    }
}
