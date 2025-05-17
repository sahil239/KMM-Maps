package dev.sahildesai.maps

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()
actual fun provideAddressSearchApiKey(): String = "getString(R.string.address_search_api_key)"