package dev.sahildesai.maps

import android.app.Application
import dev.sahildesai.maps.di.initKoin
import org.koin.android.ext.koin.androidContext

class MapsApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()

    }
}