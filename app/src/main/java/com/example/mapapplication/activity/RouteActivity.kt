package com.example.mapapplication.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.mapapplication.originCode.net.models.TruckCostingOptions
import com.example.mapapplication.originCode.net.models.AutoCostingOptions
import com.example.mapapplication.originCode.net.models.CostingModel
import com.example.mapapplication.originCode.net.models.CostingOptions
import com.example.mapapplication.originCode.net.models.DirectionsOptions
import com.example.mapapplication.originCode.net.models.DistanceUnit
import com.example.mapapplication.originCode.net.models.RouteOsrmResponse
import com.example.mapapplication.originCode.net.models.RoutingWaypoint
import com.example.mapapplication.originCode.net.models.ValhallaLanguages
import com.example.mapapplication.BuildConfig
import com.example.mapapplication.MainActivity
import com.example.mapapplication.R
import com.example.mapapplication.adapter.WayPintData
import com.example.mapapplication.adapter.WayPointAdapter
import com.example.mapapplication.common.Alert
import com.example.mapapplication.common.BaseActivity
import com.example.mapapplication.common.Commons
import com.example.mapapplication.common.Constants
import com.example.mapapplication.common.hideKeyboard
import com.example.mapapplication.common.updateText
import com.example.mapapplication.databinding.ActivityRouteBinding
import com.example.mapapplication.helpers.addMarker
import com.example.mapapplication.helpers.decodePolyline
import com.example.mapapplication.helpers.toLatLng
import com.example.mapapplication.helpers.toPoint
import com.example.mapapplication.navigation.NavigationLauncher
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.LineManager
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions
import com.mapbox.services.android.navigation.v5.models.DirectionsCriteria
import com.mapbox.services.android.navigation.v5.models.DirectionsResponse
import com.mapbox.services.android.navigation.v5.models.DirectionsRoute
import com.mapbox.services.android.navigation.v5.models.RouteOptions
import kotlinx.coroutines.launch
import okhttp3.internal.userAgent

private const val DOTTED_POLYLINE_SOURCE_ID = "DOTTED_POLYLINE_SOURCE_ID"
private const val DOTTED_POLYLINE_LAYER_ID = "DOTTED_POLYLINE_LAYER_ID"
private const val DOTTED_POLYLINE_SOURCE_ID_NEW = "DOTTED_POLYLINE_SOURCE_ID_NEW"
private const val DOTTED_POLYLINE_LAYER_ID_NEW = "DOTTED_POLYLINE_LAYER_ID_NEW"

class RouteActivity : BaseActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityRouteBinding

    private var openPopup: SymbolLayer? = null // Track the currently open popup

    private lateinit var map: MapboxMap
    private val adapterWayPoint: WayPointAdapter by lazy { WayPointAdapter() }
    private lateinit var getResult: ActivityResultLauncher<Intent>

    private var symbolManager: SymbolManager? = null
    private var lineManager: LineManager? = null
    private var routeResponse: RouteOsrmResponse? = null
    private var routeResponseShortest: RouteOsrmResponse? = null
    private var place = ""
    private var wayList = ArrayList<RoutingWaypoint>()
    private var wayPoints = ArrayList<WayPintData>()
    private var destination: Point? = null
    private var position: WayPintData? = null
    private var wayIndex: Int? = null

    companion object {

        var origin: Point? = null
        var originAdded: Boolean? = false
        var current: Point? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRouteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mapView.apply {
            onCreate(savedInstanceState)
            getMapAsync(this@RouteActivity)
        }

        setupView()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupView() = binding.apply {
        result()
        recyclerWayPoints.adapter = adapterWayPoint
        startRouteButton.setOnClickListener {
            if (routeResponse == null) {
                showToast("Please select start and end points")
                return@setOnClickListener
            }

            startNavigation(routeResponse!!)
        }
        shuffle.setOnClickListener {
            if (validate()) {

                // Swap the text values
                val tempText = originPoint.text
                originPoint.text = destinationPoint.text
                destinationPoint.text = tempText

                // Swap the points
                val tempPoint = destination
                destination = RouteActivity.origin
                RouteActivity.origin = tempPoint

                // Update the wayList
                wayList[wayList.size - 1] =
                    RoutingWaypoint(lat = destination!!.latitude(), lon = destination!!.longitude())
                wayList[0] = RoutingWaypoint(
                    lat = RouteActivity.origin!!.latitude(),
                    lon = RouteActivity.origin!!.longitude()
                )
                getDirectionsOsrm(CostingModel.auto)
            }

        }

        fabRecenter.setOnClickListener {
            moveCameraResult()
        }
        fabStyles.setOnClickListener {
            showBottomSheetDialog()
        }
        destinationPoint.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                place = Commons.destination
                startNewActivity(destinationPoint.text.toString(), destination)
            }
            true
        }

        originPoint.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                place = Commons.origin
                startNewActivity(originPoint.text.toString(), origin)
            }
            true
        }


        clearOrigin.setOnClickListener {
            originPoint.text = ""
            symbolManager?.deleteAll()
            lineManager?.deleteAll()
            RouteActivity.origin = null
            bottomView.isVisible = false
        }
        clearDestination.setOnClickListener {
            destinationPoint.text = ""
            symbolManager?.deleteAll()
            lineManager?.deleteAll()
            destination = null
            bottomView.isVisible = false
        }
        clearPoints.setOnClickListener {
            destroyAll()
            startActivity(Intent(this@RouteActivity, MainActivity::class.java))
        }

        walkLayout.setOnClickListener {
            if (validate()) {
                clearPolylines()
                getDirectionsOsrm(CostingModel.pedestrian)
                walkLayout.background =
                    ContextCompat.getDrawable(this@RouteActivity, R.drawable.rounded_blue)
                busLayout.background = ContextCompat.getDrawable(
                    this@RouteActivity,
                    R.drawable.background_map_search
                )
                carLayout.background = ContextCompat.getDrawable(
                    this@RouteActivity,
                    R.drawable.background_map_search
                )
                truckLayout.background = ContextCompat.getDrawable(
                    this@RouteActivity,
                    R.drawable.background_map_search
                )
                bicycleLayout.background = ContextCompat.getDrawable(
                    this@RouteActivity,
                    R.drawable.background_map_search
                )

            }
        }
        busLayout.setOnClickListener {
            if (validate()) {
                clearPolylines()
                getDirectionsOsrmShortest(
                    CostingModel.bus, CostingOptions(
                        AutoCostingOptions(shortest = true)
                    )
                )
                getDirectionsOsrm(CostingModel.bus)

                walkLayout.background = ContextCompat.getDrawable(
                    this@RouteActivity,
                    R.drawable.background_map_search
                )
                busLayout.background =
                    ContextCompat.getDrawable(this@RouteActivity, R.drawable.rounded_blue)
                carLayout.background = ContextCompat.getDrawable(
                    this@RouteActivity,
                    R.drawable.background_map_search
                )
                truckLayout.background = ContextCompat.getDrawable(
                    this@RouteActivity,
                    R.drawable.background_map_search
                )
                bicycleLayout.background = ContextCompat.getDrawable(
                    this@RouteActivity,
                    R.drawable.background_map_search
                )
            }
        }
        binding.truckLayout.setOnClickListener {
            if (validate()) {
                clearPolylines()
                getDirectionsOsrmShortest(
                    CostingModel.truck, CostingOptions(
                        truck = TruckCostingOptions(shortest = true)
                    )
                )
                getDirectionsOsrm(CostingModel.truck)
                walkLayout.background = ContextCompat.getDrawable(
                    this@RouteActivity,
                    R.drawable.background_map_search
                )
                busLayout.background = ContextCompat.getDrawable(
                    this@RouteActivity,
                    R.drawable.background_map_search
                )
                carLayout.background = ContextCompat.getDrawable(
                    this@RouteActivity,
                    R.drawable.background_map_search
                )
                truckLayout.background =
                    ContextCompat.getDrawable(this@RouteActivity, R.drawable.rounded_blue)
                bicycleLayout.background = ContextCompat.getDrawable(
                    this@RouteActivity,
                    R.drawable.background_map_search
                )
            }
        }
        bicycleLayout.setOnClickListener {
            if (validate()) {
                clearPolylines()
                getDirectionsOsrm(CostingModel.bicycle)
                walkLayout.background = ContextCompat.getDrawable(
                    this@RouteActivity,
                    R.drawable.background_map_search
                )
                busLayout.background = ContextCompat.getDrawable(
                    this@RouteActivity,
                    R.drawable.background_map_search
                )
                carLayout.background = ContextCompat.getDrawable(
                    this@RouteActivity,
                    R.drawable.background_map_search
                )
                truckLayout.background = ContextCompat.getDrawable(
                    this@RouteActivity,
                    R.drawable.background_map_search
                )
                bicycleLayout.background =
                    ContextCompat.getDrawable(this@RouteActivity, R.drawable.rounded_blue)
            }
        }
        carLayout.setOnClickListener {
            if (validate()) {
                clearPolylines()
                getDirectionsOsrmShortest(
                    CostingModel.auto, CostingOptions(
                        auto = AutoCostingOptions(shortest = true)
                    )
                )
                getDirectionsOsrm(CostingModel.auto)
                walkLayout.background = ContextCompat.getDrawable(
                    this@RouteActivity,
                    R.drawable.background_map_search
                )
                busLayout.background = ContextCompat.getDrawable(
                    this@RouteActivity,
                    R.drawable.background_map_search
                )
                carLayout.background =
                    ContextCompat.getDrawable(this@RouteActivity, R.drawable.rounded_blue)
                truckLayout.background = ContextCompat.getDrawable(
                    this@RouteActivity,
                    R.drawable.background_map_search
                )
                bicycleLayout.background = ContextCompat.getDrawable(
                    this@RouteActivity,
                    R.drawable.background_map_search
                )
            }
        }

        optimalTime.setOnClickListener {
            position = null
            place = Commons.wayPoint
            startNewActivity("", RouteActivity.origin)
        }
        adapterWayPoint.setOnItemClicked { prediction ->

            val wPoint =
                RoutingWaypoint(lat = prediction.point.latitude, lon = prediction.point.longitude)
            if (wayList.contains(wPoint)) {
                showToast("node deleted")
                wayList.remove(wPoint)
                clearPolylines()
                getDirectionsOsrm(CostingModel.auto)

            }

            wayPoints.remove(prediction)
            if (wayPoints.isEmpty()) {
                binding.recyclerWayPoints.isVisible = false
            }
            if (wayPoints.size >= 2) {
                val heightInPx = dpToPx(120)
                val layoutParams = binding.recyclerWayPoints.layoutParams
                layoutParams.height = heightInPx
                binding.recyclerWayPoints.layoutParams = layoutParams
            } else {
                val layoutParams = binding.recyclerWayPoints.layoutParams
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                binding.recyclerWayPoints.layoutParams = layoutParams

            }
            adapterWayPoint.submitList(wayPoints)
            adapterWayPoint.notifyDataSetChanged()


            getDirectionsOsrmShortest(
                CostingModel.auto, CostingOptions(
                    auto = AutoCostingOptions(shortest = true)
                )
            )
            getDirectionsOsrm(CostingModel.auto)
        }
        adapterWayPoint.setOnTextClicked {
            position = it
            place = Commons.wayPoint
            startNewActivity(it.description, RouteActivity.origin)
        }
    }

    override fun onMapReady(map: MapboxMap) {
        this.map = map
        setMapStyle(geoHelper.tilesUrl)
        map.setOnInfoWindowCloseListener {
            showToast(it.position.toString())
            map.selectMarker(it)
        }
//        setupLegend()
        map.addOnMapClickListener { latLng ->
//            changeRoute(latLng)
            if (destination != null) {
                Alert.alert {
                    title = "Put point"
                    description = "Add as via point"
                    alertContext = this@RouteActivity
                    positiveButton = {
                        if (destination != null && wayList.size >= 2) {
                            val point = latLng.toPoint()

                            wayList.add(
                                wayList.size - 1, RoutingWaypoint(
                                    lat = point.latitude(),
                                    lon = point.longitude()
                                )
                            )
                        } else {
                            destination = latLng.toPoint()
                            wayList.clear()
                            wayList.add(
                                RoutingWaypoint(
                                    lat = RouteActivity.origin!!.latitude(),
                                    lon = RouteActivity.origin!!.longitude()
                                )
                            )
                            wayList.add(
                                RoutingWaypoint(
                                    lat = destination!!.latitude(),
                                    lon = destination!!.longitude()
                                )
                            )
                        }
                        //clear previous line
                        clearPolylines()


                        symbolManager?.addMarker(latLng, "")
                        getReverseGeocodeWayPoint(latLng)

                        getDirectionsOsrmShortest(
                            CostingModel.auto, CostingOptions(
                                auto = AutoCostingOptions(shortest = true)
                            )
                        )
                        getDirectionsOsrm(CostingModel.auto)
                        binding.clearPoints.isVisible = true
                        flyCameraToBounds()
                    }
                    negativeButton = {}
                }

                true
            } else false
        }
        map.addOnMapLongClickListener { latLng ->
            showToast("hihjkjbnbmbnm  $latLng")

            val screenPoint = map.projection.toScreenLocation(latLng)
            val features = map.queryRenderedFeatures(screenPoint, "line-layer")
            if (features.isNotEmpty()) {
                val feature = features[0]
                showToast("You clicked on the polyline")
            }
            true
        }
    }

    private fun setMapStyle(style: String) {
        map.setStyle(style) { style ->

            lineManager = LineManager(binding.mapView, map, style)
            symbolManager = SymbolManager(binding.mapView, map, style).apply {
                iconAllowOverlap = true
                this.textAllowOverlap = true
            }
            style.addImage(
                "myMarker",
                BitmapFactory.decodeResource(resources, R.drawable.maplibre_marker_icon_default)
            )
            if (routeResponse != null) {
                clearPolylines()
            }
            getLocation(style)

        }
    }

    private fun flyCameraToBounds() {
        if (origin == null && destination == null) return
        val latLngList =
            listOf(origin!!.toLatLng(), destination!!.toLatLng())
        val latLngBounds = LatLngBounds.fromLatLngs(latLngList)
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 200)
        map.easeCamera(cameraUpdate)
    }

    private fun getLocation(style: Style) {
        // Check if permissions are granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permissions
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            locationHelper.setupLocationComponent(map.locationComponent, style)
            if (RouteActivity.origin == null || RouteActivity.origin == RouteActivity.current) {
                RouteActivity.origin = Point.fromLngLat(
                    locationHelper.lastLocation?.longitude ?: 0.0,
                    locationHelper.lastLocation?.latitude ?: 0.0
                )
            }
            RouteActivity.originAdded = true
            place = Commons.origin
            getReverseGeocode(
                LatLng(
                    locationHelper.lastLocation?.latitude ?: 0.0,
                    locationHelper.lastLocation?.longitude ?: 0.0
                )
            )
            RouteActivity.current = RouteActivity.origin
            val ss: LatLng? = intent.getParcelableExtra<LatLng>(Commons.address)
            if (ss != null) {
                place = Commons.destination
                getReverseGeocode(ss)

            }
        }
    }

    private fun getReverseGeocode(latLng: LatLng) {
        geoHelper.geoMapsApi?.getReverseGeocode(
            pointLat = latLng.latitude,
            pointLon = latLng.longitude,
            onSuccess = { peliasResponse ->
                lifecycleScope.launch {
                    if (peliasResponse.features.isEmpty()) {
                        showToast("No results found")
                        return@launch
                    }

                    val properties = peliasResponse.features.first().properties
                    val label = properties?.get("label")
//                    visibleNavigation()
//                    if (place == Commons.origin) {
//                        binding.originAutocomplete.updateText(label.toString())
//                        binding.originPoint.text = label.toString()
//                        originAdded = false
//                    } else if (place == Commons.destination) {
//                        binding.destinationAutocomplete.updateText(label.toString())
//                        binding.destinationPoint.text = label.toString()
//                    }
                    prepareAndStartNavigationNew(latLng,place,label.toString())
                }
            },
            onError = { errorMsg ->
                showToast(errorMsg)
            }
        )
    }

    private fun getReverseGeocodeWayPoint(latLng: LatLng) {
        geoHelper.geoMapsApi?.getReverseGeocode(
            pointLat = latLng.latitude,
            pointLon = latLng.longitude,
            onSuccess = { peliasResponse ->
                lifecycleScope.launch {
                    if (peliasResponse.features.isEmpty()) {
                        showToast("No results found")
                        return@launch
                    }

                    val properties = peliasResponse.features.first().properties
                    val label = properties?.get("label")

                    visibleNavigation()
                    if (wayPoints.isEmpty()) wayPoints.add(WayPintData(label.toString(), latLng, 0))
                    else wayPoints.add(WayPintData(label.toString(), latLng, wayList.size))
                    binding.recyclerWayPoints.isVisible = true
                    if (wayPoints.size >= 2) {
//                        binding.recyclerWayPoints.layoutParams.height = 120
                        val heightInPx = dpToPx(120)
                        val layoutParams = binding.recyclerWayPoints.layoutParams
                        layoutParams.height = heightInPx
                        binding.recyclerWayPoints.layoutParams = layoutParams
                    } else {
                        val layoutParams = binding.recyclerWayPoints.layoutParams
                        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                        binding.recyclerWayPoints.layoutParams = layoutParams

                    }
                    adapterWayPoint.submitList(wayPoints)
                    adapterWayPoint.notifyDataSetChanged()
                }
            },
            onError = { errorMsg ->
                showToast(errorMsg)
            }
        )
    }

    private fun startNavigation(routeResponse: RouteOsrmResponse) {
        showToast(BuildConfig.BASE_URL)
        val directionsResponse = DirectionsResponse.fromJson(Gson().toJson(routeResponse))
        val decodedShape = decodePolyline(routeResponse.routes.first().geometry)
        val routeOptions = RouteOptions.builder()
            .geometries(DirectionsCriteria.GEOMETRY_POLYLINE6)
            .profile(DirectionsCriteria.PROFILE_DRIVING)
            .accessToken(Commons.token) // fake AccessToken
            .user(userAgent)
//            .requestUuid("c945b0b4-9764-11ed-a8fc-0242ac120002") // fake UUID ver.1
            .requestUuid(directionsResponse.uuid().toString()) // fake UUID ver.1
//            .baseUrl("www.fakeUrl.com") // fake url
//            .baseUrl(GeoHelper.apiUrl) // fake url
            .baseUrl(BuildConfig.BASE_URL) // fake url
            .coordinates(decodedShape.map { latLng -> latLng.toPoint() })
            .voiceInstructions(true)
            .bannerInstructions(true)
            .build()

        val route = DirectionsRoute
            .fromJson(directionsResponse.routes().first().toJson())
            .toBuilder()
            .routeOptions(routeOptions).build()

        val options = NavigationLauncherOptions.builder()
            .directionsRoute(route)
            .shouldSimulateRoute(false)
            //For now that the way to put tile server..
            .lightThemeResId(R.style.TestNavigationViewDark)
            .darkThemeResId(R.style.TestNavigationViewDark)
            .initialMapCameraPosition(
                CameraPosition.Builder().target(RouteActivity.origin?.toLatLng()).build()
            )
            .build()
        NavigationLauncher.startNavigation(this@RouteActivity, options)
    }

    private fun validate(): Boolean {
        if (RouteActivity.origin == null) {
            showToast("Please select start points")
            return false
        } else if (destination == null) {
            showToast("Please select end points")
            return false
        }
        return true
    }

    private fun startNewActivity(place: String, origins: Point?) {
        val intent = Intent(this, FindActivity::class.java)
        intent.putExtra(Commons.place, place)
        intent.putExtra(Commons.origin, origins)
        getResult.launch(intent)

    }

    fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun result() {
        getResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val resultData = data?.getStringExtra(Constants.DESCRIPTION.toString())
                val placeID = data?.getStringExtra(Constants.PLACEID.toString())

                if (place == Commons.origin) {
                    binding.originAutocomplete.updateText(resultData.toString())

                } else if (place == Commons.destination) {
                    binding.destinationAutocomplete.updateText(resultData.toString())

                } else if (place == Commons.wayPoint) {
                    if (position != null) {
                        wayIndex = wayList.indexOf(
                            RoutingWaypoint(
                                lat = position?.point?.latitude!!,
                                lon = position?.point?.longitude!!,
                            )
                        )
                    }
                }
                if (placeID.isNullOrBlank()) {
                    getForwardGeocode(resultData.toString(), place)
                } else {
                    getPlaceDetails(listOf(placeID), place)
                }
                // Handle the result here, e.g., update UI
            }
        }

    }

    private fun getForwardGeocode(text: String, place: String) {
        geoHelper.geoMapsApi?.getForwardGeocode(
            text = text,
            onSuccess = { peliasResponse ->
                val coordinates = peliasResponse.features.first().geometry.coordinates.toLatLng()
                lifecycleScope.launch {

                    prepareAndStartNavigationNew(coordinates, place, text)

                }
            },
            onError = { errorMsg ->
                showToast(errorMsg)
            }
        )
    }

    private fun getPlaceDetails(ids: List<String>, place: String) {
        geoHelper.geoMapsApi?.getPlaceDetails(
            ids = ids,
            onSuccess = { peliasResponse ->
                val feature = peliasResponse.features.first()
                val coordinates = feature.geometry.coordinates.toLatLng()
                lifecycleScope.launch {
                    val label = feature.properties?.get("label")
                    prepareAndStartNavigationNew(coordinates, place, label.toString())
                }
            },
            onError = { errorMsg ->
                showToast(errorMsg)
            }
        )
    }

    private fun prepareAndStartNavigationNew(
        coordinates: LatLng,
        place: String,
        description: String
    ) {
        symbolManager?.deleteAll()
        lineManager?.deleteAll()
        hideKeyboard()
        when (place) {
            Commons.origin -> {
                originAdded = false
                origin = coordinates.toPoint()
                binding.originPoint.text = description
                wayList[0] = RoutingWaypoint(
                    lat = origin!!.latitude(),
                    lon = origin!!.longitude()
                )


            }

            Commons.destination -> {
                destination = coordinates.toPoint()
                binding.destinationPoint.text = description

                wayList[wayList.size - 1] = RoutingWaypoint(
                    lat = destination!!.latitude(),
                    lon = destination!!.longitude()
                )
            }

            Commons.wayPoint -> {
                if (position != null) {
                    val i = wayPoints.indexOf(position)
                    position?.description = description
                    position?.point = coordinates
                    wayPoints[i] = position!!
                    wayList[wayIndex!!] =
                        RoutingWaypoint(lat = coordinates.latitude, lon = coordinates.longitude)

                } else {
                    wayPoints.add(WayPintData(description, coordinates, 0))
                    wayList.add(
                        wayList.size - 1,
                        RoutingWaypoint(lat = coordinates.latitude, lon = coordinates.longitude)
                    )

                }
//               wayIndex = wayList.lastIndexOf(RoutingWaypoint(lat = position?.point?.latitude!! , lon = position?.point?.longitude!!))
//               wayList.add(wayIndex!!,RoutingWaypoint(lat = coordinates.latitude, lon = coordinates.longitude))
                binding.recyclerWayPoints.isVisible = true
                if (wayPoints.size >= 2) {
//                    binding.recyclerWayPoints.layoutParams.height = 420
                    val heightInPx = dpToPx(120)
                    val layoutParams = binding.recyclerWayPoints.layoutParams
                    layoutParams.height = heightInPx
                    binding.recyclerWayPoints.layoutParams = layoutParams
                } else {
                    val layoutParams = binding.recyclerWayPoints.layoutParams
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    binding.recyclerWayPoints.layoutParams = layoutParams

                }
                adapterWayPoint.submitList(wayPoints)
                adapterWayPoint.notifyDataSetChanged()

            }

        }

        visibleNavigation()
        moveCameraResult()
        symbolManager?.addMarker(coordinates, "")
        binding.startRouteLayout.isVisible = true

    }

    private fun prepareAndStartNavigation(coordinates: LatLng) {
        hideKeyboard()
        destination = coordinates.toPoint()
        wayList.clear()
        wayList.add(
            RoutingWaypoint(
                lat = RouteActivity.origin!!.latitude(),
                lon = RouteActivity.origin!!.longitude()
            )
        )
        wayList.add(
            RoutingWaypoint(
                lat = destination!!.latitude(),
                lon = destination!!.longitude()
            )
        )
        visibleNavigation()
//        mapHelper.moveCameraToResult(map, coordinates)
        moveCameraResult()
        symbolManager?.addMarker(coordinates, "")
        binding.startRouteLayout.isVisible = true

    }

    private fun visibleNavigation() {
        if (RouteActivity.origin == null) {
            showToast("Please add starting point")
        } else if (destination == null) {
            showToast("Please add destination point")
        } else {
            binding.containerNavigation.visibility = View.VISIBLE
            getDirectionsOsrmShortest(
                CostingModel.auto, CostingOptions(
                    auto = AutoCostingOptions(shortest = true)
                )
            )
            getDirectionsOsrm(CostingModel.auto)
        }
    }

    private fun getDirectionsOsrm(costingModel: CostingModel) {
        if (RouteActivity.origin == null && destination == null) {
            showToast("Please select start and end points")
            return
        }

        val directionsOptions = DirectionsOptions(
            DistanceUnit.km,
            ValhallaLanguages.enMinusUS,
            DirectionsOptions.DirectionsType.instructions
        )
        geoHelper.geoMapsApi?.getDirectionsOsrm(locations = wayList,
            costing = costingModel,
            directionsOptions = directionsOptions,
            onSuccess = { routeResponse ->
                val gson = Gson()
                Log.d("routeResponsess", gson.toJson(routeResponse))
                val route = routeResponse.routes.first()
                val points = decodePolyline(route.geometry)
                val lineOptions = LineOptions()
                    .withLatLngs(points)
                    .withLineWidth(6.0f)
                    .withLineColor("#4285F4")
//                    .withLineGapWidth(1.0f)
                    .withLineOffset(1.0f)

                this.routeResponse = routeResponse
                runOnUiThread(Runnable {
                    openBottomSheet(
                        convertAndRoundOff(routeResponse.routes.first().distance).toString(),
                        getMinTime(routeResponse.routes.first().duration.toInt())
                    )
                })
                lifecycleScope.launch {
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            val mapboxPoints: List<Point>
                            if (costingModel == CostingModel.pedestrian) {
                                mapboxPoints = points.map { it.toPoint() }
                            } else {
                                lineManager?.create(lineOptions)
                                mapboxPoints = listOf(points.last().toPoint(), destination!!)
                            }

                            drawDottedLine(mapboxPoints)
                            binding.startRouteLayout.isVisible = true
                        }, 100
                    )

                }
            },
            onError = { error ->
                showToast(error)
            })
    }

    private fun getDirectionsOsrmShortest(
        costingModel: CostingModel,
        costingOptions: CostingOptions
    ) {
        if (RouteActivity.origin == null && destination == null) {
            showToast("Please select start and end points")
            return
        }

        val directionsOptions = DirectionsOptions(
            DistanceUnit.km,
            ValhallaLanguages.enMinusUS,
            DirectionsOptions.DirectionsType.instructions
        )
        geoHelper.geoMapsApi?.getDirectionsOsrm(locations = wayList,
            costing = costingModel,
            directionsOptions = directionsOptions,
            costingOptions = costingOptions,
            onSuccess = { routeResponse ->
                val gson = Gson()
                Log.d("routeResponseShort", gson.toJson(routeResponse))
                val route = routeResponse.routes.first()
                val points = decodePolyline(route.geometry)
                val lineOptions = LineOptions()
                    .withLatLngs(points)
                    .withLineWidth(6.0f)
                    .withLineColor("#008000")
                this.routeResponseShortest = routeResponse

                lifecycleScope.launch {
                    lineManager?.create(lineOptions)
                    drawDottedLineNew(points.last().toPoint())
                    binding.startRouteLayout.isVisible = true

                }
            },
            onError = { error ->
                showToast(error)
            })
    }

    private fun drawDottedLine(route: List<Point>) {
        map.getStyle { style ->
            style.removeLayer(DOTTED_POLYLINE_LAYER_ID)
            style.removeSource(DOTTED_POLYLINE_SOURCE_ID)
        }

        map.getStyle { style ->

//            val lineString = LineString.fromLngLats(listOf(route, destination))
            val lineString = LineString.fromLngLats(route)
            style.addSource(GeoJsonSource(DOTTED_POLYLINE_SOURCE_ID, lineString))
            for (x in wayList.indices) {
                if (wayList.size > 2 && x != 0 && x < wayList.size - 1) {
                    style.addImage(
                        "Marker",
                        convertVectorToBitmap(R.drawable.fullmoon, 44, 44)
                    )


                    symbolManager?.addMarker(
                        (Point.fromLngLat(wayList[x].lon, wayList[x].lat).toLatLng()), "$x"
                    )

                } else if (x == wayList.size - 1) {

                    style.addImage(
                        "myMarker",
                        BitmapFactory.decodeResource(
                            resources,
                            R.drawable.maplibre_marker_icon_default
                        )
                    )
                    symbolManager?.addMarker(
                        (Point.fromLngLat(wayList[x].lon, wayList[x].lat).toLatLng())
                    )

                } else if (x == 0) {
                    style.addImage(
                        "startMarker",
                        convertVectorToBitmap(R.drawable.rounded_blue, 24, 24)
                    )

                    symbolManager?.addMarker(
                        (Point.fromLngLat(wayList[x].lon, wayList[x].lat).toLatLng()),
                        2.0F,
                        "#000000"
                    )

                }
            }
            val lineLayer = LineLayer(DOTTED_POLYLINE_LAYER_ID, DOTTED_POLYLINE_SOURCE_ID).apply {
                setProperties(
                    PropertyFactory.lineDasharray(arrayOf(2f, 2f)),
                    PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                    PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                    PropertyFactory.lineWidth(5f),
                    PropertyFactory.lineColor(Color.BLACK),
                    PropertyFactory.lineTranslate(arrayOf(0f, 2f))
                )
            }

            val sl =
                SymbolLayer(
                    DOTTED_POLYLINE_LAYER_ID,
                    DOTTED_POLYLINE_SOURCE_ID
                ).withProperties(
                    PropertyFactory.iconImage("icon-name"),
                    PropertyFactory.iconSize(1.5f),
                    PropertyFactory.textField("{name}"),
                    PropertyFactory.textColor(Color.BLACK)
                )
            style.addLayer(lineLayer)
//            style.addLayer(sl)
            openPopup = sl
            moveCameraResult()
        }
    }

    private fun getMinTime(totalSecs: Int): String {
        val hours = totalSecs / 3600
        val minutes = (totalSecs % 3600) / 60
        val seconds = totalSecs % 60
        if (hours > 0) return "$hours h $minutes m $seconds s"
        else return "$minutes m $seconds s"
    }

    private fun openBottomSheet(
        optDistance: String,
        optTime: String
    ) {
        binding.bottomView.isVisible = true
        binding.time.text = "$optTime"
        binding.distance.text = "$optDistance km"
    }

    private fun drawDottedLineNew(route: Point) {
        map.getStyle { style ->
            style.removeLayer(DOTTED_POLYLINE_LAYER_ID_NEW)
            style.removeSource(DOTTED_POLYLINE_SOURCE_ID_NEW)
        }
        map.getStyle { style ->
            val lineString = LineString.fromLngLats(listOf(route, destination))
            style.addSource(GeoJsonSource(DOTTED_POLYLINE_SOURCE_ID_NEW, lineString))
            val lineLayer =
                LineLayer(DOTTED_POLYLINE_LAYER_ID_NEW, DOTTED_POLYLINE_SOURCE_ID_NEW).apply {
                    setProperties(
                        PropertyFactory.lineDasharray(arrayOf(0f, 1f)),
                        PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                        PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                        PropertyFactory.lineWidth(5f),
//                        lineColor(Color.parseColor("#008000")),
                        PropertyFactory.lineTranslate(arrayOf(-2f, 1f))
                    )
                }

            style.addLayer(lineLayer)
//            mapHelper.moveCameraToResult(map, origin!!.toLatLng())

            moveCameraResult()


        }
    }

    private fun moveCameraResult() {
        val latLngBounds = LatLngBounds.Builder()
            .includes(wayList.map { LatLng(it.lat, it.lon) })
            .build()

        val newCameraPosition = CameraPosition.Builder()
            .target(latLngBounds.center)
            .zoom(12.0)
            .build()
        map.animateCamera(
            CameraUpdateFactory.newCameraPosition(newCameraPosition),
            2000
        )
    }

    private fun clearPolylines() {

        map.getStyle { style ->
            style.removeLayer(DOTTED_POLYLINE_LAYER_ID)
            style.removeSource(DOTTED_POLYLINE_SOURCE_ID)
        }
        map.getStyle { style ->
            style.removeLayer(DOTTED_POLYLINE_LAYER_ID_NEW)
            style.removeSource(DOTTED_POLYLINE_SOURCE_ID_NEW)
        }
        symbolManager?.deleteAll()
        lineManager?.deleteAll()
    }

    private fun destroyAll() {
        binding.containerNavigation.visibility = View.GONE
        binding.bottomView.visibility = View.GONE
        binding.startRouteLayout.isVisible = false
        hideKeyboard()
        if (origin != null) destination = null
        clearPolylines()
    }

    private fun convertAndRoundOff(meters: Double): Double {
        // Convert meters to kilometers
        val kilometers = meters / 1000

        // Round off to two decimal places
        return String.format("%.2f", kilometers).toDouble()
    }

    private fun convertVectorToBitmap(vectorDrawableId: Int, width: Int, height: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(this, vectorDrawableId)
            ?: throw IllegalArgumentException("Invalid vector drawable ID")
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun showBottomSheetDialog() {
        // Inflate the layout for the Bottom Sheet
        val view: View = layoutInflater.inflate(R.layout.map_style_layout, null)

        // Create the Bottom Sheet Dialog
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(view)

        // Handle interactions within the Bottom Sheet
        val satellite: ImageView = view.findViewById(R.id.satellite_view)
        satellite.setOnClickListener {
            setMapStyle(geoHelper.satelliteUrl)
            bottomSheetDialog.dismiss()
        }
        val default: ImageView = view.findViewById(R.id.default_view)
        default.setOnClickListener {
            setMapStyle(geoHelper.tilesUrl)
            bottomSheetDialog.dismiss()
        }
        val traffic: ImageView = view.findViewById(R.id.traffic)
        val close: ImageView = view.findViewById(R.id.close_style)

        close.setOnClickListener {
            // Handle button click
            bottomSheetDialog.dismiss() // Dismiss the Bottom Sheet Dialog
        }

        // Show the Bottom Sheet Dialog
        bottomSheetDialog.show()
    }
}