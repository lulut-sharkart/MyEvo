package com.example.ui.screens.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralOrange
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.PriorityHigh
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

@Composable
fun StandardCalculatorTool() {
    var display by remember { mutableStateOf("0") }
    var operand1 by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var resetOnNextDigit by remember { mutableStateOf(false) }
    var history by remember { mutableStateOf<List<String>>(emptyList()) }

    fun formatNum(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        return if (value % 1.0 == 0.0 && kotlin.math.abs(value) < 1e12) {
            value.toLong().toString()
        } else {
            String.format(Locale.US, "%.6f", value).trimEnd('0').trimEnd('.')
        }
    }

    fun applyOperation(op: String?) {
        val current = display.toDoubleOrNull() ?: return
        val first = operand1
        if (first != null && pendingOp != null && !resetOnNextDigit) {
            val res = when (pendingOp) {
                "+" -> first + current
                "−" -> first - current
                "×" -> first * current
                "÷" -> if (current == 0.0) Double.NaN else first / current
                else -> current
            }
            val expr = "${formatNum(first)} $pendingOp ${formatNum(current)} = ${formatNum(res)}"
            history = (listOf(expr) + history).take(5)
            display = formatNum(res)
            operand1 = if (res.isNaN()) null else res
        } else {
            operand1 = current
        }
        pendingOp = op
        resetOnNextDigit = true
    }

    fun pressKey(key: String) {
        when (key) {
            "AC" -> {
                display = "0"
                operand1 = null
                pendingOp = null
                resetOnNextDigit = false
            }
            "⌫" -> {
                display = if (display.length > 1) display.dropLast(1) else "0"
            }
            "%" -> {
                val v = display.toDoubleOrNull() ?: 0.0
                display = formatNum(v / 100.0)
            }
            "±" -> {
                val v = display.toDoubleOrNull() ?: 0.0
                display = formatNum(-v)
            }
            "+", "−", "×", "÷" -> applyOperation(key)
            "=" -> {
                applyOperation(null)
                operand1 = null
            }
            "." -> {
                if (resetOnNextDigit) {
                    display = "0."
                    resetOnNextDigit = false
                } else if (!display.contains(".")) {
                    display += "."
                }
            }
            else -> {
                if (resetOnNextDigit || display == "0") {
                    display = key
                    resetOnNextDigit = false
                } else if (display.length < 16) {
                    display += key
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (operand1 != null && pendingOp != null) {
                    Text(
                        text = "${formatNum(operand1!!)} $pendingOp",
                        fontSize = 14.sp,
                        color = NeonIndigo,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = display,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.testTag("calc_display")
                )
            }
        }

        val keypad = listOf(
            listOf("AC", "±", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "−"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "⌫", "=")
        )

        keypad.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { key ->
                    val isOp = key in listOf("+", "−", "×", "÷", "=")
                    val isUtil = key in listOf("AC", "±", "%")
                    val bgColor = when {
                        key == "=" -> EmeraldGreen
                        isOp -> NeonIndigo
                        isUtil -> CoralOrange.copy(alpha = 0.16f)
                        else -> MaterialTheme.colorScheme.surface
                    }
                    val txtColor = when {
                        isOp -> Color.White
                        isUtil -> CoralOrange
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = bgColor,
                        modifier = Modifier
                            .weight(1f)
                            .height(58.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { pressKey(key) }
                            .testTag("calc_btn_$key")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = key,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = txtColor
                            )
                        }
                    }
                }
            }
        }

        if (history.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Riwayat Perhitungan:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    history.forEach { line ->
                        Text(line, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun PeriodCalculatorTool() {
    var lastPeriodDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    }
    var cycleLength by remember { mutableIntStateOf(28) }
    var periodDuration by remember { mutableIntStateOf(5) }

    val result = remember(lastPeriodDate, cycleLength, periodDuration) {
        PeriodCalculatorMath.calculate(lastPeriodDate, cycleLength, periodDuration)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Data Siklus Menstruasi", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                OutlinedTextField(
                    value = lastPeriodDate,
                    onValueChange = { lastPeriodDate = it },
                    label = { Text("Hari Pertama Haid Terakhir (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("period_last_date_input")
                )

                Text("Panjang Siklus: $cycleLength hari", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Slider(
                    value = cycleLength.toFloat(),
                    onValueChange = { cycleLength = it.roundToInt() },
                    valueRange = 21f..40f
                )

                Text("Lama Haid: $periodDuration hari", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Slider(
                    value = periodDuration.toFloat(),
                    onValueChange = { periodDuration = it.roundToInt() },
                    valueRange = 2f..10f
                )
            }
        }

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEC4899).copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Analisis & Prediksi Siklus", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFFEC4899))
                Text("Fase Saat Ini: ${result.currentPhaseLabel}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("Perkiraan Haid Berikutnya: ${result.nextPeriodDate}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Perkiraan Ovulasi: ${result.ovulationDate}", fontSize = 13.sp)
                Text("Masa Subur: ${result.fertileStartDate} s/d ${result.fertileEndDate}", fontSize = 13.sp, color = EmeraldGreen, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Proyeksi 3 Siklus Mendatang:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                result.upcomingCycles.forEachIndexed { idx, dt ->
                    Text("• Siklus +${idx + 1}: $dt", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun WeightBmiCalculatorTool() {
    var weightInput by remember { mutableStateOf("62") }
    var heightInput by remember { mutableStateOf("168") }
    var ageInput by remember { mutableStateOf("25") }
    var isMale by remember { mutableStateOf(true) }

    val weight = weightInput.toDoubleOrNull() ?: 60.0
    val height = heightInput.toDoubleOrNull() ?: 165.0
    val age = ageInput.toIntOrNull() ?: 25
    val result = remember(weight, height, age, isMale) {
        BmiCalculatorMath.calculate(weight, height, age, isMale)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isMale,
                        onClick = { isMale = true },
                        label = { Text("Pria") }
                    )
                    FilterChip(
                        selected = !isMale,
                        onClick = { isMale = false },
                        label = { Text("Wanita") }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Berat (kg)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("bmi_weight_input")
                    )
                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { heightInput = it },
                        label = { Text("Tinggi (cm)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("bmi_height_input")
                    )
                    OutlinedTextField(
                        value = ageInput,
                        onValueChange = { ageInput = it },
                        label = { Text("Usia") },
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldGreen.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = String.format(Locale.US, "IMT / BMI: %.1f", result.bmi),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldGreen
                )
                Text("Kategori: ${result.category}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = String.format(Locale.US, "Rentang Berat Ideal: %.1f kg – %.1f kg", result.idealMinKg, result.idealMaxKg),
                    fontSize = 13.sp
                )
                Text(
                    text = "Kebutuhan Kalori Basal (BMR): ${result.estimatedBmrKcal} kkal/hari",
                    fontSize = 13.sp,
                    color = NeonIndigo,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun CurrencyConverterTool() {
    val scope = rememberCoroutineScope()
    val currencies = listOf("USD", "IDR", "EUR", "JPY", "SGD", "MYR", "GBP", "AUD", "SAR", "KRW", "CNY")
    var amountInput by remember { mutableStateOf("100") }
    var fromCurrency by remember { mutableStateOf("USD") }
    var toCurrency by remember { mutableStateOf("IDR") }
    var ratesMap by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lastUpdated by remember { mutableStateOf<String?>(null) }

    val httpClient = remember {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
            .build()
    }

    fun fetchLiveRates(base: String) {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val parsedRates = withContext(Dispatchers.IO) {
                    val req = Request.Builder()
                        .url("https://open.er-api.com/v6/latest/$base")
                        .get()
                        .build()
                    httpClient.newCall(req).execute().use { resp ->
                        val body = resp.body?.string().orEmpty()
                        if (!resp.isSuccessful) {
                            throw IllegalStateException("HTTP ${resp.code} dari penyedia kurs.")
                        }
                        val ratesMatch = Regex("\"rates\"\\s*:\\s*\\{([^}]*)\\}").find(body)
                            ?: throw IllegalStateException("Format data kurs tidak valid.")
                        val entries = mutableMapOf<String, Double>()
                        Regex("\"([A-Z]{3})\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)").findAll(ratesMatch.groupValues[1])
                            .forEach { m ->
                                val code = m.groupValues[1]
                                val rate = m.groupValues[2].toDoubleOrNull()
                                if (rate != null) entries[code] = rate
                            }
                        entries
                    }
                }
                ratesMap = parsedRates
                lastUpdated = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            } catch (e: Exception) {
                errorMessage = "Gagal memuat kurs langsung (${e.localizedMessage ?: "periksa koneksi internet"})."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(fromCurrency) {
        fetchLiveRates(fromCurrency)
    }

    val amount = amountInput.toDoubleOrNull() ?: 0.0
    val rate = ratesMap[toCurrency]
    val converted = if (rate != null) amount * rate else null

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Kalkulator Kurs Mata Uang", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    IconButton(onClick = { fetchLiveRates(fromCurrency) }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Refresh, contentDescription = "Muat ulang kurs", tint = NeonIndigo)
                    }
                }

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Nominal ($fromCurrency)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("currency_amount_input")
                )

                Text("Dari:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(currencies) { code ->
                        FilterChip(
                            selected = fromCurrency == code,
                            onClick = { fromCurrency = code },
                            label = { Text(code, fontSize = 11.sp) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(onClick = {
                        val prev = fromCurrency
                        fromCurrency = toCurrency
                        toCurrency = prev
                    }) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Tukar")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tukar $fromCurrency ⇄ $toCurrency")
                    }
                }

                Text("Ke:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(currencies) { code ->
                        FilterChip(
                            selected = toCurrency == code,
                            onClick = { toCurrency = code },
                            label = { Text(code, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = AmberGold.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (isLoading) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mengambil kurs real-time open.er-api.com...", fontSize = 12.sp)
                    }
                } else if (errorMessage != null) {
                    Text(errorMessage ?: "", color = PriorityHigh, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                } else if (converted != null && rate != null) {
                    val formatted = NumberFormat.getNumberInstance(Locale.US).apply {
                        maximumFractionDigits = 2
                    }.format(converted)
                    Text(
                        text = "$formatted $toCurrency",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "1 $fromCurrency = ${String.format(Locale.US, "%.4f", rate)} $toCurrency (Diperbarui $lastUpdated)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun UnitConverterTool() {
    val categories = listOf("Panjang", "Massa", "Suhu", "Data Digital")
    var selectedCat by remember { mutableStateOf("Panjang") }
    var inputVal by remember { mutableStateOf("1") }
    val num = inputVal.toDoubleOrNull() ?: 0.0

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(categories) { cat ->
                FilterChip(
                    selected = selectedCat == cat,
                    onClick = { selectedCat = cat },
                    label = { Text(cat) }
                )
            }
        }

        OutlinedTextField(
            value = inputVal,
            onValueChange = { inputVal = it },
            label = { Text("Masukkan nilai awal") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        val rows: List<Pair<String, String>> = when (selectedCat) {
            "Panjang" -> listOf(
                "Meter (m) -> Sentimeter (cm)" to "${num * 100.0} cm",
                "Meter (m) -> Kilometer (km)" to String.format(Locale.US, "%.4f km", num / 1000.0),
                "Kilometer (km) -> Mil (mi)" to String.format(Locale.US, "%.4f mi", num * 0.621371),
                "Inci (in) -> Sentimeter (cm)" to String.format(Locale.US, "%.2f cm", num * 2.54)
            )
            "Massa" -> listOf(
                "Kilogram (kg) -> Gram (g)" to "${num * 1000.0} g",
                "Kilogram (kg) -> Pon (lbs)" to String.format(Locale.US, "%.3f lbs", num * 2.20462),
                "Gram (g) -> Ons (oz)" to String.format(Locale.US, "%.3f oz", num * 0.035274)
            )
            "Suhu" -> listOf(
                "°C -> Fahrenheit (°F)" to String.format(Locale.US, "%.2f °F", num * 9.0 / 5.0 + 32.0),
                "°C -> Kelvin (K)" to String.format(Locale.US, "%.2f K", num + 273.15),
                "°F -> Celcius (°C)" to String.format(Locale.US, "%.2f °C", (num - 32.0) * 5.0 / 9.0)
            )
            else -> listOf(
                "Gigabyte (GB) -> Megabyte (MB)" to "${num * 1024.0} MB",
                "Megabyte (MB) -> Kilobyte (KB)" to "${num * 1024.0} KB",
                "Terabyte (TB) -> Gigabyte (GB)" to "${num * 1024.0} GB"
            )
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rows.forEach { (label, result) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(result, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NeonIndigo)
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordGeneratorTool() {
    val context = LocalContext.current
    var length by remember { mutableIntStateOf(16) }
    var useSymbols by remember { mutableStateOf(true) }
    var generated by remember { mutableStateOf(PasswordGeneratorHelper.generate(16, true)) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Generator Kata Sandi Kriptografis (SecureRandom)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = NeonIndigo.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = generated,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("MyEvo Password", generated))
                        Toast.makeText(context, "Disalin ke clipboard", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Salin", tint = NeonIndigo)
                    }
                }
            }

            Text("Panjang: $length karakter", fontSize = 13.sp)
            Slider(
                value = length.toFloat(),
                onValueChange = { length = it.roundToInt() },
                valueRange = 8f..32f
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sertakan Simbol Khusus (!@#...)", fontSize = 13.sp)
                Switch(checked = useSymbols, onCheckedChange = { useSymbols = it })
            }

            Button(
                onClick = { generated = PasswordGeneratorHelper.generate(length, useSymbols) },
                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Buat Kata Sandi Baru", fontWeight = FontWeight.Bold)
            }
        }
    }
}
