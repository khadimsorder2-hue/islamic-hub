package com.islamichub.app.ui.screens.qibla

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.islamichub.app.R
import com.islamichub.app.data.AppContainer
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun QiblaScreen(container: AppContainer) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        hasPermission = grants.values.any { it }
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.qibla_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (!hasPermission) {
            Text(
                text = stringResource(R.string.qibla_need_location),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }) {
                Text(stringResource(R.string.prayer_use_my_location))
            }
        } else {
            // Get device location (one-shot)
            var location by remember { mutableStateOf<Location?>(null) }
            LaunchedEffect(hasPermission) {
                if (hasPermission) {
                    location = container.prayerRepository.getCurrentLocation()
                }
            }

            val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
            val rotationSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }
            var azimuth by remember { mutableStateOf(0f) }

            DisposableEffect(rotationSensor) {
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        val r = FloatArray(9)
                        val i = FloatArray(9)
                        SensorManager.getRotationMatrixFromVector(r, event.values)
                        SensorManager.remapCoordinateSystem(
                            r, SensorManager.AXIS_X, SensorManager.AXIS_Z, i
                        )
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(i, orientation)
                        azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat().mod(360f)
                    }
                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }
                if (rotationSensor != null) {
                    sensorManager.registerListener(
                        listener, rotationSensor, SensorManager.SENSOR_DELAY_UI
                    )
                }
                onDispose {
                    sensorManager.unregisterListener(listener)
                }
            }

            // Qibla bearing from current location to Kaaba (21.4225, 39.8262)
            val qiblaBearing = remember(location) {
                val lat = location?.latitude ?: 21.4225
                val lng = location?.longitude ?: 39.8262
                computeQiblaBearing(lat, lng)
            }

            Text(
                text = String.format(stringResource(R.string.qibla_angle), qiblaBearing.toInt()),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            QiblaCompass(
                azimuth = azimuth,
                qiblaBearing = qiblaBearing,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )

            val diff = ((qiblaBearing - azimuth + 540f).mod(360f) - 180f)
            val aligned = kotlin.math.abs(diff) < 5f
            Text(
                text = if (aligned) stringResource(R.string.qibla_aligned)
                       else stringResource(R.string.qibla_instruction),
                style = MaterialTheme.typography.bodyMedium,
                color = if (aligned) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun computeQiblaBearing(lat: Double, lng: Double): Float {
    val kaabaLat = Math.toRadians(21.4225)
    val kaabaLng = Math.toRadians(39.8262)
    val userLat = Math.toRadians(lat)
    val userLng = Math.toRadians(lng)
    val dLng = kaabaLng - userLng
    val y = sin(dLng)
    val x = cos(userLat) * sin(kaabaLat) - sin(userLat) * cos(kaabaLat) * cos(dLng)
    val bearing = Math.toDegrees(atan2(y, x))
    return ((bearing + 360).mod(360.0)).toFloat()
}

@Composable
private fun QiblaCompass(
    azimuth: Float,
    qiblaBearing: Float,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val secondary = MaterialTheme.colorScheme.secondary
    Canvas(modifier = modifier) {
        val canvasSize = size.minDimension
        val center = Offset(size.width / 2, size.height / 2)
        val radius = canvasSize / 2 * 0.85f

        // Outer circle
        drawCircle(
            color = onSurfaceVariant.copy(alpha = 0.4f),
            radius = radius,
            center = center,
            style = Stroke(width = 4f)
        )

        // Cardinal direction marks (rotate by -azimuth so N points to true north)
        rotate(degrees = -azimuth, pivot = center) {
            // North pointer
            drawLine(
                color = Color.Red,
                start = center,
                end = Offset(center.x, center.y - radius),
                strokeWidth = 6f
            )
            // N label
            for (angle in 0 until 360 step 90) {
                val rad = Math.toRadians(angle.toDouble())
                val x = center.x + (radius * cos(rad)).toFloat()
                val y = center.y + (radius * sin(rad)).toFloat()
                drawCircle(
                    color = onSurfaceVariant,
                    radius = 6f,
                    center = Offset(x, y)
                )
            }
        }

        // Qibla pointer (rotates by qiblaBearing - azimuth)
        rotate(degrees = qiblaBearing - azimuth, pivot = center) {
            // Kaaba marker (gold/green arrow)
            drawLine(
                color = primary,
                start = center,
                end = Offset(center.x, center.y - radius * 0.95f),
                strokeWidth = 10f
            )
            // Arrow head
            val tip = Offset(center.x, center.y - radius * 0.95f)
            drawCircle(
                color = secondary,
                radius = 16f,
                center = tip
            )
        }

        // Center hub
        drawCircle(
            color = primary,
            radius = 14f,
            center = center
        )
    }
}
