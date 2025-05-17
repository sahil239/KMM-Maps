package dev.sahildesai.maps

import androidx.compose.ui.window.ComposeUIViewController
import dev.sahildesai.maps.ui.MapSearchViewModel
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
   val viewModel = MapSearchViewModel()
   return ComposeUIViewController { App(viewModel) }
}