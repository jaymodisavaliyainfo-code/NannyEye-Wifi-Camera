package monitoringcamera.transmitterconnect.officeconnectcamera

import android.annotation.SuppressLint
import android.hardware.Camera
import android.media.*
import android.os.Build
import android.os.CountDownTimer
import android.os.Environment
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.util.Locale

import androidx.compose.ui.tooling.preview.Preview
import monitoringcamera.transmitterconnect.officeconnectcamera.ui.theme.NannyEyeWiFiCameraMonitorTheme
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import monitoringcamera.transmitterconnect.officeconnectcamera.R

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Suppress("DEPRECATION")
@Composable
fun RecordVideoScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var isRecording by remember { mutableStateOf(false) }
    var timerText by remember { mutableStateOf("00:00") }
    var isFlashOn by remember { mutableStateOf(false) }
    var currentCameraId by remember { mutableIntStateOf(getFrontCameraId()) }
    var hasStartedAutomatically by remember { mutableStateOf(false) }
    var shouldResumeRecordingAfterFlip by remember { mutableStateOf(false) }
    var accumulatedTimeMillis by remember { mutableLongStateOf(0L) }
    val videoParts = remember { mutableStateListOf<String>() }
    var isProcessing by remember { mutableStateOf(false) }
    var isFlippingUI by remember { mutableStateOf(false) }
    var initialOrientation by remember { mutableIntStateOf(0) }

    // Find a common quality supported by both cameras to ensure seamless merging
    val commonQuality = remember {
        val backId = getBackCameraId()
        val frontId = getFrontCameraId()
        when {
            CamcorderProfile.hasProfile(backId, CamcorderProfile.QUALITY_720P) &&
                    CamcorderProfile.hasProfile(frontId, CamcorderProfile.QUALITY_720P) -> CamcorderProfile.QUALITY_720P
            else -> CamcorderProfile.QUALITY_480P
        }
    }

    val scope = rememberCoroutineScope()
    val cameraState = remember { mutableStateOf<Camera?>(null) }
    val mediaActionSound = remember {
        MediaActionSound().apply {
            load(MediaActionSound.START_VIDEO_RECORDING)
            load(MediaActionSound.STOP_VIDEO_RECORDING)
        }
    }
    val mediaRecorderState = remember { mutableStateOf<MediaRecorder?>(null) }
    val surfaceHolderState = remember { mutableStateOf<SurfaceHolder?>(null) }
    val countDownTimerState = remember { mutableStateOf<CountDownTimer?>(null) }
    val startTimeState = remember { mutableLongStateOf(0L) }

    fun stopTimer() {
        countDownTimerState.value?.cancel()
        timerText = "00:00"
        accumulatedTimeMillis = 0L
    }

    suspend fun mergeVideoFiles(parts: List<String>, finalOrientation: Int) {
        if (parts.isEmpty()) return
        withContext(Dispatchers.IO) {
            val downloadDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Sentinel Video")
            if (!downloadDir.exists()) downloadDir.mkdirs()
            val outputFile = File(downloadDir, "REC_${System.currentTimeMillis()}.mp4")

            var muxer: MediaMuxer? = null
            try {
                muxer = MediaMuxer(outputFile.path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
                muxer.setOrientationHint(finalOrientation)

                val extractors = parts.map { path -> MediaExtractor().apply { setDataSource(path) } }
                var videoTrackIndexMuxer = -1
                var audioTrackIndexMuxer = -1

                // Map track indices for each extractor
                val trackMappings = extractors.map { extractor ->
                    var vPart = -1
                    var aPart = -1
                    for (i in 0 until extractor.trackCount) {
                        val format = extractor.getTrackFormat(i)
                        val mime = format.getString(MediaFormat.KEY_MIME)
                        if (mime?.startsWith("video/") == true) {
                            vPart = i
                            if (videoTrackIndexMuxer == -1) videoTrackIndexMuxer = muxer.addTrack(format)
                        } else if (mime?.startsWith("audio/") == true) {
                            aPart = i
                            if (audioTrackIndexMuxer == -1) audioTrackIndexMuxer = muxer.addTrack(format)
                        }
                    }
                    Pair(vPart, aPart)
                }

                muxer.start()
                val buffer = ByteBuffer.allocate(2 * 1024 * 1024)
                val bufferInfo = MediaCodec.BufferInfo()
                var videoOffsetUs = 0L
                var audioOffsetUs = 0L

                extractors.forEachIndexed { index, extractor ->
                    val (partVideoTrack, partAudioTrack) = trackMappings[index]
                    var maxVideoUs = 0L
                    var maxAudioUs = 0L

                    if (partVideoTrack != -1 && videoTrackIndexMuxer != -1) {
                        extractor.selectTrack(partVideoTrack)
                        while (true) {
                            val size = extractor.readSampleData(buffer, 0)
                            if (size < 0) break
                            @Suppress("WrongConstant")
                            bufferInfo.set(0, size, extractor.sampleTime + videoOffsetUs, extractor.sampleFlags)
                            muxer.writeSampleData(videoTrackIndexMuxer, buffer, bufferInfo)
                            maxVideoUs = maxOf(maxVideoUs, extractor.sampleTime)
                            extractor.advance()
                        }
                        extractor.unselectTrack(partVideoTrack)
                    }

                    if (partAudioTrack != -1 && audioTrackIndexMuxer != -1) {
                        extractor.selectTrack(partAudioTrack)
                        while (true) {
                            val size = extractor.readSampleData(buffer, 0)
                            if (size < 0) break
                            @Suppress("WrongConstant")
                            bufferInfo.set(0, size, extractor.sampleTime + audioOffsetUs, extractor.sampleFlags)
                            muxer.writeSampleData(audioTrackIndexMuxer, buffer, bufferInfo)
                            maxAudioUs = maxOf(maxAudioUs, extractor.sampleTime)
                            extractor.advance()
                        }
                        extractor.unselectTrack(partAudioTrack)
                    }
                    videoOffsetUs += maxVideoUs + 1000 // 1ms gap to avoid overlap
                    audioOffsetUs += maxAudioUs + 1000
                    extractor.release()
                }
                try { muxer.stop() } catch (_: Exception) {}
                withContext(Dispatchers.Main) { Toast.makeText(context, "Video saved to Downloads", Toast.LENGTH_SHORT).show() }
            } catch (e: Exception) {
                Log.e("VideoRecording", "Merge failed: ${e.message}")
            } finally {
                try { muxer?.release() } catch (_: Exception) {}
                parts.forEach { try { File(it).delete() } catch (_: Exception) {} }
            }
        }
    }

    fun stopRecording(isFlipping: Boolean = false) {
        val mediaRecorder = mediaRecorderState.value
        if (mediaRecorder != null) {
            try { mediaRecorder.stop() } catch (_: Exception) {}
            mediaRecorder.release()
            mediaRecorderState.value = null
            try { cameraState.value?.lock() } catch (_: Exception) {}

            if (!isFlipping) {
                isRecording = false
                countDownTimerState.value?.cancel()
                mediaActionSound.play(MediaActionSound.STOP_VIDEO_RECORDING)
                val partsToMerge = videoParts.toList()
                val finalOrient = initialOrientation
                isProcessing = true
                scope.launch {
                    if (partsToMerge.size > 1) {
                        mergeVideoFiles(partsToMerge, finalOrient)
                    } else if (partsToMerge.size == 1) {
                        val downloadDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Sentinel Video")
                        if (!downloadDir.exists()) downloadDir.mkdirs()
                        val sourceFile = File(partsToMerge[0])
                        val destFile = File(downloadDir, "REC_${System.currentTimeMillis()}.mp4")
                        try {
                            withContext(Dispatchers.IO) {
                                sourceFile.inputStream().use { input -> destFile.outputStream().use { output -> input.copyTo(output) } }
                                sourceFile.delete()
                            }
                            Toast.makeText(context, "Video saved to Downloads", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Log.e("VideoRecording", "Save failed: ${e.message}")
                        }
                    }
                    videoParts.clear()
                    isProcessing = false
                    stopTimer()
                }
            } else {
                countDownTimerState.value?.cancel()
            }
        }
    }

    fun startRecording(isFlipping: Boolean = false) {
        val camera = cameraState.value ?: return
        val holder = surfaceHolderState.value ?: return
        if (isRecording && !isFlipping) return

        try {
            val file = File(context.cacheDir, "PART_${System.currentTimeMillis()}.mp4")
            videoParts.add(file.path)
            val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else MediaRecorder()

            camera.unlock()
            mediaRecorder.setCamera(camera)
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA)

            @Suppress("DEPRECATION")
            mediaRecorder.setProfile(CamcorderProfile.get(currentCameraId, commonQuality))
            mediaRecorder.setOutputFile(file.path)
            mediaRecorder.setPreviewDisplay(holder.surface)

            val info = Camera.CameraInfo()
            Camera.getCameraInfo(currentCameraId, info)
            mediaRecorder.setOrientationHint(info.orientation)
            if (videoParts.size == 1) initialOrientation = info.orientation

            mediaRecorder.prepare()
            mediaRecorder.start()
            mediaRecorderState.value = mediaRecorder
            isRecording = true
            if (!isFlipping) mediaActionSound.play(MediaActionSound.START_VIDEO_RECORDING)
            startTimeState.longValue = System.currentTimeMillis()

            val remainingMillis = 15000 - accumulatedTimeMillis
            countDownTimerState.value = object : CountDownTimer(remainingMillis, 1000) {
                override fun onTick(ms: Long) {
                    val totalSec = ((System.currentTimeMillis() - startTimeState.longValue + accumulatedTimeMillis) / 1000).toInt()
                    if (totalSec >= 15) stopRecording()
                    else timerText = String.format(Locale.US, "%02d:%02d", totalSec / 60, totalSec % 60)
                }
                override fun onFinish() { stopRecording() }
            }.start()
        } catch (e: Exception) {
            Log.e("VideoRecording", "Start error: ${e.message}")
            try { camera.lock() } catch (_: Exception) {}
        }
    }

    fun flipCamera() {
        if (isProcessing || isFlippingUI) return
        isFlippingUI = true
        if (isRecording) {
            shouldResumeRecordingAfterFlip = true
            accumulatedTimeMillis += System.currentTimeMillis() - startTimeState.longValue
            stopRecording(isFlipping = true)
        }
        cameraState.value?.let { it.stopPreview(); it.release() }
        cameraState.value = null
        currentCameraId = if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) getFrontCameraId() else getBackCameraId()
    }

    LaunchedEffect(cameraState.value, surfaceHolderState.value) {
        if (cameraState.value != null && surfaceHolderState.value != null) {
            if (!hasStartedAutomatically) { startRecording(); hasStartedAutomatically = true }
            else if (shouldResumeRecordingAfterFlip) { startRecording(isFlipping = true); shouldResumeRecordingAfterFlip = false }
        }
    }

    DisposableEffect(Unit) { onDispose { stopRecording(); cameraState.value?.release(); countDownTimerState.value?.cancel() } }

    Scaffold(containerColor = Color.Black) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedContent(targetState = currentCameraId, transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) }, label = "Flip") { targetId ->
                AndroidView(factory = { ctx ->
                    var activeCamera: Camera? = null
                    SurfaceView(ctx).apply {
                        holder.addCallback(object : SurfaceHolder.Callback {
                            override fun surfaceCreated(h: SurfaceHolder) {
                                try {
                                    val cam = Camera.open(targetId)
                                    activeCamera = cam
                                    cam.setDisplayOrientation(90)
                                    cam.setPreviewDisplay(h)
                                    cam.startPreview()
                                    cameraState.value = cam
                                    surfaceHolderState.value = h
                                    isFlippingUI = false
                                } catch (_: Exception) { isFlippingUI = false }
                            }
                            override fun surfaceChanged(h: SurfaceHolder, f: Int, w: Int, hi: Int) {}
                            override fun surfaceDestroyed(h: SurfaceHolder) {
                                activeCamera?.let {
                                    try { it.stopPreview() } catch (_: Exception) {}
                                    try { it.release() } catch (_: Exception) {}
                                    if (cameraState.value == it) cameraState.value = null
                                }
                                activeCamera = null
                            }
                        })
                    }
                }, modifier = Modifier.fillMaxSize())
            }

            AnimatedVisibility(visible = isFlippingUI, enter = fadeIn(), exit = fadeOut()) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.4f)).blur(dimensionResource(id = R.dimen.section_spacing)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            // Top Bar
            Row(modifier = Modifier.fillMaxWidth().padding(top = dimensionResource(id = R.dimen.icon_size_medium), start = dimensionResource(id = R.dimen.screen_padding_small), end = dimensionResource(id = R.dimen.screen_padding_small)), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onBack, modifier = Modifier.background(Color.Black.copy(0.3f), CircleShape)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(Color.Black.copy(0.3f), RoundedCornerShape(dimensionResource(id = R.dimen.text_title))).padding(horizontal = dimensionResource(id = R.dimen.element_spacing), vertical = dimensionResource(id = R.dimen.padding_micro))) {
                    if (isRecording) Box(modifier = Modifier.size(dimensionResource(id = R.dimen.spacer_small)).clip(CircleShape).background(Color.Red))
                    Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.spacer_small)))
                    Text(text = timerText, color = Color.White, fontSize = with(density) { dimensionResource(id = R.dimen.text_subtitle).toSp() }, fontWeight = FontWeight.Bold)
                }
//                Box(modifier = Modifier.size(dimensionResource(id = R.dimen.icon_size_standard)))
                IconButton(onClick = {flipCamera()} , enabled = !isProcessing, modifier = Modifier.background(Color.Black.copy(0.3f), CircleShape)) { Icon(Icons.Default.FlipCameraAndroid, null, tint = Color.White) }

            }

            Column (
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = dimensionResource(id = R.dimen.status_bar_height))
                    .padding(horizontal = dimensionResource(id = R.dimen.icon_size_medium)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
//                IconButton(onClick = { flipCamera() }, enabled = !isProcessing, modifier = Modifier.size(dimensionResource(id = R.dimen.option_icon_container)).background(Color.Black.copy(0.3f), CircleShape)) { Icon(Icons.Default.FlipCameraAndroid, null, tint = Color.White) }
                Box(modifier = Modifier.size(dimensionResource(id = R.dimen.bottom_nav_height)).border(dimensionResource(id = R.dimen.spacer_micro), Color.White, CircleShape).padding(dimensionResource(id = R.dimen.spacer_small)).clip(CircleShape).background(if (isRecording) Color.White else Color.Red).clickable(enabled = !isProcessing) { if (isRecording) stopRecording() else startRecording() }, contentAlignment = Alignment.Center) {
                    if (isRecording) Box(modifier = Modifier.size(dimensionResource(id = R.dimen.radius_large)).background(Color.Red, RoundedCornerShape(dimensionResource(id = R.dimen.spacer_micro))))
                }
                /*IconButton(onClick = {
                    val camera = cameraState.value
                    if (camera != null) {
                        try {
                            val params = camera.parameters
                            val flashModes = params.supportedFlashModes
                            if (flashModes != null && flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                                if (isFlashOn) {
                                    params.flashMode = Camera.Parameters.FLASH_MODE_OFF
                                    isFlashOn = false
                                } else {
                                    params.flashMode = Camera.Parameters.FLASH_MODE_TORCH
                                    isFlashOn = true
                                }
                                camera.parameters = params
                            } else {
                                Toast.makeText(context, "Flash not supported", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Log.e("VideoRecording", "Flash error: ${e.message}")
                        }
                    }
                }, modifier = Modifier.size(dimensionResource(id = R.dimen.option_icon_container)).background(Color.Black.copy(0.3f), CircleShape)) {
                    Icon(if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff, null, tint = if (isFlashOn) Color.Yellow else Color.White)
                }*/
            }
        }
    }
    if (isProcessing) Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.5f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) { CircularProgressIndicator(color = Color.White); Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.card_padding))); Text("Processing video...", color = Color.White, fontSize = with(density) { dimensionResource(id = R.dimen.text_body).toSp() }) }
    }
}

private fun getFrontCameraId(): Int = (0 until Camera.getNumberOfCameras()).firstOrNull { i -> Camera.CameraInfo().also { Camera.getCameraInfo(i, it) }.facing == Camera.CameraInfo.CAMERA_FACING_FRONT } ?: 0
private fun getBackCameraId(): Int = (0 until Camera.getNumberOfCameras()).firstOrNull { i -> Camera.CameraInfo().also { Camera.getCameraInfo(i, it) }.facing == Camera.CameraInfo.CAMERA_FACING_BACK } ?: 0

@Preview(showBackground = true)
@Composable
fun RecordVideoScreenPreview() {
    NannyEyeWiFiCameraMonitorTheme {
        RecordVideoScreen(onBack = {})
    }
}
