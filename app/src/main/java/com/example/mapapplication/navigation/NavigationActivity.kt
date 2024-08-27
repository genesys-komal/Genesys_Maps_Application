package com.example.mapapplication.navigation

import android.os.Bundle
import android.os.Parcelable
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import com.example.mapapplication.databinding.NavigationActivityBinding
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.services.android.navigation.ui.v5.NavigationView
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback
import com.mapbox.services.android.navigation.ui.v5.listeners.NavigationListener
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions
import com.mapbox.services.android.navigation.v5.navigation.NavigationConstants


class NavigationActivity : AppCompatActivity(), OnNavigationReadyCallback,
    NavigationListener {
    private lateinit var map: MapboxMap
    private lateinit var binding: NavigationActivityBinding
    private var navigationView: NavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NavigationActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navigationView = binding.navigationView
//        map.uiSettings.isAttributionEnabled = false
        navigationView?.onCreate(savedInstanceState)
        initialize()
    }


    public override fun onStart() {
        super.onStart()
        navigationView?.onStart()
    }

    public override fun onResume() {
        super.onResume()
        navigationView?.onResume()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        navigationView?.onLowMemory()
    }

    override fun onBackPressed() {
        // If the navigation view didn't need to do anything, call super
        if (!navigationView?.onBackPressed()!!) {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        navigationView?.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        navigationView?.onRestoreInstanceState(savedInstanceState)
    }

    public override fun onPause() {
        super.onPause()
        navigationView?.onPause()
    }

    public override fun onStop() {
        super.onStop()
        navigationView?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        navigationView?.onDestroy()
    }

    override fun onNavigationReady(isRunning: Boolean) {
        val options = NavigationViewOptions.builder()
        options.navigationListener(this)
//        options.routeListener(object : RouteListener {
//            override fun allowRerouteFrom(p0: Point?): Boolean {
//                return false
//            }
//
//            override fun onOffRoute(p0: Point?) {
//
//            }
//
//            override fun onRerouteAlong(p0: DirectionsRoute?) {
//
//            }
//
//            override fun onFailedReroute(p0: String?) {
//
//            }
//
//            override fun onArrival() {
//
//            }
//        })
        extractRoute(options)
        extractConfiguration(options)
        val mapBoxNavigationOptions =
            MapboxNavigationOptions.builder().defaultMilestonesEnabled(true).build()

        options.navigationOptions(mapBoxNavigationOptions)
        navigationView?.startNavigation(options.build())
    }

    override fun onCancelNavigation() {
        finishNavigation()
    }

    override fun onNavigationFinished() {
        finishNavigation()
    }

    override fun onNavigationRunning() {
        // Intentionally empty
    }

    private fun initialize() {
        val position =
            intent.getParcelableExtra<Parcelable>(NavigationConstants.NAVIGATION_VIEW_INITIAL_MAP_POSITION)
        if (position != null) {
            navigationView?.initialize(this, (position as CameraPosition?)!!)
        } else {
            navigationView?.initialize(this)
        }
    }

    private fun extractRoute(options: NavigationViewOptions.Builder) {
        val route = NavigationLauncher.extractRoute(this)
        options.directionsRoute(route)
    }

    private fun extractConfiguration(options: NavigationViewOptions.Builder) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        options.shouldSimulateRoute(
            preferences.getBoolean(
                NavigationConstants.NAVIGATION_VIEW_SIMULATE_ROUTE,
                false
            )
        )
    }

    private fun finishNavigation() {
        NavigationLauncher.cleanUpPreferences(this)
        finish()
    }
}
