package dev.sahildesai.maps.domain

import androidx.compose.material3.SnackbarHostState
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController

interface IPermissionUseCase {
    suspend fun checkPermission(
        permission: Permission,
        controller: PermissionsController,
        snackBarHostState: SnackbarHostState
    )
}