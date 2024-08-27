package com.example.mapapplication.helpers

import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.plugins.annotation.Symbol
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions

class MapHelper {

    fun moveCameraToResult(map: MapboxMap?, latLng: LatLng) {
        val newCameraPosition = CameraPosition.Builder()
            .target(latLng)
            .zoom(15.0)
            .build()

        map?.animateCamera(
            CameraUpdateFactory.newCameraPosition(newCameraPosition),
            2000
        )
    }
}

fun SymbolManager.addMarker(latLng: LatLng, text: String): Symbol? =
    create(SymbolOptions().withLatLng(latLng).withIconImage("Marker").withTextField(text).withTextColor("#FFFFFF"))
fun SymbolManager.addMarker(latLng: LatLng): Symbol? =
    create(SymbolOptions().withLatLng(latLng).withIconImage("myMarker"))
fun SymbolManager.addMarker(latLng: LatLng,size: Float, color: String): Symbol? =
    create(SymbolOptions().withLatLng(latLng).withIconImage("startMarker").withIconSize(size).withIconColor(color))