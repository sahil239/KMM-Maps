package dev.sahildesai.maps.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import dev.icerock.moko.permissions.location.LOCATION
import dev.sahildesai.maps.GoldenOrange
import dev.sahildesai.maps.Twilight
import dev.sahildesai.maps.models.AutocompletePrediction
import dev.sahildesai.maps.models.LatLng
import dev.sahildesai.maps.models.MarkerData
import dev.sahildesai.maps.models.MarkerType
import dev.sahildesai.maps.models.Route
import dev.sahildesai.maps.models.decodePolyline
import dev.sahildesai.maps.toOneDecimalString
import kmm_maps.composeapp.generated.resources.Res
import kmm_maps.composeapp.generated.resources.ic_swap
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

@Composable
fun MapSearchScreenContent(viewModel: MapSearchViewModel) {

    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) {
        factory.createPermissionsController()
    }

    BindEffect(controller)

    val askedLocation = remember { mutableStateOf(false) }
    if (!askedLocation.value) {
        askedLocation.value = true
        scope.launch {
            viewModel.checkPermission(
                permission = Permission.LOCATION,
                controller = controller,
                snackBarHostState = snackBarHostState
            )
        }
    }
    MapContent(viewModel)
}

@Composable
fun MapContent(viewModel: MapSearchViewModel) {

    val pickUpQuery by viewModel.pickUpQuery.collectAsState(initial = "")
    val pickUpSuggestions = viewModel.pickUpResultsSuggestions.collectAsStateWithLifecycle()
    val selectedPickUpAddress by viewModel.pickUpAddress.collectAsStateWithLifecycle()
    val dropOffQuery by viewModel.dropOffQuery.collectAsState(initial = "")
    val dropOffSuggestions = viewModel.dropOffResultsSuggestions.collectAsStateWithLifecycle()
    val selectedDropOffAddress by viewModel.dropOffAddress.collectAsStateWithLifecycle()

    val routeInfo = viewModel.routeInfo.collectAsStateWithLifecycle()

    var pickUpCoordinates by remember { mutableStateOf(Offset.Zero) }
    var pickUpSize by remember { mutableStateOf(IntSize.Zero) }

    var dropOffCoordinates by remember { mutableStateOf(Offset.Zero) }
    var dropOffSize by remember { mutableStateOf(IntSize.Zero) }

    val zoom by viewModel.zoom.collectAsState(initial = 1f)
    Column(Modifier.fillMaxSize()) {

        val markers = buildList {
            selectedPickUpAddress?.let {
                add(MarkerData(LatLng(it.lat, it.lng), "Pick Up", MarkerType.PICK_UP))
            }
            selectedDropOffAddress?.let {
                add(MarkerData(LatLng(it.lat, it.lng), "Drop Off", MarkerType.DROP_OFF))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().background(Twilight).statusBarsPadding()
                .padding(vertical = 20.dp).padding(start = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Column(Modifier.fillMaxWidth().weight(1f)) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coordinates ->
                            pickUpCoordinates = coordinates.localToRoot(Offset.Zero)
                            pickUpSize = coordinates.size
                        }
                ) {
                    SimpleSearchField(
                        value = pickUpQuery,
                        onValueChange = { viewModel.onQueryChanged(it, true) },
                        placeholder = "Pick Up",
                        clearField = { viewModel.clearPickup() }
                    )
                }

                Spacer(Modifier.height(8.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            dropOffCoordinates = coords.localToRoot(Offset.Zero)
                            dropOffSize = coords.size
                        }
                ) {
                    SimpleSearchField(
                        value = dropOffQuery,
                        onValueChange = { viewModel.onQueryChanged(it, false) },
                        placeholder = "Drop Off",
                        clearField = { viewModel.clearDropOff() }
                    )
                }
            }

            IconButton(onClick = { viewModel.swapPickupAndDropOff() }) {
                Image(
                    painter = painterResource(Res.drawable.ic_swap),
                    contentDescription = "Swap markers"
                )
            }
        }

        if (pickUpSuggestions.value.isNotEmpty()) {
            Popup(
                offset = IntOffset(
                    x = pickUpCoordinates.x.roundToInt(),
                    y = (pickUpCoordinates.y + pickUpSize.height).roundToInt()
                ),
                onDismissRequest = { /* you can clear suggestions here if you want */ },
                properties = PopupProperties(focusable = false)
            ) {
                SuggestionList(
                    isPickUp = true,
                    items = pickUpSuggestions.value,
                    onItem = viewModel::onPlaceSelected
                )
            }
        }

        if (dropOffSuggestions.value.isNotEmpty()) {
            Popup(
                offset = IntOffset(
                    x = dropOffCoordinates.x.roundToInt(),
                    y = (dropOffCoordinates.y + dropOffSize.height).roundToInt()
                ),
                onDismissRequest = { /* clear dropoff suggestions */ },
                properties = PopupProperties(focusable = false)
            ) {
                SuggestionList(
                    isPickUp = false,
                    items = dropOffSuggestions.value,
                    onItem = viewModel::onPlaceSelected
                )
            }
        }

        MapView(
            zoom = zoom,
            markers = markers,
            modifier = Modifier.fillMaxSize().weight(1f),
            polyline = routeInfo.value?.polyline?.encodedPolyline?.let { decodePolyline(it) }
                ?: emptyList()
        )

        BottomInfoBar(routeInfo.value)
    }
}

@Composable
fun BottomInfoBar(route: Route?) {
    route?.let { info ->
        // format distance in km, time in minutes:
        val km = (info.distanceMeters / 1000f)
        val mins = info.duration.replace("s", "").toInt() / 60

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Twilight.copy(alpha = 0.9f))
                .padding(vertical = 12.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextValueElement("Distance (approx.)", "${km.toOneDecimalString()} kms")
            TextValueElement("Duration", "$mins mins")
        }
    }
}

@Composable
private fun SimpleSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    clearField: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.White) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = clearField) {
                    Icon(
                        tint = Color.White,
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear pickup"
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            cursorColor = Twilight,
            focusedBorderColor = GoldenOrange,
            unfocusedBorderColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
        )
    )
}

@Composable
private fun SuggestionList(
    isPickUp: Boolean,
    items: List<AutocompletePrediction>,
    onItem: (String, Boolean) -> Unit
) {
    LazyColumn(
        Modifier
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .verticalScroll(rememberScrollState())
            .heightIn(max = 600.dp)
            .padding(start = 10.dp)
    ) {
        itemsIndexed(
            items = items,
            key = { _, address ->
                address.hashCode()
            }
        ) { _, address ->
            Text(
                text = address.description,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItem(address.placeId, isPickUp) }
                    .padding(12.dp)
            )
        }
    }
}

@Composable
fun TextValueElement(label: String, value: String) {
    Column(
        modifier = Modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )
    }
}

@Composable
expect fun MapView(
    zoom: Float,
    markers: List<MarkerData>,
    polyline: List<LatLng>,
    modifier: Modifier = Modifier
)