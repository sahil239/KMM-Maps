package dev.sahildesai.maps.data

import dev.sahildesai.maps.BuildKonfig
import dev.sahildesai.maps.models.Address
import dev.sahildesai.maps.models.AutocompletePrediction
import dev.sahildesai.maps.models.AutocompleteResponse
import dev.sahildesai.maps.models.ComputeRoutesRequest
import dev.sahildesai.maps.models.ComputeRoutesResponse
import dev.sahildesai.maps.models.GeocodeResponse
import dev.sahildesai.maps.models.LatLng
import dev.sahildesai.maps.models.LocationLatLng
import dev.sahildesai.maps.models.Route
import dev.sahildesai.maps.models.Waypoint
import dev.sahildesai.maps.models.toAddress
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class AddressSearchService(
    private val client: HttpClient,
): IAddressSearchService {

    companion object {
        private const val ADDRESS_BASE_URL = "https://maps.googleapis.com/maps/api/"
        private const val ROUTES_API = "https://routes.googleapis.com/directions/v2:computeRoutes"
        private const val DETAILS = "place/details/"
        private const val PLACE = "place/autocomplete/"
        private const val OUTPUT = "json"
        private const val FIELDS = "routes.distanceMeters,routes.duration,routes.polyline.encodedPolyline"
    }


    override suspend fun searchPlace(query: String): Address {
        val response: GeocodeResponse = client.get("${ADDRESS_BASE_URL}${DETAILS}${OUTPUT}") {
            parameter("placeid", query)
            parameter("fields", "formatted_address,name,geometry")
            parameter("key", BuildKonfig.ADDRESS_SEARCH_API_KEY)
        }.body()
        return response.results.toAddress()
    }

    override suspend fun searchAutoComplete(query: String): List<AutocompletePrediction> {
        val response: AutocompleteResponse = client
            .get("${ADDRESS_BASE_URL}${PLACE}${OUTPUT}") {
                parameter("input", query)
                parameter("key", BuildKonfig.ADDRESS_SEARCH_API_KEY)
            }
            .body()
        return response.predictions.map {
            AutocompletePrediction(it.description, it.placeId)
        }
    }

    override suspend fun computeRoute(pickup: LatLng, dropoff: LatLng): Route? {
        val requestBody = ComputeRoutesRequest(
            origin      = Waypoint(LocationLatLng(pickup)),
            destination = Waypoint(LocationLatLng(dropoff)),
            travelMode  = "DRIVE",
            routingPreference = "TRAFFIC_AWARE"
        )

        val response: HttpResponse = client.post(ROUTES_API) {
            contentType(ContentType.Application.Json)
            header("X-Goog-Api-Key", BuildKonfig.ADDRESS_SEARCH_API_KEY)
            header("X-Goog-FieldMask", FIELDS)
            setBody(requestBody)
        }

        if (!response.status.isSuccess()) {
            // handle error or throw
            return null
        }

        val routesResponse: ComputeRoutesResponse = response.body()
        return routesResponse.routes.firstOrNull()
    }
}