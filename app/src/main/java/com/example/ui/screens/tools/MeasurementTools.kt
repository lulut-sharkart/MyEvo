package com.example.ui.screens.tools

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CoralOrange
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.PriorityHigh
import kotlinx.coroutines.delay
import java.util.Locale

private fun formatElapsedMillis(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val centis = (ms % 1000) / 10
    return String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, centis)
}

@Composable
fun StopwatchTool() {
    var isRunning by remember { mutableStateOf(false) }
    var elapsedMs by remember { mutableLongStateOf(0L) }
    var baseElapsedMs by remember { mutableLongStateOf(0L) }
    var startRealtime by remember { mutableLongStateOf(0L) }
    var laps by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            startRealtime = SystemClock.elapsedRealtime()
            while (isRunning) {
                elapsedMs = baseElapsedMs + (SystemClock.elapsedRealtime() - startRealtime)
                delay(25L)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatElapsedMillis(elapsedMs),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = ElectricCyan,
                    modifier = Modifier.testTag("stopwatch_display")
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (isRunning) {
                                baseElapsedMs = elapsedMs
                                isRunning = false
                            } else {
                                isRunning = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) CoralOrange else EmeraldGreen
                        ),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(if (isRunning) "Jeda" else "Mulai", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            if (isRunning) {
                                laps = listOf("Lap ${laps.size + 1}: ${formatElapsedMillis(elapsedMs)}") + laps
                            } else {
                                elapsedMs = 0L
                                baseElapsedMs = 0L
                                laps = emptyList()
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(if (isRunning) "Catat Lap" else "Reset")
                    }
                }
            }
        }

        if (laps.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Catatan Putaran (Lap):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    laps.take(10).forEach { lap ->
                        Text(lap, fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CountdownTimerTool() {
    val context = LocalContext.current
    var minutesInput by remember { mutableStateOf("5") }
    var totalSeconds by remember { mutableIntStateOf(300) }
    var remainingSeconds by remember { mutableIntStateOf(300) }
    var isRunning by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            isFinished = false
            while (isRunning && remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds -= 1
            }
            if (remainingSeconds == 0 && isRunning) {
                isRunning = false
                isFinished = true
                triggerTimerCompletionFeedback(context)
            }
        }
    }

    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val progress = if (totalSeconds > 0) remainingSeconds.toFloat() / totalSeconds.toFloat() else 0f

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf(1, 3, 5, 10, 25)) { preset ->
                FilterChip(
                    selected = totalSeconds == preset * 60 && !isRunning,
                    onClick = {
                        isRunning = false
                        isFinished = false
                        minutesInput = preset.toString()
                        totalSeconds = preset * 60
                        remainingSeconds = preset * 60
                    },
                    label = { Text("${preset} Menit") }
                )
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = String.format(Locale.US, "%02d:%02d", mins, secs),
                    fontSize = 46.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = if (isFinished) PriorityHigh else CoralOrange
                )

                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = CoralOrange,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                if (isFinished) {
                    Text("⏰ Waktu Habis!", color = PriorityHigh, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = minutesInput,
                        onValueChange = { txt ->
                            val clean = txt.filter { it.isDigit() }.take(3)
                            minutesInput = clean
                            val parsed = (clean.toIntOrNull() ?: 1).coerceIn(1, 240)
                            if (!isRunning) {
                                totalSeconds = parsed * 60
                                remainingSeconds = parsed * 60
                            }
                        },
                        label = { Text("Menit") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (remainingSeconds == 0) remainingSeconds = totalSeconds
                            isRunning = !isRunning
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isRunning) CoralOrange else NeonIndigo),
                        modifier = Modifier.weight(1.2f).height(48.dp)
                    ) {
                        Text(if (isRunning) "Jeda" else "Mulai Timer", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            isRunning = false
                            isFinished = false
                            val parsed = (minutesInput.toIntOrNull() ?: 5).coerceIn(1, 240)
                            totalSeconds = parsed * 60
                            remainingSeconds = parsed * 60
                        },
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Reset")
                    }
                }
            }
        }
    }
}

@Suppress("DEPRECATION")
private fun triggerTimerCompletionFeedback(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            val v = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            v?.vibrate(500)
        }
    } catch (_: Exception) {
    }
    try {
        val tg = ToneGenerator(AudioManager.STREAM_ALARM, 80)
        tg.startTone(ToneGenerator.TONE_PROP_BEEP2, 600)
    } catch (_: Exception) {
    }
}

@Composable
fun PhysicalRulerTool() {
    val context = LocalContext.current
    val ydpi = remember {
        context.resources.displayMetrics.ydpi.takeIf { it > 40f } ?: 320f
    }
    // Pixels per millimeter using actual physical display density (ydpi)
    val pxPerMm = ydpi / 25.4f
    var caliperOffsetPx by remember { mutableFloatStateOf(pxPerMm * 30f) } // defaults to 30mm

    val measuredMm = (caliperOffsetPx / pxPerMm).coerceAtLeast(0f)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = EmeraldGreen.copy(alpha = 0.12f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Pengukuran Kaliper Geser", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = String.format(Locale.US, "%.1f mm  (%.2f cm)", measuredMm, measuredMm / 10f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldGreen
                    )
                }
                Text(
                    text = String.format(Locale.US, "%.0f DPI", ydpi),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(pxPerMm) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            caliperOffsetPx = (caliperOffsetPx + dragAmount.y).coerceIn(0f, size.height.toFloat())
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val maxMm = (size.height / pxPerMm).toInt()
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 30f
                        isAntiAlias = true
                    }

                    for (mm in 0..maxMm) {
                        val y = mm * pxPerMm
                        val tickWidth = when {
                            mm % 10 == 0 -> 76f
                            mm % 5 == 0 -> 50f
                            else -> 28f
                        }
                        drawLine(
                            color = if (mm % 10 == 0) NeonIndigo else Color.Gray,
                            start = Offset(0f, y),
                            end = Offset(tickWidth, y),
                            strokeWidth = if (mm % 10 == 0) 4f else 2f
                        )
                        if (mm % 10 == 0) {
                            drawContext.canvas.nativeCanvas.drawText(
                                "${mm / 10} cm",
                                88f,
                                y + 10f,
                                textPaint
                            )
                        }
                    }

                    // Shaded measured span + draggable caliper line
                    val clampedY = caliperOffsetPx.coerceIn(0f, size.height)
                    drawRect(
                        color = EmeraldGreen.copy(alpha = 0.14f),
                        topLeft = Offset.Zero,
                        size = androidx.compose.ui.geometry.Size(size.width, clampedY)
                    )
                    drawLine(
                        color = CoralOrange,
                        start = Offset(0f, clampedY),
                        end = Offset(size.width, clampedY),
                        strokeWidth = 5f
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CoralOrange,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Geser garis oranye untuk mengukur",
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
