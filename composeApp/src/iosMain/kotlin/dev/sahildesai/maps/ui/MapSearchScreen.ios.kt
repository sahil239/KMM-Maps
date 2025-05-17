package dev.sahildesai.maps.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import dev.sahildesai.maps.models.LatLng
import dev.sahildesai.maps.models.MarkerData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import platform.CoreGraphics.CGRect
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMake
import platform.MapKit.MKCoordinateSpanMake
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKOverlayProtocol
import platform.MapKit.MKOverlayRenderer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGRectMake
import platform.MapKit.MKPointAnnotation
import platform.MapKit.MKPolyline
import platform.MapKit.MKPolylineRenderer
import platform.MapKit.MKUserTrackingModeNone
import platform.MapKit.addOverlay
import platform.MapKit.overlays
import platform.MapKit.removeOverlay
import platform.MapKit.removeOverlays
import platform.UIKit.UIColor
import platform.UIKit.UIScreen
import platform.UIKit.systemBlueColor
import platform.darwin.NSObject

/**
 * Subclassed MKMapView that holds a strong reference to its delegate,
 * preventing it from being deallocated prematurely.
 */
@OptIn(ExperimentalForeignApi::class)
class RetainedMapView(x: Double, y: Double, width: Double, height: Double) :
    MKMapView(CGRectMake(x, y, width, height)) {
    var strongDelegate: MKMapViewDelegateProtocol? = null
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun MapView(
    zoom: Float,
    markers: List<MarkerData>,
    polyline: List<LatLng>,
    modifier: Modifier
) {
    UIKitView(
        modifier = modifier,
        factory = {
            // 1) Create the map view and wire its delegate
            val bounds = UIScreen.mainScreen.bounds
            RetainedMapView(
                0.0, 0.0,
                bounds.useContents { size.width },
                bounds.useContents { size.height }
            ).apply {
                showsUserLocation = false
                userTrackingMode = MKUserTrackingModeNone

                // Delegate that provides the renderer for polylines
                val delegateImpl = object : NSObject(), MKMapViewDelegateProtocol {
                    override fun mapView(
                        mapView: MKMapView,
                        rendererForOverlay: MKOverlayProtocol
                    ): MKOverlayRenderer {
                        return if (rendererForOverlay is MKPolyline) {
                            MKPolylineRenderer(rendererForOverlay).apply {
                                strokeColor = UIColor.systemBlueColor
                                lineWidth = 4.0
                            }
                        } else {
                            MKOverlayRenderer(rendererForOverlay)
                        }
                    }
                }
                strongDelegate = delegateImpl    // keep alive
                delegate = delegateImpl
            }
        },
        update = { raw ->
            // 2) Cast back to our subclass
            val mapView = raw as RetainedMapView

            // 3) Clear old annotations & overlays
            mapView.removeAnnotations(mapView.annotations)
            mapView.removeOverlays(mapView.overlays)
            // 4) Add point markers
            markers.forEach { md ->
                val ann = MKPointAnnotation().apply {
                    setTitle(md.title)
                    setCoordinate(
                        CLLocationCoordinate2DMake(
                            md.position.latitude,
                            md.position.longitude
                        )
                    )
                }
                mapView.addAnnotation(ann)
            }

            // 5) Add polyline overlay
            if (polyline.size >= 2) {
                memScoped {
                    val count = polyline.size
                    val coords = allocArray<CLLocationCoordinate2D>(count)
                    for (i in 0 until count) {
                        val ll = polyline[i]
                        coords[i].latitude = ll.latitude
                        coords[i].longitude = ll.longitude
                    }
                    val mkPoly = MKPolyline.polylineWithCoordinates(
                        coords,
                        count.toULong()
                    )
                    mapView.addOverlay(mkPoly)
                }
            }

            // 6) Fit region to all markers + polyline
            val allPoints = markers.map { it.position } + polyline
            if (allPoints.isNotEmpty()) {
                val lats = allPoints.map { it.latitude }
                val lons = allPoints.map { it.longitude }
                val maxLat = lats.maxOrNull() ?: 0.0
                val minLat = lats.minOrNull() ?: 0.0
                val maxLon = lons.maxOrNull() ?: 0.0
                val minLon = lons.minOrNull() ?: 0.0

                val center = CLLocationCoordinate2DMake(
                    (maxLat + minLat) / 2.0,
                    (maxLon + minLon) / 2.0
                )
                val span = MKCoordinateSpanMake(
                    ((maxLat - minLat).coerceAtLeast(0.01)) * 1.2,
                    ((maxLon - minLon).coerceAtLeast(0.01)) * 1.2
                )
                mapView.setRegion(
                    MKCoordinateRegionMake(center, span),
                    animated = true
                )
            }
        }
    )
}


@OptIn(ExperimentalForeignApi::class)
fun LatLng.toCL() = CLLocationCoordinate2DMake(latitude, longitude)

/** Convert your shared MarkerData into an MKPointAnnotation for MapKit. */
@OptIn(ExperimentalForeignApi::class)
fun MarkerData.toMKAnnotation(): MKPointAnnotation =
    MKPointAnnotation().apply {
        setCoordinate(position.toCL())
        setTitle(this@toMKAnnotation.title)
    }