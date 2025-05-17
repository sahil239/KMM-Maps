package dev.sahildesai.maps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.PermissionsControllerImpl
import dev.sahildesai.maps.ui.MapSearchViewModel
import org.koin.android.ext.android.get

class MainActivity : ComponentActivity() {
    private lateinit var permissionsController: PermissionsController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionsController = PermissionsControllerImpl(this@MainActivity)
        permissionsController.bind(this@MainActivity)

        setContent {
            App(get<MapSearchViewModel>())
        }
    }
}