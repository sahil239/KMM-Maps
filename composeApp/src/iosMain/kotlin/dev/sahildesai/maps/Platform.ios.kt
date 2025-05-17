package dev.sahildesai.maps

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()
actual fun provideAddressSearchApiKey(): String {
    TODO("Not yet implemented")
}