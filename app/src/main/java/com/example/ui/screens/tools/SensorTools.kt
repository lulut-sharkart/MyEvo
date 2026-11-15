package com.example.ui.screens.tools

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.BatteryManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun ThermometerTool() {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val ambientSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) }

    var ambientCelsius by remember { mutableStateOf<Float?>(null) }
    var batteryCelsius by remember { mutableStateOf<Float?>(null) }

    LaunchedEffect(Unit) {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val tenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE) ?: Int.MIN_VALUE
        if (tenths != Int.MIN_VALUE) {
            batteryCelsius = tenths / 10f
        }
    }

    DisposableEffect(ambientSensor) {
        if (ambientSensor == null) {
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    ambientCelsius = event.values.firstOrNull()
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            sensorManager.registerListener(listener, ambientSensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Sensor Suhu Lingkungan (Ambient)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (ambientSensor == null) {
                    Text(
                        text = "Sensor suhu udara (TYPE_AMBIENT_TEMPERATURE) tidak tersedia pada perangkat keras ponsel ini.",
                        fontSize = 13.sp,
                        color = CoralOrange,
                        textAlign = TextAlign.Center
                    )
                } else {
                    val tempC = ambientCelsius
                    Text(
                        text = if (tempC != null) String.format(Locale.US, "%.1f °C", tempC) else "Membaca sensor...",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PriorityHigh
                    )
                    if (tempC != null) {
                        val f = tempC * 9f / 5f + 32f
                        Text(
                            text = String.format(Locale.US, "%.1f °F  •  %.1f K", f, tempC + 273.15f),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = AmberGold.copy(alpha = 0.12f))
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Termometer Sensor Baterai Perangkat", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                val bat = batteryCelsius
                Text(
                    text = if (bat != null) String.format(Locale.US, "%.1f °C (%.1f °F)", bat, bat * 9f / 5f + 32f) else "Data baterai tidak tersedia",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberGold
                )
            }
        }
    }
}

@Composable
fun LuxMeterTool() {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val lightSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) }

    var luxValue by remember { mutableStateOf<Float?>(null) }
    var peakLux by remember { mutableFloatStateOf(0f) }

    DisposableEffect(lightSensor) {
        if (lightSensor == null) {
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val v = event.values.firstOrNull() ?: return
                    luxValue = v
                    if (v > peakLux) peakLux = v
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Pengukur Intensitas Cahaya (Lux)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            if (lightSensor == null) {
                Text(
                    text = "Sensor cahaya (TYPE_LIGHT) tidak tersedia pada perangkat ini.",
                    color = CoralOrange,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )
            } else {
                val cur = luxValue ?: 0f
                val label = when {
                    cur < 15f -> "Sangat Gelap"
                    cur < 100f -> "Pencahayaan Redup / Kamar"
                    cur < 500f -> "Cahaya Ruangan Kerja Ideal"
                    cur < 5000f -> "Terang / Luar Ruangan Teduh"
                    else -> "Sinar Matahari Langsung"
                }
                Text(
                    text = String.format(Locale.US, "%.0f lx", cur),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = AmberGold
                )
                Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(
                    text = String.format(Locale.US, "Puncak terukur: %.0f lx", peakLux),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SpiritLevelTool() {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    var pitchDeg by remember { mutableFloatStateOf(0f) }
    var rollDeg by remember { mutableFloatStateOf(0f) }

    DisposableEffect(accelSensor) {
        if (accelSensor == null) {
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val ax = event.values[0]
                    val ay = event.values[1]
                    val az = event.values[2]
                    val newRoll = Math.toDegrees(atan2(-ax.toDouble(), sqrt((ay * ay + az * az).toDouble()))).toFloat()
                    val newPitch = Math.toDegrees(atan2(ay.toDouble(), sqrt((ax * ax + az * az).toDouble()))).toFloat()
                    rollDeg = rollDeg * 0.75f + newRoll * 0.25f
                    pitchDeg = pitchDeg * 0.75f + newPitch * 0.25f
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }

    val isBalanced = abs(rollDeg) < 1.5f && abs(pitchDeg) < 1.5f

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (accelSensor == null) {
                Text("Sensor akselerometer tidak tersedia pada perangkat ini.", color = CoralOrange)
            } else {
                Text(
                    text = if (isBalanced) "DATAR PRESISI (0°)" else "Miring",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = if (isBalanced) EmeraldGreen else CoralOrange
                )
                Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.minDimension / 2f - 12f
                        val ringColor = if (isBalanced) EmeraldGreen else NeonIndigo

                        drawCircle(color = ringColor.copy(alpha = 0.12f), radius = radius, center = center)
                        drawCircle(color = ringColor, radius = radius, center = center, style = Stroke(width = 5f))
                        drawCircle(color = ringColor.copy(alpha = 0.6f), radius = 26f, center = center, style = Stroke(width = 3f))

                        val maxShift = radius - 26f
                        val dx = ((rollDeg / 45f).coerceIn(-1f, 1f)) * maxShift
                        val dy = ((pitchDeg / 45f).coerceIn(-1f, 1f)) * maxShift
                        drawCircle(
                            color = if (isBalanced) EmeraldGreen else CoralOrange,
                            radius = 22f,
                            center = Offset(center.x + dx, center.y + dy)
                        )
                    }
                }
                Text(
                    text = String.format(Locale.US, "Roll (X): %+.1f°   •   Pitch (Y): %+.1f°", rollDeg, pitchDeg),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun DigitalCompassTool() {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    val magSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) }

    var azimuthDeg by remember { mutableFloatStateOf(0f) }
    val hasHardware = accelSensor != null && magSensor != null

    DisposableEffect(hasHardware) {
        if (!hasHardware) {
            onDispose { }
        } else {
            val gravity = FloatArray(3)
            val geomagnetic = FloatArray(3)
            val rMat = FloatArray(9)
            val iMat = FloatArray(9)
            val orientation = FloatArray(3)
            var hasG = false
            var hasM = false

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val alpha = 0.8f
                    if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                        for (i in 0..2) gravity[i] = alpha * gravity[i] + (1 - alpha) * event.values[i]
                        hasG = true
                    } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                        for (i in 0..2) geomagnetic[i] = alpha * geomagnetic[i] + (1 - alpha) * event.values[i]
                        hasM = true
                    }
                    if (hasG && hasM && SensorManager.getRotationMatrix(rMat, iMat, gravity, geomagnetic)) {
                        SensorManager.getOrientation(rMat, orientation)
                        val deg = ((Math.toDegrees(orientation[0].toDouble()) + 360.0) % 360.0).toFloat()
                        azimuthDeg = deg
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(listener, magSensor, SensorManager.SENSOR_DELAY_UI)
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }

    val cardinal = when (((azimuthDeg + 22.5f) % 360f).toInt() / 45) {
        0 -> "Utara (U)"
        1 -> "Timur Laut (TL)"
        2 -> "Timur (T)"
        3 -> "Tenggara (TG)"
        4 -> "Selatan (S)"
        5 -> "Barat Daya (BD)"
        6 -> "Barat (B)"
        else -> "Barat Laut (BL)"
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!hasHardware) {
                Text(
                    text = "Sensor magnetometer / kompas tidak tersedia pada perangkat ini.",
                    color = CoralOrange,
                    textAlign = TextAlign.Center
                )
            } else {
                Text("${azimuthDeg.toInt()}° $cardinal", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = ElectricCyan)
                Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.minDimension / 2f - 14f
                        drawCircle(color = NeonIndigo.copy(alpha = 0.08f), radius = radius, center = center)
                        drawCircle(color = NeonIndigo, radius = radius, center = center, style = Stroke(width = 5f))

                        val rad = Math.toRadians((-azimuthDeg).toDouble() - 90.0)
                        val tip = Offset(
                            center.x + (cos(rad) * (radius - 18f)).toFloat(),
                            center.y + (sin(rad) * (radius - 18f)).toFloat()
                        )
                        val tail = Offset(
                            center.x - (cos(rad) * (radius * 0.45f)).toFloat(),
                            center.y - (sin(rad) * (radius * 0.45f)).toFloat()
                        )
                        drawLine(color = PriorityHigh, start = center, end = tip, strokeWidth = 10f, cap = StrokeCap.Round)
                        drawLine(color = Color.Gray, start = center, end = tail, strokeWidth = 8f, cap = StrokeCap.Round)
                        drawCircle(color = Color.White, radius = 10f, center = center)
                    }
                }
                Text("Kalibrasi: gerakkan ponsel membentuk angka 8 jika arah melenceng.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun FlashlightTool() {
    val context = LocalContext.current
    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    val torchCameraId = remember {
        try {
            cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (_: Exception) {
            null
        }
    }

    var isTorchOn by remember { mutableStateOf(false) }
    var isSosActive by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    fun setTorch(state: Boolean) {
        val id = torchCameraId ?: return
        try {
            cameraManager.setTorchMode(id, state)
            isTorchOn = state
            errorMsg = null
        } catch (e: Exception) {
            errorMsg = e.localizedMessage
        }
    }

    LaunchedEffect(isSosActive) {
        if (isSosActive && torchCameraId != null) {
            val pattern = listOf(180L, 180L, 180L, 420L, 420L, 420L, 180L, 180L, 180L)
            while (isSosActive) {
                for (dur in pattern) {
                    if (!isSosActive) break
                    setTorch(true)
                    delay(dur)
                    setTorch(false)
                    delay(180L)
                }
                delay(650L)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                if (torchCameraId != null) cameraManager.setTorchMode(torchCameraId, false)
            } catch (_: Exception) {
            }
        }
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (torchCameraId == null) {
                Text("Perangkat ini tidak memiliki lampu kilat kamera (Flash).", color = CoralOrange)
            } else {
                Text(if (isTorchOn) "SENTER MENYALA 🔦" else "Senter Mati", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            isSosActive = false
                            setTorch(!isTorchOn)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isTorchOn) CoralOrange else EmeraldGreen),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(if (isTorchOn) "Matikan" else "Nyalakan", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = {
                            isSosActive = !isSosActive
                            if (!isSosActive) setTorch(false)
                        },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(if (isSosActive) "Stop SOS" else "Mode SOS")
                    }
                }
                if (errorMsg != null) Text(errorMsg ?: "", color = PriorityHigh, fontSize = 11.sp)
            }
        }
    }
}
