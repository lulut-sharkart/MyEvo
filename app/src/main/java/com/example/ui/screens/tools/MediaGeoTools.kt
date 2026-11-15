package com.example.ui.screens.tools

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import android.os.Looper
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.CoralOrange
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.PriorityHigh
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.math.log10
import kotlin.math.sqrt

@Composable
fun CodeScannerTool() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasCameraPerm = it
    }

    var scannedResult by remember { mutableStateOf<String?>(null) }
    var scannedFormat by remember { mutableStateOf<String?>(null) }
    var scannerError by remember { mutableStateOf<String?>(null) }

    val executor = remember { Executors.newSingleThreadExecutor() }
    val reader = remember { MultiFormatReader() }
    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    DisposableEffect(Unit) {
        onDispose { executor.shutdown() }
    }

    if (hasCameraPerm) {
        DisposableEffect(lifecycleOwner) {
            val future = ProcessCameraProvider.getInstance(context)
            val mainExec = ContextCompat.getMainExecutor(context)
            var provider: ProcessCameraProvider? = null

            future.addListener({
                try {
                    val camProvider = future.get()
                    provider = camProvider
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also { ia ->
                            ia.setAnalyzer(executor) { proxy ->
                                decodeBarcodeProxy(proxy, reader)?.let { (text, format) ->
                                    mainExec.execute {
                                        scannedResult = text
                                        scannedFormat = format
                                    }
                                }
                                proxy.close()
                            }
                        }

                    val selector = if (camProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    } else {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    }
                    camProvider.unbindAll()
                    camProvider.bindToLifecycle(lifecycleOwner, selector, preview, analysis)
                    scannerError = null
                } catch (e: Exception) {
                    scannerError = e.localizedMessage ?: "Kamera pemindai tidak tersedia."
                }
            }, mainExec)

            onDispose {
                try {
                    provider?.unbindAll()
                } catch (_: Exception) {
                }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!hasCameraPerm) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = NeonIndigo, modifier = Modifier.size(44.dp))
                    Text("Izin Kamera Diperlukan untuk Kode Scanner", fontWeight = FontWeight.Bold)
                    Text("Berikan akses kamera agar dapat memindai QR Code & Barcode secara langsung.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(
                        onClick = { launcher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Izinkan Kamera & Mulai Pindai")
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.Black)
                    .border(2.dp, ElectricCyan, RoundedCornerShape(18.dp))
            ) {
                AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .align(Alignment.Center)
                        .border(2.dp, EmeraldGreen, RoundedCornerShape(16.dp))
                )
            }
            if (scannerError != null) {
                Text(scannerError ?: "", color = PriorityHigh, fontSize = 12.sp)
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Hasil Pemindaian (${scannedFormat ?: "Menunggu QR/Barcode"}):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    text = scannedResult ?: "Arahkan kamera ke kode QR atau barcode produk.",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = if (scannedResult != null) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (scannedResult != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("Scan", scannedResult))
                            Toast.makeText(context, "Hasil pindai disalin", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salin")
                        }
                        if (scannedResult!!.startsWith("http://") || scannedResult!!.startsWith("https://")) {
                            Button(
                                onClick = {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(scannedResult)))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
                            ) {
                                Text("Buka Tautan")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun decodeBarcodeProxy(image: ImageProxy, reader: MultiFormatReader): Pair<String, String>? {
    return try {
        val plane = image.planes.firstOrNull() ?: return null
        val buffer = plane.buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)
        val source = PlanarYUVLuminanceSource(
            data, image.width, image.height, 0, 0, image.width, image.height, false
        )
        val bitmap = BinaryBitmap(HybridBinarizer(source))
        val res = reader.decodeWithState(bitmap)
        res.text to res.barcodeFormat.name
    } catch (_: Exception) {
        null
    } finally {
        reader.reset()
    }
}

@SuppressLint("MissingPermission")
@Composable
fun LocationGpsTool() {
    val context = LocalContext.current
    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }

    var hasPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
        hasPerm = map[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            map[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var addressLabel by remember { mutableStateOf<String?>(null) }

    DisposableEffect(hasPerm) {
        if (!hasPerm) {
            onDispose { }
        } else {
            val initial = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (initial != null) currentLocation = initial

            val listener = LocationListener { loc -> currentLocation = loc }
            try {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2500L, 1f, listener, Looper.getMainLooper())
                }
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2500L, 1f, listener, Looper.getMainLooper())
                }
            } catch (_: Exception) {
            }
            onDispose { locationManager.removeUpdates(listener) }
        }
    }

    LaunchedEffect(currentLocation) {
        val loc = currentLocation ?: return@LaunchedEffect
        addressLabel = withContext(Dispatchers.IO) {
            try {
                @Suppress("DEPRECATION")
                val list = Geocoder(context, Locale.getDefault()).getFromLocation(loc.latitude, loc.longitude, 1)
                list?.firstOrNull()?.getAddressLine(0)
            } catch (_: Exception) {
                null
            }
        }
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = PriorityHigh)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pelacak Koordinat Lokasi GPS", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            if (!hasPerm) {
                Text("Izin lokasi diperlukan untuk membaca koordinat GPS & alamat perangkat.", fontSize = 12.sp)
                Button(
                    onClick = {
                        permLauncher.launch(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Izinkan Akses Lokasi")
                }
            } else if (currentLocation == null) {
                Text("Mencari sinyal satelit GPS / jaringan...", fontSize = 13.sp, color = ElectricCyan)
            } else {
                val loc = currentLocation!!
                Text(
                    text = String.format(Locale.US, "Lat: %.6f\nLon: %.6f", loc.latitude, loc.longitude),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = EmeraldGreen
                )
                Text(
                    text = String.format(
                        Locale.US,
                        "Akurasi: ±%.1f m  •  Elevasi: %.1f mdpl  •  Kecepatan: %.1f km/j",
                        loc.accuracy, loc.altitude, loc.speed * 3.6f
                    ),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!addressLabel.isNullOrBlank()) {
                    Text("Alamat: $addressLabel", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("GPS", "${loc.latitude},${loc.longitude}"))
                        Toast.makeText(context, "Koordinat disalin", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Salin Koordinat GPS")
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun AudioMeterAndMicTool(isVisualizerMode: Boolean) {
    val context = LocalContext.current
    var hasMicPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasMicPerm = it
    }

    var isListening by remember { mutableStateOf(true) }
    var currentDb by remember { mutableFloatStateOf(0f) }
    var peakDb by remember { mutableFloatStateOf(0f) }
    var waveBars by remember { mutableStateOf(List(24) { 0f }) }
    var micError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(hasMicPerm, isListening) {
        if (!hasMicPerm || !isListening) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            val sampleRate = 8000
            val minBuf = AudioRecord.getMinBufferSize(
                sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(1024)

            var recorder: AudioRecord? = null
            try {
                recorder = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBuf
                )
                if (recorder.state != AudioRecord.STATE_INITIALIZED) {
                    micError = "Mikrofon tidak dapat diinisialisasi pada perangkat ini."
                    return@withContext
                }
                recorder.startRecording()
                val buf = ShortArray(minBuf / 2)
                while (isActive && isListening) {
                    val readCount = recorder.read(buf, 0, buf.size)
                    if (readCount > 0) {
                        var sumSq = 0.0
                        for (i in 0 until readCount) {
                            val s = buf[i].toDouble()
                            sumSq += s * s
                        }
                        val rms = sqrt(sumSq / readCount)
                        val db = if (rms > 1.0) (20.0 * log10(rms)).toFloat().coerceIn(0f, 110f) else 0f
                        val normLevel = (db / 95f).coerceIn(0.05f, 1f)
                        withContext(Dispatchers.Main) {
                            currentDb = db
                            if (db > peakDb) peakDb = db
                            waveBars = (waveBars.drop(1) + normLevel)
                            micError = null
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    micError = e.localizedMessage ?: "Gagal membaca mikrofon."
                }
            } finally {
                try {
                    recorder?.stop()
                    recorder?.release()
                } catch (_: Exception) {
                }
            }
        }
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Mic, contentDescription = null, tint = CoralOrange)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isVisualizerMode) "Monitor Mikrofon & Spektrum" else "Pengukur Kebisingan Suara (dB)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            if (!hasMicPerm) {
                Text("Izin mikrofon (RECORD_AUDIO) diperlukan untuk mengukur intensitas suara & gelombang audio.", fontSize = 12.sp)
                Button(
                    onClick = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralOrange),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Izinkan Mikrofon")
                }
            } else {
                Text(
                    text = String.format(Locale.US, "%.1f dB", currentDb),
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (currentDb > 75f) PriorityHigh else EmeraldGreen
                )
                Text(
                    text = String.format(Locale.US, "Puncak: %.1f dB", peakDb),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    val count = waveBars.size
                    val barW = size.width / (count * 1.4f)
                    val gap = (size.width - count * barW) / (count + 1)
                    waveBars.forEachIndexed { idx, level ->
                        val barH = (level * (size.height - 16f)).coerceAtLeast(8f)
                        val x = gap + idx * (barW + gap)
                        val y = (size.height - barH) / 2f
                        drawRect(
                            color = if (level > 0.78f) PriorityHigh else NeonIndigo,
                            topLeft = Offset(x, y),
                            size = Size(barW, barH)
                        )
                    }
                }

                if (micError != null) Text(micError ?: "", color = PriorityHigh, fontSize = 11.sp)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { isListening = !isListening },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isListening) CoralOrange else EmeraldGreen),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text(if (isListening) "Jeda Monitor" else "Lanjutkan", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = { peakDb = 0f },
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Text("Reset Puncak")
                    }
                }
            }
        }
    }
}
