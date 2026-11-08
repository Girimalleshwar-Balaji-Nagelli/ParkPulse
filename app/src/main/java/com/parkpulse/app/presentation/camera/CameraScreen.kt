package com.parkpulse.app.presentation.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parkpulse.app.presentation.theme.ParksColors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private suspend fun Context.cameraProvider(): ProcessCameraProvider = suspendCoroutine { cont ->
    val future = ProcessCameraProvider.getInstance(this)
    future.addListener({ cont.resume(future.get()) }, ContextCompat.getMainExecutor(this))
}

private enum class FlashSetting(val label: String, val mode: Int) {
    OFF("Off", ImageCapture.FLASH_MODE_OFF),
    ON("On", ImageCapture.FLASH_MODE_ON),
    AUTO("Auto", ImageCapture.FLASH_MODE_AUTO);

    fun next() = entries[(ordinal + 1) % entries.size]
}

@Composable
fun CameraScreen(
    onBack: () -> Unit,
    onPhotoConfirmed: (encodedDraft: String) -> Unit,
    viewModel: CameraViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // ---- permissions (camera + location for the GPS stamp) ----
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val allPermissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        hasCameraPermission = result[Manifest.permission.CAMERA] == true
        viewModel.onPermissionResult(
            result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        )
    }
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(allPermissions)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CameraEvent.PhotoSaved -> onPhotoConfirmed(event.draft.encode())
            }
        }
    }

    LifecycleStartEffect(Unit) {
        viewModel.onScreenStart()
        onStopOrDispose { viewModel.onScreenStop() }
    }

    // ---- CameraX ----
    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            // ~3 MP is plenty for a survey photo and keeps stamping memory-safe on low-end phones.
            .setResolutionSelector(
                ResolutionSelector.Builder()
                    .setResolutionStrategy(
                        ResolutionStrategy(Size(2048, 1536), ResolutionStrategy.FALLBACK_RULE_CLOSEST_LOWER_THEN_HIGHER)
                    )
                    .build()
            )
            .build()
    }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var flash by remember { mutableStateOf(FlashSetting.OFF) }
    var hasFlashUnit by remember { mutableStateOf(false) }

    LaunchedEffect(lensFacing, hasCameraPermission) {
        if (!hasCameraPermission) return@LaunchedEffect
        try {
            val provider = context.cameraProvider()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
            val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            provider.unbindAll()
            val camera = provider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
            hasFlashUnit = camera.cameraInfo.hasFlashUnit()
        } catch (e: Exception) {
            viewModel.onCaptureFailed()
        }
    }
    LaunchedEffect(flash, hasFlashUnit) {
        imageCapture.flashMode = if (hasFlashUnit) flash.mode else ImageCapture.FLASH_MODE_OFF
    }

    fun takePhoto() {
        val stamp = viewModel.beginCapture() ?: return // blocked: no trustworthy GPS fix yet
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val rotation = image.imageInfo.rotationDegrees
                    val bitmap = try {
                        image.toBitmap()
                    } catch (e: Exception) {
                        null
                    } finally {
                        image.close()
                    }
                    if (bitmap == null) viewModel.onCaptureFailed()
                    else viewModel.onImageCaptured(bitmap, rotation, stamp)
                }

                override fun onError(exception: ImageCaptureException) {
                    viewModel.onCaptureFailed()
                }
            },
        )
    }

    // ---- UI (reference screen 6: rounded preview, stamp box bottom-left, Flip | shutter | Flash) ----
    val phase = state.phase
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ParksColors.BackgroundMint)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(ParksColors.SecondaryGreen.copy(alpha = 0.25f)),
        ) {
            when {
                phase is CameraPhase.Review -> Image(
                    bitmap = phase.bitmap.asImageBitmap(),
                    contentDescription = "Captured photo with stamp",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
                !hasCameraPermission -> Column(
                    Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        "Camera permission is needed to take survey photos.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = ParksColors.DarkGreen,
                    )
                    TextButton(onClick = { permissionLauncher.launch(allPermissions) }) { Text("Allow camera") }
                }
                else -> AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
            }

            if (phase is CameraPhase.Live || phase is CameraPhase.Processing) {
                StampOverlay(
                    data = state.stampPreview,
                    parkName = state.parkName,
                    waitingText = state.gpsMessage,
                    modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
                )
            }

            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).padding(4.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Close camera", tint = ParksColors.DarkGreen)
            }
            if (phase is CameraPhase.Processing || phase is CameraPhase.Saving) {
                CircularProgressIndicator(Modifier.align(Alignment.Center), color = ParksColors.PrimaryGreen)
            }
            state.message?.let { msg ->
                Text(
                    msg,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                        .background(ParksColors.White, RoundedCornerShape(12.dp))
                        .clickable { viewModel.dismissMessage() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = ParksColors.ErrorRed,
                )
            }
        }

        when (phase) {
            is CameraPhase.Review, CameraPhase.Saving -> ReviewControls(
                enabled = phase is CameraPhase.Review,
                onRetake = viewModel::retake,
                onUse = viewModel::confirm,
            )
            else -> LiveControls(
                shutterEnabled = state.canCapture && hasCameraPermission,
                flashLabel = if (hasFlashUnit) "Flash: ${flash.label}" else "Flash: n/a",
                flashEnabled = hasFlashUnit,
                onFlip = {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT
                    else CameraSelector.LENS_FACING_BACK
                },
                onFlash = { flash = flash.next() },
                onShutter = ::takePhoto,
            )
        }
    }
}

@Composable
private fun StampOverlay(
    data: StampData?,
    parkName: String,
    waitingText: String?,
    modifier: Modifier = Modifier,
) {
    val lines = if (data != null) StampText.lines(data) else listOf(parkName, waitingText ?: "Waiting for GPS…")
    Column(
        modifier = modifier
            .background(ParksColors.White.copy(alpha = 0.88f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        lines.forEachIndexed { i, line ->
            Text(
                text = line,
                style = if (i == 0) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium,
                fontWeight = if (i == 0) FontWeight.Bold else FontWeight.Normal,
                color = if (data == null && i > 0) ParksColors.ErrorRed else ParksColors.Black,
            )
        }
    }
}

@Composable
private fun LiveControls(
    shutterEnabled: Boolean,
    flashLabel: String,
    flashEnabled: Boolean,
    onFlip: () -> Unit,
    onFlash: () -> Unit,
    onShutter: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().height(96.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onFlip) {
            Text("Flip", style = MaterialTheme.typography.titleMedium, color = ParksColors.DarkGreen)
        }
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .border(3.dp, ParksColors.PrimaryGreen, CircleShape)
                .clickable(enabled = shutterEnabled, onClick = onShutter)
                .padding(8.dp),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(ParksColors.PrimaryGreen.copy(alpha = if (shutterEnabled) 0.45f else 0.12f)),
            )
        }
        TextButton(onClick = onFlash, enabled = flashEnabled) {
            Text(flashLabel, style = MaterialTheme.typography.titleMedium, color = ParksColors.DarkGreen)
        }
    }
}

@Composable
private fun ReviewControls(enabled: Boolean, onRetake: () -> Unit, onUse: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(96.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onRetake, enabled = enabled) {
            Text("Retake", style = MaterialTheme.typography.titleMedium, color = ParksColors.DarkGreen)
        }
        TextButton(onClick = onUse, enabled = enabled) {
            Text("Use photo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ParksColors.DarkGreen)
        }
    }
}
