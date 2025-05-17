package dev.sahildesai.maps.ui

import androidx.compose.material3.SnackbarHostState
import com.rickclephas.kmm.viewmodel.KMMViewModel
import com.rickclephas.kmm.viewmodel.coroutineScope
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.sahildesai.maps.data.IAddressSearchService
import dev.sahildesai.maps.domain.IPermissionUseCase
import dev.sahildesai.maps.models.Address
import dev.sahildesai.maps.models.AutocompletePrediction
import dev.sahildesai.maps.models.LatLng
import dev.sahildesai.maps.models.Route
import dev.sahildesai.maps.models.decodePolyline
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class MapSearchViewModel : KMMViewModel(), KoinComponent {

    private val geoService by inject<IAddressSearchService>()
    private val permissionUseCase by inject<IPermissionUseCase>()

    // 1) Text the user typed
    private val _pickUpQuery = MutableStateFlow("")
    var pickUpQuery = _pickUpQuery.asStateFlow()

    private val _dropOffQuery = MutableStateFlow("")
    var dropOffQuery = _dropOffQuery.asStateFlow()

    private val _pickUpResultsSuggestions = MutableStateFlow(emptyList<AutocompletePrediction>())
    val pickUpResultsSuggestions = _pickUpResultsSuggestions.asStateFlow()

    private val _dropOffResultsSuggestions = MutableStateFlow(emptyList<AutocompletePrediction>())
    val dropOffResultsSuggestions = _dropOffResultsSuggestions.asStateFlow()

    // 3) Where the map camera should point
    private val _pickUpAddress = MutableStateFlow<Address?>(null)
    val pickUpAddress = _pickUpAddress.asStateFlow()

    private val _dropOffAddress = MutableStateFlow<Address?>(null)
    val dropOffAddress = _dropOffAddress.asStateFlow()

    // 4) Map zoom level
    private val _zoom = MutableStateFlow(1f)
    val zoom = _zoom.asStateFlow()

    private val _routeInfo = MutableStateFlow<Route?>(null)
    val routeInfo = _routeInfo.asStateFlow()

    // “Skip” flags for each field
    private val _skipPickUpAuto = MutableStateFlow(false)
    private val _skipDropOffAuto = MutableStateFlow(false)

    init {
        // 1) Debounce pickup‐query and call autocomplete
        viewModelScope.coroutineScope.launch {
            pickUpQuery
                .debounce(1000)                     // wait 300 ms of “no typing”
                .filter { it.length >= 2 }         // optional: only search after 2+ chars
                .distinctUntilChanged()
                .filter { !_skipPickUpAuto.value }       // skip when true
                .flatMapLatest { query ->
                    flow { emit(geoService.searchAutoComplete(query)) }
                        .catch { emit(emptyList()) }
                }
                .collect { suggestions ->
                    _pickUpResultsSuggestions.value = suggestions
                }
        }

        // 2) Debounce dropoff‐query the same way
        viewModelScope.coroutineScope.launch {
            dropOffQuery
                .debounce(1000)
                .filter { it.length >= 2 }
                .distinctUntilChanged()
                .filter { !_skipDropOffAuto.value }       // skip when true
                .flatMapLatest { query ->
                    flow { emit(geoService.searchAutoComplete(query)) }
                        .catch { emit(emptyList()) }
                }
                .collect { suggestions ->
                    _dropOffResultsSuggestions.value = suggestions
                }
        }
    }

    /** Call this from your TextField’s onValueChange */
    fun onQueryChanged(newQuery: String, isPickUp: Boolean) {
        if (isPickUp) {
            _skipPickUpAuto.value = false         // re-enable when user types
            _pickUpResultsSuggestions.value = emptyList()
            _pickUpQuery.value = newQuery
        } else {
            _skipPickUpAuto.value = false         // re-enable when user types
            _dropOffResultsSuggestions.value = emptyList()
            _dropOffQuery.value = newQuery
        }
        //onSearch(isPickUp)
    }

    fun onPlaceSelected(placeId: String, isPickUp: Boolean) {
        viewModelScope.coroutineScope.launch {
            val details = geoService.searchPlace(placeId)
            if (isPickUp) {

                _skipPickUpAuto.value = true         // skip the next debounce emission
                _pickUpResultsSuggestions.value = emptyList()
                _pickUpAddress.value = details
                _pickUpQuery.value = "${details.name}, ${details.formattedAddress}"
            } else {
                _skipDropOffAuto.value = true
                _dropOffResultsSuggestions.value = emptyList()
                _dropOffAddress.value = details
                _dropOffQuery.value = "${details.name}, ${details.formattedAddress}"
            }
            onZoomUpdate(14f)
            computePath()
        }
    }

    private suspend fun computePath() {
        if (_pickUpAddress.value != null && _dropOffAddress.value != null) {
            val response = geoService.computeRoute(
                LatLng(
                    _pickUpAddress.value!!.lat,
                    _pickUpAddress.value!!.lng
                ),
                LatLng(
                    _dropOffAddress.value!!.lat,
                    _dropOffAddress.value!!.lng
                )
            )

            response?.let {
                _routeInfo.value = it
            }
        }
    }

    fun clearPickup() {
        _skipPickUpAuto.value = false
        _pickUpQuery.value = ""
        _pickUpResultsSuggestions.value = emptyList()
        _pickUpAddress.value = null
        _routeInfo.value = null
    }

    fun clearDropOff() {
        _skipDropOffAuto.value = false
        _dropOffQuery.value = ""
        _dropOffResultsSuggestions.value = emptyList()
        _dropOffAddress.value = null
        _routeInfo.value = null
    }

    fun swapPickupAndDropOff() {
        // Swap the text queries
        val oldQuery = _pickUpQuery.value
        _pickUpQuery.value = _dropOffQuery.value
        _dropOffQuery.value = oldQuery

        // Swap the suggestions lists
        val oldSuggestions = _pickUpResultsSuggestions.value
        _pickUpResultsSuggestions.value = _dropOffResultsSuggestions.value
        _dropOffResultsSuggestions.value = oldSuggestions

        // Swap the selected address objects (so your markers swap too)
        val oldAddress = _pickUpAddress.value
        _pickUpAddress.value = _dropOffAddress.value
        _dropOffAddress.value = oldAddress

        _skipPickUpAuto.value = true
        _skipDropOffAuto.value = true

        _routeInfo.value = null
        viewModelScope.coroutineScope.launch { computePath() }

    }

    private fun onZoomUpdate(newZoom: Float) {
        _zoom.value = newZoom
    }

    suspend fun checkPermission(
        permission: Permission,
        controller: PermissionsController,
        snackBarHostState: SnackbarHostState
    ) {
        permissionUseCase.checkPermission(
            permission = permission,
            controller = controller,
            snackBarHostState = snackBarHostState
        )
    }
}