package com.example.ui.screens.tools

import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.pow

enum class ToolCategory(val label: String) {
    ALL("Semua"),
    CALCULATOR("Kalkulator"),
    SENSOR("Sensor"),
    MEDIA_GEO("Kamera & Audio"),
    MEASURE("Waktu & Ukur")
}

enum class ToolId(
    val title: String,
    val subtitle: String,
    val emoji: String,
    val category: ToolCategory,
    val accentHex: Long
) {
    CALCULATOR("Kalkulator", "Aritmetika cepat & riwayat hitung", "🧮", ToolCategory.CALCULATOR, 0xFF6366F1),
    PERIOD_CALC("Kalkulator Haid", "Prediksi siklus, ovulasi & masa subur", "🌸", ToolCategory.CALCULATOR, 0xFFEC4899),
    WEIGHT_BMI("Kalkulator Berat", "IMT/BMI, berat ideal & kalori BMR", "⚖️", ToolCategory.CALCULATOR, 0xFF10B981),
    CURRENCY("Mata Uang", "Kurs mata uang global real-time", "💱", ToolCategory.CALCULATOR, 0xFFF59E0B),
    UNIT_CONVERTER("Konverter Satuan", "Panjang, massa, suhu & data digital", "🔄", ToolCategory.CALCULATOR, 0xFF06B6D4),
    PASSWORD_GEN("Generator Sandi", "Kata sandi acak kriptografis", "🔑", ToolCategory.CALCULATOR, 0xFF8B5CF6),
    CODE_SCANNER("Kode Scanner", "Pemindai QR & Barcode CameraX", "📷", ToolCategory.MEDIA_GEO, 0xFF6366F1),
    STOPWATCH("Stopwatch", "Penghitung milidetik & catatan lap", "⏱️", ToolCategory.MEASURE, 0xFF06B6D4),
    TIMER("Timer Hitung Mundur", "Preset Pomodoro & alarm getar", "⏳", ToolCategory.MEASURE, 0xFFF97316),
    RULER("Penggaris", "Skala mm & cm terkalibrasi DPI", "📏", ToolCategory.MEASURE, 0xFF10B981),
    LOCATION("Lokasi GPS", "Koordinat GPS, elevasi & alamat", "📍", ToolCategory.MEDIA_GEO, 0xFFEF4444),
    SOUND_METER("Pengukur Suara", "Tingkat kebisingan dB via mikrofon", "🔊", ToolCategory.MEDIA_GEO, 0xFFF97316),
    MIC_MONITOR("Mic & Visualizer", "Monitor spektrum suara langsung", "🎙️", ToolCategory.MEDIA_GEO, 0xFFEC4899),
    THERMOMETER("Termometer", "Sensor suhu ambient & baterai", "🌡️", ToolCategory.SENSOR, 0xFFEF4444),
    LUX_METER("Lux Meter", "Intensitas cahaya lingkungan (lx)", "💡", ToolCategory.SENSOR, 0xFFF59E0B),
    SPIRIT_LEVEL("Waterpass / Level", "Kemiringan bidang 2-sumbu presisi", "📐", ToolCategory.SENSOR, 0xFF10B981),
    COMPASS("Kompas Digital", "Azimut magnetik utara & arah mata angin", "🧭", ToolCategory.SENSOR, 0xFF3B82F6),
    FLASHLIGHT("Senter & SOS", "Lampu kilat perangkat & mode darurat", "🔦", ToolCategory.MEDIA_GEO, 0xFFF59E0B)
}

data class PeriodPredictionResult(
    val nextPeriodDate: String,
    val ovulationDate: String,
    val fertileStartDate: String,
    val fertileEndDate: String,
    val currentPhaseLabel: String,
    val upcomingCycles: List<String>
)

object PeriodCalculatorMath {
    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun calculate(
        lastPeriodDateStr: String,
        cycleLengthDays: Int = 28,
        periodDurationDays: Int = 5,
        referenceDateStr: String? = null
    ): PeriodPredictionResult {
        val safeCycle = cycleLengthDays.coerceIn(21, 45)
        val safeDuration = periodDurationDays.coerceIn(2, 10)
        val baseCal = Calendar.getInstance()
        try {
            val parsed = fmt.parse(lastPeriodDateStr.trim())
            if (parsed != null) baseCal.time = parsed
        } catch (_: Exception) {
        }

        val nextCal = (baseCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, safeCycle) }
        val ovulationOffset = (safeCycle - 14).coerceAtLeast(8)
        val ovCal = (baseCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, ovulationOffset) }
        val fertileStart = (ovCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, -4) }
        val fertileEnd = (ovCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }

        val upcoming = (1..3).map { multiplier ->
            val c = (baseCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, safeCycle * multiplier) }
            fmt.format(c.time)
        }

        val refCal = Calendar.getInstance()
        if (referenceDateStr != null) {
            try {
                val p = fmt.parse(referenceDateStr)
                if (p != null) refCal.time = p
            } catch (_: Exception) {
            }
        }
        val diffDays = ((refCal.timeInMillis - baseCal.timeInMillis) / (1000L * 60 * 60 * 24)).toInt()
        val cycleDay = if (diffDays >= 0) (diffDays % safeCycle) + 1 else 1
        val phase = when {
            cycleDay <= safeDuration -> "Fase Menstruasi (Hari ke-$cycleDay)"
            cycleDay in (ovulationOffset - 4)..(ovulationOffset + 1) -> "Masa Subur / Ovulasi (Hari ke-$cycleDay)"
            cycleDay < ovulationOffset -> "Fase Folikuler (Hari ke-$cycleDay)"
            else -> "Fase Luteal (Hari ke-$cycleDay)"
        }

        return PeriodPredictionResult(
            nextPeriodDate = fmt.format(nextCal.time),
            ovulationDate = fmt.format(ovCal.time),
            fertileStartDate = fmt.format(fertileStart.time),
            fertileEndDate = fmt.format(fertileEnd.time),
            currentPhaseLabel = phase,
            upcomingCycles = upcoming
        )
    }
}

data class BmiResult(
    val bmi: Double,
    val category: String,
    val idealMinKg: Double,
    val idealMaxKg: Double,
    val estimatedBmrKcal: Int
)

object BmiCalculatorMath {
    fun calculate(weightKg: Double, heightCm: Double, ageYears: Int = 25, isMale: Boolean = true): BmiResult {
        val safeHeightCm = heightCm.coerceIn(80.0, 240.0)
        val safeWeightKg = weightKg.coerceIn(20.0, 300.0)
        val safeAge = ageYears.coerceIn(10, 110)

        val heightM = safeHeightCm / 100.0
        val bmi = safeWeightKg / heightM.pow(2.0)

        val category = when {
            bmi < 18.5 -> "Berat Kurang (Underweight)"
            bmi < 23.0 -> "Normal Ideal (Asia-Pasifik)"
            bmi < 25.0 -> "Batas Atas / Berisiko"
            bmi < 30.0 -> "Kelebihan Berat (Overweight)"
            else -> "Obesitas"
        }

        val idealMin = 18.5 * heightM.pow(2.0)
        val idealMax = 22.9 * heightM.pow(2.0)
        val bmrBase = (10.0 * safeWeightKg) + (6.25 * safeHeightCm) - (5.0 * safeAge)
        val bmr = if (isMale) (bmrBase + 5).toInt() else (bmrBase - 161).toInt()

        return BmiResult(
            bmi = bmi,
            category = category,
            idealMinKg = idealMin,
            idealMaxKg = idealMax,
            estimatedBmrKcal = bmr.coerceAtLeast(800)
        )
    }
}

object PasswordGeneratorHelper {
    private const val UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ"
    private const val LOWER = "abcdefghijkmnopqrstuvwxyz"
    private const val DIGITS = "23456789"
    private const val SYMBOLS = "!@#$%^&*-_+=?"

    fun generate(length: Int = 16, includeSymbols: Boolean = true): String {
        val pool = UPPER + LOWER + DIGITS + if (includeSymbols) SYMBOLS else ""
        val rng = SecureRandom()
        return buildString(length) {
            repeat(length.coerceIn(8, 32)) {
                append(pool[rng.nextInt(pool.length)])
            }
        }
    }
}
