package com.example.stargazer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var rotationVectorSensor: Sensor? = null
    private lateinit var cameraExecutor: ExecutorService

    // State variables to trigger UI recomposition when the phone moves
    private var azimuth by mutableFloatStateOf(0f)
    private var pitch by mutableFloatStateOf(0f)

    // Handle the permission request dialog
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false
        if (cameraGranted) {
            setContent { StargazerApp(azimuth, pitch) }
        } else {
            // In a production app, handle the case where the user denies the camera
            Log.e("Permissions", "Camera permission denied.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        val permissionsToRequest = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        // Check if we already have permissions, otherwise request them
        if (allPermissionsGranted(permissionsToRequest)) {
            setContent { StargazerApp(azimuth, pitch) }
        } else {
            requestPermissionLauncher.launch(permissionsToRequest)
        }
    }

    private fun allPermissionsGranted(permissions: Array<String>) = permissions.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onResume() {
        super.onResume()
        // Start listening to the hardware sensors when the app is on screen
        rotationVectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        // Stop listening to sensors to save battery when the app is in the background
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)

            // Calculate Azimuth (Compass heading: 0-360)
            var newAzimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
            if (newAzimuth < 0) newAzimuth += 360f
            
            // Calculate Pitch (Tilt: -90 looking down, 90 looking up). 
            // We invert it so 0 is horizon and positive is up into the sky.
            val newPitch = -Math.toDegrees(orientation[1].toDouble()).toFloat()

            azimuth = newAzimuth
            pitch = newPitch
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for this MVP, but required by the interface
    }
}

@Composable
fun StargazerApp(azimuth: Float, pitch: Float) {
    Box(modifier = Modifier.fillMaxSize()) {
        CameraView()
        StarOverlay(azimuth, pitch)
    }
}

@Composable
fun CameraView() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, 
                        CameraSelector.DEFAULT_BACK_CAMERA, 
                        preview
                    )
                } catch (e: Exception) {
                    Log.e("CameraView", "Camera use case binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))
            
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun StarOverlay(phoneAzimuth: Float, phonePitch: Float) {
    val stars = remember { StarData.getVisibleStars() }

    // This value scales the degree differences into physical screen pixels.
    // 40f is a rough estimate for a standard mobile camera field of view.
    val pixelsPerDegree = 40f

    // Create Paint objects once per composition rather than on every draw call.
    val starLabelPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 45f
            isAntiAlias = true
            setShadowLayer(8f, 0f, 0f, android.graphics.Color.BLACK)
        }
    }
    val hudPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.GREEN
            textSize = 45f
            setShadowLayer(5f, 0f, 0f, android.graphics.Color.BLACK)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Draw a targeting crosshair in the center of the screen
        val centerX = size.width / 2
        val centerY = size.height / 2
        drawLine(Color.Red, Offset(centerX - 30f, centerY), Offset(centerX + 30f, centerY), strokeWidth = 3f)
        drawLine(Color.Red, Offset(centerX, centerY - 30f), Offset(centerX, centerY + 30f), strokeWidth = 3f)

        stars.forEach { star ->
            val (screenX, screenY) = AstroMath.calculateScreenPosition(
                star, phoneAzimuth, phonePitch, pixelsPerDegree, size.width, size.height
            )

            // Only render the star and label if it is close enough to the visible area.
            if (AstroMath.isWithinRenderBounds(screenX, screenY, size.width, size.height)) {
                drawCircle(color = Color.White, radius = 12f, center = Offset(screenX, screenY))
                drawContext.canvas.nativeCanvas.drawText(
                    star.name,
                    screenX + 20f,
                    screenY + 15f,
                    starLabelPaint
                )
            }
        }

        // Heads-Up Display (HUD): show raw sensor values for debugging.
        drawContext.canvas.nativeCanvas.drawText(
            "Heading: ${phoneAzimuth.toInt()}° | Tilt: ${phonePitch.toInt()}°",
            50f, 120f,
            hudPaint
        )
    }
}
