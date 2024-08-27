package com.example.mapapplication

import android.app.Application
import com.mapbox.mapboxsdk.Mapbox

class MapApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Mapbox.getInstance(this)
    }
}