package dev.sahildesai.maps

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import dev.sahildesai.maps.ui.MapSearchScreenContent
import dev.sahildesai.maps.ui.MapSearchViewModel

@Composable
fun App(viewModel: MapSearchViewModel) {
    MaterialTheme {
        MapSearchScreenContent(viewModel)
    }
}
