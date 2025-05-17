package dev.sahildesai.maps.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import dev.sahildesai.maps.models.LatLng
import dev.sahildesai.maps.models.MarkerData
import dev.sahildesai.maps.models.MarkerType

@Composable
actual fun MapView(
    zoom: Float,
    markers: List<MarkerData>,
    polyline: List<LatLng>,
    modifier: Modifier
) {
    val cameraState = rememberCameraPositionState()

    LaunchedEffect(polyline, markers, zoom) {
        val bounds = LatLngBounds.builder()
        if (markers.isEmpty()) {
            bounds.apply {
                include(com.google.android.gms.maps.model.LatLng(0.0, 0.0))
            }
        } else {
            polyline.forEach {
                bounds.apply { include(it.toGms()) }
            }
            markers.forEach {
                bounds.apply {
                    include(it.toGmsMarker().position)
                }
            }
        }
        cameraState.animate(
            update = CameraUpdateFactory.newLatLngBounds(bounds.build(), 50)
        )
    }

    GoogleMap(
        cameraPositionState = cameraState,
        modifier = modifier
    ) {
        markers.forEach { marker ->
            val bitmapDescriptor = BitmapDescriptorFactory.defaultMarker(
                when (marker.markerType) {
                    MarkerType.PICK_UP -> BitmapDescriptorFactory.HUE_GREEN
                    MarkerType.DROP_OFF -> BitmapDescriptorFactory.HUE_BLUE
                    MarkerType.INTERMEDIATE -> BitmapDescriptorFactory.HUE_RED
                }
            )
            Marker(
                icon = bitmapDescriptor,
                snippet = marker.title,
                state = MarkerState(marker.toGmsMarker().position)
            )
        }


        if (polyline.isNotEmpty()) {
            Polyline(
                points = polyline.map { it.toGms() },
                color = Color.Blue,
                width = 5f
            )

        }
    }
}


fun LatLng.toGms() = com.google.android.gms.maps.model.LatLng(latitude, longitude)

fun MarkerData.toGmsMarker() = MarkerOptions()
    .position(position.toGms())
    .title(title)