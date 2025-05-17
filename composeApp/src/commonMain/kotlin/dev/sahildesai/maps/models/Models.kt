package dev.sahildesai.maps.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class MarkerType{
    PICK_UP,
    DROP_OFF,
    INTERMEDIATE
}
/** A single autocomplete suggestion. */
data class AutocompletePrediction(
    val description: String,
    val placeId: String
)

/**
 * A simple platform‑agnostic lat/lng pair.
 */
@Serializable
data class LatLng(
    val latitude: Double,
    val longitude: Double
)

/**
 * Represents a pin on the map.
 */
data class MarkerData(
    val position: LatLng,
    val title: String,
    val markerType: MarkerType

)

data class Address(
    /** The human‑readable address returned by the geocoding service */
    val formattedAddress: String,
    /** Name of the address */
    val name: String,
    /** Latitude of the address */
    val lat: Double,
    /** Longitude of the address */
    val lng: Double
)

@Serializable
data class AutocompleteResponse(
    val predictions: List<PredictionDto>
)

@Serializable
data class PredictionDto(
    val description: String,
    @SerialName("place_id") val placeId: String
)

@Serializable
data class GeocodeResponse(
    @SerialName("result") val results: GeoResult
)

@Serializable
data class GeoResult(
    @SerialName("formatted_address") val formattedAddress: String,
    @SerialName("name") val name: String,
    @SerialName("geometry") val geometry: Geometry
)

@Serializable
data class Geometry(
    @SerialName("location") val location: Location
)

@Serializable
data class Location(
    @SerialName("lat") val lat: Double,
    @SerialName("lng") val lng: Double
)

@Serializable
data class ComputeRoutesRequest(
    val origin: Waypoint,
    val destination: Waypoint,
    val travelMode: String,
    val routingPreference: String = "UNSPECIFIED",
    val units: String = "METRIC"
)

@Serializable
data class Waypoint(val location: LocationLatLng)

@Serializable
data class LocationLatLng(
    @SerialName("latLng") val latLng: LatLng
)


@Serializable
data class ComputeRoutesResponse(
    val routes: List<Route>
)

@Serializable
data class Route(
    val distanceMeters: Int,
    val duration: String,
    val polyline: Polyline
)

@Serializable
data class Duration(
    val seconds: Long,
    val trafficDelaySeconds: Long? = null
)

@Serializable
data class Polyline(
    val encodedPolyline: String
)

fun decodePolyline(encoded: String): List<LatLng> {
    val poly = ArrayList<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        val deltaLat = if (result and 1 != 0) (result shr 1).inv() else (result shr 1)
        lat += deltaLat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or ((b and 0x1f) shl shift)
            shift += 5
        } while (b >= 0x20)
        val deltaLng = if (result and 1 != 0) (result shr 1).inv() else (result shr 1)
        lng += deltaLng

        // Degrees are stored as 1e-5, so convert back
        poly.add(LatLng(lat / 1e5, lng / 1e5))
    }

    return poly
}