package com.example.mapapplication.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions
import com.mapbox.services.android.navigation.v5.models.DirectionsRoute
import com.mapbox.services.android.navigation.v5.navigation.NavigationConstants

object NavigationLauncher {

    /**
     * Starts the UI with a [DirectionsRoute] already retrieved from
     * [com.mapbox.services.android.navigation.v5.navigation.NavigationRoute]
     *
     * @param activity must be launched from another [Activity]
     * @param options  with fields to customize the navigation view
     */
    fun startNavigation(activity: Activity, options: NavigationLauncherOptions) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val editor = preferences.edit()
        storeDirectionsRouteValue(options, editor)
        storeConfiguration(options, editor)
        storeThemePreferences(options, editor)
        editor.apply()
        val navigationActivity = Intent(
            activity, NavigationActivity::class.java
        )

        NavigationLauncher.storeInitialMapPosition(options, navigationActivity)
        activity.startActivity(navigationActivity)
    }

    /**
     * Used to extract the route used to launch the drop-in UI.
     *
     *
     * Extracts the route [String] from [SharedPreferences] and converts
     * it back to a [DirectionsRoute] object with [Gson].
     *
     * @param context to retrieve [SharedPreferences]
     * @return [DirectionsRoute] stored when launching
     */
    fun extractRoute(context: Context?): DirectionsRoute {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val directionsRouteJson =
            preferences.getString(NavigationConstants.NAVIGATION_VIEW_ROUTE_KEY, "")
        return DirectionsRoute.fromJson(directionsRouteJson)
    }

    fun cleanUpPreferences(context: Context?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = preferences.edit()
        editor
            .remove(NavigationConstants.NAVIGATION_VIEW_ROUTE_KEY)
            .remove(NavigationConstants.NAVIGATION_VIEW_SIMULATE_ROUTE)
            .remove(NavigationConstants.NAVIGATION_VIEW_PREFERENCE_SET_THEME)
            .remove(NavigationConstants.NAVIGATION_VIEW_PREFERENCE_SET_THEME)
            .remove(NavigationConstants.NAVIGATION_VIEW_LIGHT_THEME)
            .remove(NavigationConstants.NAVIGATION_VIEW_DARK_THEME)
            .apply()
    }

    private fun storeDirectionsRouteValue(
        options: NavigationLauncherOptions,
        editor: SharedPreferences.Editor
    ) {
        editor.putString(
            NavigationConstants.NAVIGATION_VIEW_ROUTE_KEY,
            options.directionsRoute().toJson()
        )
    }

    private fun storeConfiguration(
        options: NavigationLauncherOptions,
        editor: SharedPreferences.Editor
    ) {
        editor.putBoolean(
            NavigationConstants.NAVIGATION_VIEW_SIMULATE_ROUTE,
            options.shouldSimulateRoute()
        )
    }

    private fun storeThemePreferences(
        options: NavigationLauncherOptions,
        editor: SharedPreferences.Editor
    ) {
        val preferenceThemeSet =
            options.lightThemeResId() != null || options.darkThemeResId() != null
        editor.putBoolean(
            NavigationConstants.NAVIGATION_VIEW_PREFERENCE_SET_THEME,
            preferenceThemeSet
        )
        if (preferenceThemeSet) {
            if (options.lightThemeResId() != null) {
                editor.putInt(
                    NavigationConstants.NAVIGATION_VIEW_LIGHT_THEME,
                    options.lightThemeResId()!!
                )
            }
            if (options.darkThemeResId() != null) {
                editor.putInt(
                    NavigationConstants.NAVIGATION_VIEW_DARK_THEME,
                    options.darkThemeResId()!!
                )
            }
        }
    }

    private fun storeInitialMapPosition(
        options: NavigationLauncherOptions,
        navigationActivity: Intent
    ) {
        if (options.initialMapCameraPosition() != null) {
            navigationActivity.putExtra(
                NavigationConstants.NAVIGATION_VIEW_INITIAL_MAP_POSITION,
                options.initialMapCameraPosition()
            )
        }
    }
}
