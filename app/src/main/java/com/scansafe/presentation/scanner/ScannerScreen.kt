package com.scansafe.presentation.scanner

import android.Manifest
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.scansafe.domain.model.ScanHistory
import com.scansafe.presentation.components.PillButton
import com.scansafe.presentation.components.PillButtonType
import com.scansafe.ui.theme.*
import timber.log.Timber
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
    onProductScanned: (String) -> Unit,
    viewModel: ScannerViewModel = hiltViewModel()
) {
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)
    val recentHistory by viewModel.recentHistory.collectAsState()
    val context = LocalContext.current
    var flashEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) cameraPermission.launchPermissionRequest()
        viewModel.scanEvent.collect { event ->
            when (event) {
                is ScannerUiEvent.ProductFound -> {
                    hapticFeedback(context)
                    onProductScanned(event.barcode)
                }
                is ScannerUiEvent.Error -> { /* Show snackbar */ }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (cameraPermission.status.isGranted) {
            CameraPreview(
                flashEnabled = flashEnabled,
                onBarcodeDetected = { viewModel.onBarcodeDetected(it) },
                modifier = Modifier.fillMaxSize()
            )
            ScannerOverlay(
                laserColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            PermissionDeniedContent(onRequestPermission = { cameraPermission.launchPermissionRequest() })
        }

        // Top bar overlay with flash toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ScanSafe",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = { flashEnabled = !flashEnabled },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Toggle Flash",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Sliding translucent bottom panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 80.dp) // Leave room above BottomNavBar
                .imePadding()
        ) {
            var manualBarcode by remember { mutableStateOf("") }
            var showManualEntry by remember { mutableStateOf(false) }

            // Manual entry input box
            AnimatedVisibility(visible = showManualEntry) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.8f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.15f)
                    )
                ) {
                    OutlinedTextField(
                        value = manualBarcode,
                        onValueChange = { manualBarcode = it },
                        label = { Text("Barcode Number", color = Color.White.copy(alpha = 0.6f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (manualBarcode.isNotBlank()) {
                                    viewModel.onManualBarcodeEntered(manualBarcode)
                                    showManualEntry = false
                                    manualBarcode = ""
                                }
                            }
                        ),
                        trailingIcon = {
                            IconButton(onClick = {
                                if (manualBarcode.isNotBlank()) {
                                    viewModel.onManualBarcodeEntered(manualBarcode)
                                    showManualEntry = false
                                    manualBarcode = ""
                                }
                            }) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Edit manually button
            Surface(
                onClick = { showManualEntry = !showManualEntry },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 8.dp),
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.15f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Enter barcode manually",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Recent scans horizontal carousel
            if (recentHistory.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                        .padding(vertical = 12.dp)
                ) {
                    Column {
                        Text(
                            text = "Recent Scans",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(recentHistory) { history ->
                                RecentScanCard(
                                    history = history,
                                    onClick = { onProductScanned(history.product.barcode) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScannerOverlay(
    laserColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser"
    )

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val cornerRadius = 20.dp.toPx()
        val boxSize = minOf(size.width, size.height) * 0.65f
        val boxLeft = (size.width - boxSize) / 2
        val boxTop = (size.height - boxSize) / 2.3f

        // Draw overlay with transparency
        drawRect(Color.Black.copy(alpha = 0.55f))

        // Clear layout inside barcode finder
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(boxLeft, boxTop),
            size = androidx.compose.ui.geometry.Size(boxSize, boxSize),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
            blendMode = androidx.compose.ui.graphics.BlendMode.Clear
        )

        // Laser sweeping line
        val laserY = boxTop + boxSize * laserPosition
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, laserColor, laserColor, Color.Transparent),
                startX = boxLeft,
                endX = boxLeft + boxSize
            ),
            start = Offset(boxLeft, laserY),
            end = Offset(boxLeft + boxSize, laserY),
            strokeWidth = 3.dp.toPx()
        )

        // Rounded Finder Corners styling
        val bracketLength = 32.dp.toPx()
        val bracketWidth = 4.dp.toPx()

        // Top-left corner
        drawLine(laserColor, Offset(boxLeft, boxTop + cornerRadius), Offset(boxLeft, boxTop + bracketLength), bracketWidth)
        drawLine(laserColor, Offset(boxLeft + cornerRadius, boxTop), Offset(boxLeft + bracketLength, boxTop), bracketWidth)

        // Top-right corner
        drawLine(laserColor, Offset(boxLeft + boxSize, boxTop + cornerRadius), Offset(boxLeft + boxSize, boxTop + bracketLength), bracketWidth)
        drawLine(laserColor, Offset(boxLeft + boxSize - bracketLength, boxTop), Offset(boxLeft + boxSize - cornerRadius, boxTop), bracketWidth)

        // Bottom-left corner
        drawLine(laserColor, Offset(boxLeft, boxTop + boxSize - bracketLength), Offset(boxLeft, boxTop + boxSize - cornerRadius), bracketWidth)
        drawLine(laserColor, Offset(boxLeft + cornerRadius, boxTop + boxSize), Offset(boxLeft + bracketLength, boxTop + boxSize), bracketWidth)

        // Bottom-right corner
        drawLine(laserColor, Offset(boxLeft + boxSize, boxTop + boxSize - bracketLength), Offset(boxLeft + boxSize, boxTop + boxSize - cornerRadius), bracketWidth)
        drawLine(laserColor, Offset(boxLeft + boxSize - bracketLength, boxTop + boxSize), Offset(boxLeft + boxSize - cornerRadius, boxTop + boxSize), bracketWidth)
    }
}

@Composable
private fun RecentScanCard(history: ScanHistory, onClick: () -> Unit) {
    val score = history.product.aiAnalysis?.healthScore
    val scoreColor = when {
        score == null -> Color.Gray
        score >= 8 -> HealthScoreScale.A
        score >= 6 -> HealthScoreScale.B
        score >= 4 -> HealthScoreScale.C
        score >= 2 -> HealthScoreScale.D
        else -> HealthScoreScale.E
    }

    val shape = RoundedCornerShape(16.dp)

    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(130.dp)
            .height(72.dp),
        color = Color.Black.copy(alpha = 0.5f),
        shape = shape,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = history.product.name,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            if (score != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(scoreColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$score/10",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "No score",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun CameraPreview(
    flashEnabled: Boolean,
    onBarcodeDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember { BarcodeScanning.getClient() }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }

    LaunchedEffect(flashEnabled, cameraControl) {
        cameraControl?.enableTorch(flashEnabled)
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(executor) { imageProxy ->
                            @androidx.camera.core.ExperimentalGetImage
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val image = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.imageInfo.rotationDegrees
                                )
                                barcodeScanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        barcodes.firstOrNull { it.valueType == Barcode.TYPE_PRODUCT || it.rawValue != null }
                                            ?.rawValue
                                            ?.let { onBarcodeDetected(it) }
                                    }
                                    .addOnFailureListener { Timber.e(it, "Barcode scan failed") }
                                    .addOnCompleteListener { imageProxy.close() }
                            } else {
                                imageProxy.close()
                            }
                        }
                    }

                try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                    cameraControl = camera.cameraControl
                } catch (e: Exception) {
                    Timber.e(e, "Camera binding failed")
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        modifier = modifier
    )
}

@Composable
private fun PermissionDeniedContent(onRequestPermission: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                Text(text = "📷", style = MaterialTheme.typography.displayMedium)
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "Camera Permission",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "ScanSafe requires access to your camera to scan barcodes and display food wellness scores instantly.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(28.dp))
                PillButton(
                    text = "Grant Camera Permission",
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun hapticFeedback(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    vibrator?.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
}
