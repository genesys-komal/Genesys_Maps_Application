package com.example.mapapplication.helpers

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.engine.LocationEngineRequest
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.location.permissions.PermissionsManager
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style

class LocationHelper(private val context: Context) {

    var lastLocation: Location? = null
    var permissionsManager: PermissionsManager? = null
    private val mockedMumbaiLocation = LatLng(19.10626028949677, 72.89286085118788)

    @SuppressLint("MissingPermission")
    fun setupLocationComponent(locationComponent: LocationComponent, style: Style) {
        val locationComponentOptions =
            LocationComponentOptions.builder(context)
                .pulseEnabled(true)
                .build()

        val locationComponentActivationOptions =
            buildLocationComponentActivationOptions(style, locationComponentOptions)
        locationComponent.activateLocationComponent(locationComponentActivationOptions)
        locationComponent.isLocationComponentEnabled = true
//        locationComponent.cameraMode = CameraMode.TRACKING_GPS_NORTH
        locationComponent.cameraMode = CameraMode.TRACKING
//        locationComponent.renderMode = RenderMode.NORMAL
        locationComponent.renderMode = RenderMode.COMPASS
        locationComponent.forceLocationUpdate(lastLocation)
        lastLocation = locationComponent.lastKnownLocation
    }

    private fun buildLocationComponentActivationOptions(
        style: Style, locationComponentOptions: LocationComponentOptions
    ): LocationComponentActivationOptions {
        return LocationComponentActivationOptions
            .builder(context, style)
            .locationComponentOptions(locationComponentOptions)
            .useDefaultLocationEngine(true)
            .locationEngineRequest(
                LocationEngineRequest.Builder(500)
                    .setFastestInterval(500)
                    .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                    .build()
            )
            .build()
    }

    fun mockLocation(map: MapboxMap) {
        val newCameraPosition = CameraPosition.Builder()
            .target(mockedMumbaiLocation) // this is Mumbai
            .zoom(14.0)
            .build()

        map.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition), 1000)
    }
}