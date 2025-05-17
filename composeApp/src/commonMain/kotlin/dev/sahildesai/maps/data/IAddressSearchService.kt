package dev.sahildesai.maps.data

import dev.sahildesai.maps.models.Address
import dev.sahildesai.maps.models.AutocompletePrediction
import dev.sahildesai.maps.models.LatLng
import dev.sahildesai.maps.models.Route

interface IAddressSearchService {
        /**
         * Searches for addresses matching the given query string.
         * @param query the free‑form address or place name to geocode.
         * @return a list of Address results, or an empty list if none found.
         */
        suspend fun searchPlace(query: String): Address

        suspend fun searchAutoComplete(query: String): List<AutocompletePrediction>

        suspend fun computeRoute(
                pickup: LatLng,
                dropoff: LatLng
        ): Route?

}