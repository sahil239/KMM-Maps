package dev.sahildesai.maps.domain

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException

class PermissionUseCase: IPermissionUseCase {
    override suspend fun checkPermission(
        permission: Permission,
        controller: PermissionsController,
        snackBarHostState: SnackbarHostState
    ){
        val granted = controller.isPermissionGranted(permission)
        if(!granted){
            try {
                controller.providePermission(permission)
            }catch (e: DeniedException){
                handleSnackBarHostState(controller, snackBarHostState, "Denied!")
            }catch (e: DeniedAlwaysException){
                handleSnackBarHostState(controller, snackBarHostState, "Permission Denied!")
            }catch (e: RequestCanceledException){
                handleSnackBarHostState(controller, snackBarHostState, "Request Canceled!")
            }
        }else{
            snackBarHostState.showSnackbar(
                message = "Permission granted!"
            )
        }

    }

    private suspend fun handleSnackBarHostState(
        controller: PermissionsController,
        snackBarHostState: SnackbarHostState,
        message: String
    ){
        val result = snackBarHostState.showSnackbar(
            message = message,
            actionLabel = "Open Settings",
            duration = SnackbarDuration.Long
        )
        if(result == SnackbarResult.ActionPerformed){
            controller.openAppSettings()
        }
    }
}