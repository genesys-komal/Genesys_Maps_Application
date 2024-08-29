package com.example.mapapplication.navigation

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.window.OnBackInvokedDispatcher
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.mapapplication.originCode.net.models.AutoCostingOptions
import com.example.mapapplication.originCode.net.models.CostingModel
import com.example.mapapplication.originCode.net.models.CostingOptions
import com.example.mapapplication.originCode.net.models.DirectionsOptions
import com.example.mapapplication.originCode.net.models.DistanceUnit
import com.example.mapapplication.originCode.net.models.RouteOsrmResponse
import com.example.mapapplication.originCode.net.models.RoutingWaypoint
import com.example.mapapplication.originCode.net.models.TruckCostingOptions
import com.example.mapapplication.originCode.net.models.ValhallaLanguages
import com.example.mapapplication.MainActivity
import com.example.mapapplication.R
import com.example.mapapplication.activity.FindActivity
import com.example.mapapplication.adapter.ManeuverAdapter
import com.example.mapapplication.adapter.WayPintData
import com.example.mapapplication.adapter.WayPointAdapter
import com.example.mapapplication.base.ViewModelProviderFactory
import com.example.mapapplication.common.Alert
import com.example.mapapplication.common.BaseActivity
import com.example.mapapplication.common.Commons
import com.example.mapapplication.common.Constants
import com.example.mapapplication.common.hideKeyboard
import com.example.mapapplication.common.updateText
import com.example.mapapplication.databinding.AddNewAddressBinding
import com.example.mapapplication.databinding.TurnNavigationActivityBinding
import com.example.mapapplication.helpers.addMarker
import com.example.mapapplication.helpers.decodePolyline
import com.example.mapapplication.helpers.toLatLng
import com.example.mapapplication.helpers.toPoint
import com.example.mapapplication.originCode.net.models.BicycleCostingOptions
import com.example.mapapplication.originCode.net.models.PedestrianCostingOptions
import com.example.mapapplication.originCode.net.models.RouteManeuver
import com.example.mapapplication.originCode.net.models.RouteResponse
import com.example.mapapplication.viewModels.MainViewModel
import com.example.mapapplication.viewModels.RouteViewModel
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
import com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_ROUND
import com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND
import com.mapbox.mapboxsdk.style.layers.Property.VISIBLE
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineDasharray
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineTranslate
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions
import com.mapbox.services.android.navigation.v5.models.DirectionsCriteria
import com.mapbox.services.android.navigation.v5.models.DirectionsResponse
import com.mapbox.services.android.navigation.v5.models.DirectionsRoute
import com.mapbox.services.android.navigation.v5.models.RouteOptions
import kotlinx.coroutines.launch
import okhttp3.internal.userAgent
import kotlin.math.ln


class TurnNavigationActivity : BaseActivity(), OnMapReadyCallback {
    private var openPopup: SymbolLayer? = null // Track the currently open popup
    private val DOTTED_POLYLINE_SOURCE_ID = "DOTTED_POLYLINE_SOURCE_ID"
    private val DOTTED_POLYLINE_LAYER_ID = "DOTTED_POLYLINE_LAYER_ID"
    private val DOTTED_POLYLINE_SOURCE_ID_NEW = "DOTTED_POLYLINE_SOURCE_ID_NEW"
    private val DOTTED_POLYLINE_LAYER_ID_NEW = "DOTTED_POLYLINE_LAYER_ID_NEW"
    private var map: MapboxMap? = null
    private lateinit var binding: TurnNavigationActivityBinding
    private val adapterWayPoint: WayPointAdapter by lazy { WayPointAdapter() }
    private val maneuverAdapter: ManeuverAdapter by lazy { ManeuverAdapter() }
    private lateinit var getResult: ActivityResultLauncher<Intent>
    private lateinit var viewModel: RouteViewModel
    private var symbolManager: SymbolManager? = null
    private var lineManager: LineManager? = null
    private var routeResponse: RouteOsrmResponse? = null
    private var routeDistanceResponse: RouteResponse? = null
    private var routeResponseShortest: RouteOsrmResponse? = null
    private var place = ""
    private var originText = ""
    private var mapUrl = ""
    private var shortest = false
    private var costingModel: CostingModel = CostingModel.auto
    private lateinit var costingOptions: CostingOptions

    private var destination: Point? = null
    private var position: WayPintData? = null
    private var wayIndex: Int? = null

    companion object {
        var origin: Point? = null
        var current: Point? = null
        private var wayList = ArrayList<RoutingWaypoint>()
        private var wayPoints = ArrayList<WayPintData>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TurnNavigationActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.mapView.apply {
            onCreate(savedInstanceState)
            getMapAsync(this@TurnNavigationActivity)
        }
        viewModel =
            ViewModelProvider(this@TurnNavigationActivity, ViewModelProviderFactory(geoHelper)).get(
                RouteViewModel::class.java
            )
        setupObservers()
        costingOptions = CostingOptions(AutoCostingOptions())

        setupView()
    }

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


        optimalRoute.setOnClickListener {
            shortest = false
            optimalRoute.background =
                ContextCompat.getDrawable(this@TurnNavigationActivity, R.drawable.rounded_blue)
            shortestRoute.background =
                ContextCompat.getDrawable(this@TurnNavigationActivity, R.drawable.background_search)
            setCostingOption(costingModel, shortest)
            viewModel.getDirectionOsrm(costingModel, costingOptions, wayList)
            getDirections()

        }
        shortestRoute.setOnClickListener {
            shortest = true
            shortestRoute.background =
                ContextCompat.getDrawable(this@TurnNavigationActivity, R.drawable.rounded_blue)
            optimalRoute.background =
                ContextCompat.getDrawable(this@TurnNavigationActivity, R.drawable.background_search)
            setCostingOption(costingModel, shortest)
            viewModel.getDirectionOsrm(costingModel, costingOptions, wayList)
            getDirections()
        }

        shuffle.setOnClickListener {
            if (validate()) {
                clearPolylines()
                // Swap the text values
                val tempText = originPoint.text
                originPoint.text = destinationPoint.text
                destinationPoint.text = tempText

                // Swap the points
                val tempPoint = destination
                destination = origin
                origin = tempPoint

                // Update the wayList
                wayList[wayList.size - 1] =
                    RoutingWaypoint(lat = destination!!.latitude(), lon = destination!!.longitude())
                wayList[0] = RoutingWaypoint(lat = origin!!.latitude(), lon = origin!!.longitude())

                viewModel.getDirectionOsrm(costingModel, costingOptions, wayList)
                getDirections()
            }

        }

        fabRecenter.setOnClickListener {
            moveCameraResult()
        }
        fabStyles.setOnClickListener {
            showBottomSheetDialog()
        }
        destinationPoint.setOnClickListener {
            place = Commons.destination
            startNewActivity(destinationPoint.text.toString())
        }
        originPoint.setOnClickListener {
            place = Commons.origin
            startNewActivity(originText)
        }
//to clear origin data
        clearOrigin.setOnClickListener {
//            originPoint.text = ""
//            clearPolylines()
//            origin = null
//            bottomView.isVisible = false
            try {
                openOptionsMenu()
            } catch (e: Exception) {
                showToast(e.message.toString())
            }
        }
        //to remove destination data
        clearDestination.setOnClickListener {
            if (destinationPoint.text != "") {
                destinationPoint.text = ""
                clearPolylines()
                destination = null
                bottomView.isVisible = false
                wayList.removeAt(wayList.size - 1)
            }
        }
        //to clear all the data on the map
        clearPoints.setOnClickListener {
            destroyAll()
            startActivity(Intent(this@TurnNavigationActivity, MainActivity::class.java))
            finish()
        }
        //to plot route for pedestrian view click
        walkLayout.setOnClickListener {
            if (validate()) {
                costingModel = CostingModel.pedestrian
                costingOptions =
                    CostingOptions(pedestrian = PedestrianCostingOptions(shortest = shortest))
                viewModel.getDirectionOsrm(costingModel, costingOptions, wayList)
                getDirections()
                walkLayout.background =
                    ContextCompat.getDrawable(this@TurnNavigationActivity, R.drawable.rounded_blue)
                busLayout.background = ContextCompat.getDrawable(
                    this@TurnNavigationActivity,
                    R.drawable.background_map_search
                )
                carLayout.background = ContextCompat.getDrawable(
                    this@TurnNavigationActivity,
                    R.drawable.background_map_search
                )
                truckLayout.background = ContextCompat.getDrawable(
                    this@TurnNavigationActivity,
                    R.drawable.background_map_search
                )
                bicycleLayout.background = ContextCompat.getDrawable(
                    this@TurnNavigationActivity,
                    R.drawable.background_map_search
                )

            }
        }
        busLayout.setOnClickListener {
            if (validate()) {
                costingModel = CostingModel.bus
                costingOptions = CostingOptions(AutoCostingOptions(shortest = shortest))
                viewModel.getDirectionOsrm(costingModel, costingOptions, wayList)
                getDirections()

                walkLayout.background = ContextCompat.getDrawable(
                    this@TurnNavigationActivity,
                    R.drawable.background_map_search
                )
                busLayout.background =
                    ContextCompat.getDrawable(this@TurnNavigationActivity, R.drawable.rounded_blue)
                carLayout.background = ContextCompat.getDrawable(
                    this@TurnNavigationActivity,
                    R.drawable.background_map_search
                )
                truckLayout.background = ContextCompat.getDrawable(
                    this@TurnNavigationActivity,
                    R.drawable.background_map_search
                )
                bicycleLayout.background = ContextCompat.getDrawable(
                    this@TurnNavigationActivity,
                    R.drawable.background_map_search
                )
            }
        }
        binding.truckLayout.setOnClickListener {
            if (validate()) {
                costingModel = CostingModel.truck
                costingOptions = CostingOptions(truck = TruckCostingOptions(shortest = shortest))
                viewModel.getDirectionOsrm(costingModel, costingOptions, wayList)
                getDirections()
                walkLayout.background = ContextCompat.getDrawable(
                    this@TurnNavigationActivity,
                    R.drawable.background_map_search
                )
                busLayout.background = ContextCompat.getDrawable(
                    this@TurnNavigationActivity,
                    R.drawable.background_map_search
                )
                carLayout.background = ContextCompat.getDrawable(
                    this@TurnNavigationActivity,
                    R.drawable.background_map_search
                )
                truckLayout.background =
                    ContextCompat.getDrawable(this@TurnNavigationActivity, R.drawable.rounded_blue)
                bicycleLayout.background = ContextCompat.getDrawable(
                    this@TurnNavigationActivity,
                    R.drawable.background_map_search
                )
            }
        }
        bicycleLayout.setOnClickListener {
            if (validate()) {
                costingModel = CostingModel.bicycle
                costingOptions =
                    CostingOptions(bicycle = BicycleCostingOptions(shortest = shortest))
                viewModel.getDirectionOsrm(costingModel, costingOptions, wayList)
                getDirections()
                walkLayout.background = ContextCompat.getDrawable(
                    this@TurnNavigationActivity,
                    R.drawable.background_map_search
                )
                busLayout.background = ContextCompat.getDrawable(
                    this@TurnNavigationActivity,
                    R.drawable.background_map_search
                )
                carLayout.background = ContextCompat.getDrawable(
                    this@TurnNavigationActivity,
                    R.drawable.background_map_search
                )
                truckLayout.background = ContextCompat.getDrawable(
                    this@TurnNavigationActivity,
                    R.drawable.background_map_search
                )
                bicycleLayout.background =
                    ContextCompat.getDrawable(this@TurnNavigationActivity, R.drawable.rounded_blue)
            }
        }
        carLayout.setOnClickListener {
            if (validate()) {
                costingModel = CostingModel.auto
                costingOptions = CostingOptions(auto = AutoCostingOptions(shortest = shortest))
                viewModel.getDirectionOsrm(costingModel, costingOptions, wayList)
                getDirections()
                walkLayout.background = ContextCompat.getDrawable(
                    this@TurnNavigationActivity,
                    R.drawable.background_map_search
                )
                busLayout.background = ContextCompat.getDrawable(
                    this@TurnNavigationActivity,
                    R.drawable.background_map_search
                )
                carLayout.background =
                    ContextCompat.getDrawable(this@TurnNavigationActivity, R.drawable.rounded_blue)
                truckLayout.background = ContextCompat.getDrawable(
                    this@TurnNavigationActivity,
                    R.drawable.background_map_search
                )
                bicycleLayout.background = ContextCompat.getDrawable(
                    this@TurnNavigationActivity,
                    R.drawable.background_map_search
                )
            }
        }

//        addWayPoint.setOnClickListener {
//            position = null
//            place = Commons.wayPoint
//            startNewActivity(originPoint.text.toString())
//        }
        adapterWayPoint.setOnItemClicked { prediction ->

            val wPoint =
                RoutingWaypoint(lat = prediction.point.latitude, lon = prediction.point.longitude)
            if (wayList.contains(wPoint)) {
//                showToast("node deleted")
                wayList.remove(wPoint)

            }

            wayPoints.remove(prediction)
            if (wayPoints.isEmpty()) {
                binding.recyclerWayPoints.isVisible = false
                binding.shuffle.isVisible = true
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
            viewModel.getDirectionOsrm(costingModel, costingOptions, wayList)
            getDirections()
        }
        adapterWayPoint.setOnTextClicked {
            position = it
            place = Commons.wayPoint
            startNewActivity(it.description)
        }
    }

    override fun onMapReady(map: MapboxMap) {
        this.map = map
        setMapStyle(geoHelper.tilesUrl)

//        setupLegend()
        map.addOnMapClickListener { latLng ->
            AddNewAddressDialog(latLng)
//            changeRoute(latLng)
            /*         if (destination != null) {

                       *//*  Alert.alert {
                    title = "Put point"
                    description = "Add as via point"
                    alertContext = this@TurnNavigationActivity
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
                                    lat = origin!!.latitude(),
                                    lon = origin!!.longitude()
                                )
                            )
                            wayList.add(
                                RoutingWaypoint(
                                    lat = destination!!.latitude(),
                                    lon = destination!!.longitude()
                                )
                            )
                        }

                        place = Commons.wayPoint
                        binding.clearPoints.isVisible = true
                        getReverseGeocode(latLng)

                    }
                    negativeButton = {}
                }*//*

                true
            }*/ //else false
            true
        }
        map.addOnMapLongClickListener { latLng ->
//            showToast("hihjkjbnbmbnm  $latLng")

            val screenPoint = map.projection.toScreenLocation(latLng)
            val features = map.queryRenderedFeatures(screenPoint, "line-layer")
            if (features.isNotEmpty()) {
                val feature = features[0]
                showToast("You clicked on the polyline")
            }
            true
        }
    }

    private fun setupObservers() {
        viewModel.routeOsrmResponse.observe(this) { result ->
            result?.let {

                this@TurnNavigationActivity.routeResponse = result
                drawDottedLine()

            }
        }

        viewModel.routeResponse.observe(this) { result ->
            result?.let {

                this.routeDistanceResponse = result
                drawDottedLine()

            }
        }

        viewModel.loadingState.observe(this) { isLoading ->
            // Update UI to show/hide loading spinner
        }

        viewModel.errorState.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    //on map click listener dialog
    private fun AddNewAddressDialog(latLng: LatLng) {
        // Inflate the custom layout
        val inflater = LayoutInflater.from(this)
        val dialogView: View = inflater.inflate(R.layout.add_new_address, null)

        // Initialize the dialog builder
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val alertDialog = dialogBuilder.create()

        val directionFrom: TextView = dialogView.findViewById(R.id.directionFrom)
        val directionTo: TextView = dialogView.findViewById(R.id.directionTo)
        val addWayPoint: TextView = dialogView.findViewById(R.id.addWayPoint)

        // Set up the direction from action
        directionFrom.setOnClickListener {
            origin = latLng.toPoint()
            place = Commons.origin
            getReverseGeocode(latLng)
            alertDialog.dismiss()
            wayList[0] = RoutingWaypoint(lat = origin!!.latitude(), lon = origin!!.longitude())
        }
        // Set up the direction to action
        directionTo.setOnClickListener {
            destination = latLng.toPoint()
            wayList[wayList.size - 1] =
                RoutingWaypoint(lat = destination!!.latitude(), lon = destination!!.longitude())
            place = Commons.destination
            getReverseGeocode(latLng)
            alertDialog.dismiss()
        }
        // Set up the add way point action
        addWayPoint.setOnClickListener {
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
                        lat = origin!!.latitude(),
                        lon = origin!!.longitude()
                    )
                )
                wayList.add(
                    RoutingWaypoint(
                        lat = destination!!.latitude(),
                        lon = destination!!.longitude()
                    )
                )
            }

            place = Commons.wayPoint
            binding.clearPoints.isVisible = true
            getReverseGeocode(latLng)

            alertDialog.dismiss()
        }

        // Show the dialog
        alertDialog.show()
    }

    private fun setMapStyle(style: String) {
        map?.setStyle(style) { style ->
            clearPolylines()
            lineManager = LineManager(binding.mapView, map!!, style)
            lineManager?.deleteAll()
            symbolManager = SymbolManager(binding.mapView, map!!, style).apply {
                iconAllowOverlap = true
                textAllowOverlap = true
            }
            style.addImage(
                "myMarker",
                convertVectorToBitmap(R.drawable.nav_icon, 84, 84)
            )
            if (routeResponse != null) {
                clearPolylines()
            }
            getLocation(style)

        }
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
            locationHelper.setupLocationComponent(map?.locationComponent!!, style)
            if (origin == null || origin == current) {
                origin = Point.fromLngLat(
                    locationHelper.lastLocation?.longitude ?: 0.0,
                    locationHelper.lastLocation?.latitude ?: 0.0
                )
                current = origin
            }


            val ss: LatLng? = intent.getParcelableExtra<LatLng>("address")
            val ss1 = intent.getStringExtra(Commons.place)
            originText = ss1.toString()
            if (!ss1.isNullOrBlank())
                binding.originPoint.text = ss1.toString()
            if (ss != null) {
                place = Commons.destination
                getReverseGeocode(ss)
                prepareAndStartNavigation(ss)
            }
        }
    }

    /*  private fun getDirectionsOsrm() {
          if (origin == null && destination == null) {
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

                  runOnUiThread {
                      val gson = Gson()
                      Log.d("routeResponsess", gson.toJson(routeResponse))
                      this@TurnNavigationActivity.routeResponse = routeResponse
                      drawDottedLine()
                  }

              },
              onError = { error ->
                  showToast(error)
              })
      }*/

    private fun getDirections() {
        if (origin == null && destination == null) {
            showToast("Please select start and end points")
            return
        }

        val directionsOptions = DirectionsOptions(
            DistanceUnit.km,
            ValhallaLanguages.enMinusUS,
            DirectionsOptions.DirectionsType.instructions
        )
        geoHelper.geoMapsApi?.getDirections(locations = wayList,
            costing = costingModel,
            directionsOptions = directionsOptions,
            costingOptions = costingOptions,
            onSuccess = { routeResponse ->
                val gson = Gson()
                Log.d("routeResponsess", gson.toJson(routeResponse))
                val route = routeResponse
                routeDistanceResponse = routeResponse
            },
            onError = { error ->
                showToast(error)
            })
    }

    private fun drawDottedLine() {
        runOnUiThread(Runnable {
            clearPolylines()
            val route = routeResponse?.routes?.first()
            val points = decodePolyline(route?.geometry!!)
            map?.getStyle { style ->
                style.removeLayer(DOTTED_POLYLINE_LAYER_ID)
                style.removeSource(DOTTED_POLYLINE_SOURCE_ID)
            }
            val lineOptions = LineOptions()
                .withLatLngs(points)
                .withLineWidth(6.0f)
                .withLineColor("#4285F4")
//                    .withLineGapWidth(1.0f)
                .withLineOffset(1.0f)



            openBottomSheet(
                convertAndRoundOff(routeResponse?.routes?.first()?.distance!!).toString(),
                getMinTime(routeResponse?.routes?.first()?.duration!!.toInt())
            )
            var mapboxPoints: List<Point>? = null
            lifecycleScope.launch {


                if (costingModel == CostingModel.pedestrian) {
                    mapboxPoints = points.map { it.toPoint() }
                } else {
                    lineManager?.create(lineOptions)
                    mapboxPoints = listOf(points.last().toPoint(), destination!!)
                }
                binding.startRouteLayout.isVisible = true
            }
            map?.getStyle { style ->

//            val lineString = LineString.fromLngLats(listOf(route, destination))
                val lineString = LineString.fromLngLats(mapboxPoints!!)
                style.addSource(GeoJsonSource(DOTTED_POLYLINE_SOURCE_ID, lineString))
                if (wayList.isNotEmpty()) {
                    for (x in wayList.indices) {
                        style.addImage(
                            "Marker",
                            convertVectorToBitmap(R.drawable.nav_icon, 84, 84)
                        )


                        symbolManager?.addMarker(
                            (Point.fromLngLat(wayList[x].lon, wayList[x].lat).toLatLng()),
                            "${x + 1}"
                        )
                        /*if (wayList.size > 2 && x != 0 && x < wayList.size - 1) {
                            style.addImage(
                                "Marker",
                                convertVectorToBitmap(R.drawable.nav_icon, 44, 44)
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

                        }*/
                    }
                }
                val lineLayer =
                    LineLayer(DOTTED_POLYLINE_LAYER_ID, DOTTED_POLYLINE_SOURCE_ID).apply {
                        setProperties(
                            lineDasharray(arrayOf(2f, 2f)),
                            lineCap(LINE_CAP_ROUND),
                            lineJoin(LINE_JOIN_ROUND),
                            lineWidth(5f),
                            lineColor(Color.BLACK),
                            lineTranslate(arrayOf(0f, 2f))
                        )
                    }

                val sl =
                    SymbolLayer(DOTTED_POLYLINE_LAYER_ID, DOTTED_POLYLINE_SOURCE_ID).withProperties(
                        PropertyFactory.iconImage("icon-name"),
                        PropertyFactory.iconSize(1.5f),
                        PropertyFactory.textField("{name}"),
                        PropertyFactory.textColor(Color.BLACK)
                    )
                style.addLayer(lineLayer)
//            style.addLayer(sl)
                openPopup = sl
                moveCameraResult()
//                flyCameraToBounds()
            }
        })
    }

    private fun drawDottedLineNew(route: Point) {
        map?.getStyle { style ->
            style.removeLayer(DOTTED_POLYLINE_LAYER_ID_NEW)
            style.removeSource(DOTTED_POLYLINE_SOURCE_ID_NEW)
        }
        map?.getStyle { style ->
            val lineString = LineString.fromLngLats(listOf(route, destination))
            style.addSource(GeoJsonSource(DOTTED_POLYLINE_SOURCE_ID_NEW, lineString))
            val lineLayer =
                LineLayer(DOTTED_POLYLINE_LAYER_ID_NEW, DOTTED_POLYLINE_SOURCE_ID_NEW).apply {
                    setProperties(
                        lineDasharray(arrayOf(0f, 1f)),
                        lineCap(LINE_CAP_ROUND),
                        lineJoin(LINE_JOIN_ROUND),
                        lineWidth(5f),
//                        lineColor(Color.parseColor("#008000")),
                        lineTranslate(arrayOf(-2f, 1f))
                    )
                }

            style.addLayer(lineLayer)
//            mapHelper.moveCameraToResult(map, origin!!.toLatLng())

            moveCameraResult()


        }
    }

    private fun startNavigation(routeResponse: RouteOsrmResponse) {
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
            .baseUrl(geoHelper.apiUrl) // fake url
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
                CameraPosition.Builder().target(origin?.toLatLng()).build()
            )
            .build()
        NavigationLauncher.startNavigation(this@TurnNavigationActivity, options)
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
                    } else {
                        val properties = peliasResponse.features.first().properties
                        val label = properties?.get("label")

                        if (place == Commons.origin) {
                            originText = label.toString()
                            binding.originPoint.text = label.toString()
                        } else if (place == Commons.destination) {
                            binding.destinationPoint.text = label.toString()
                        } else if (place == Commons.wayPoint) {
                            if (wayPoints.isEmpty()) wayPoints.add(
                                WayPintData(
                                    label.toString(),
                                    latLng,
                                    0
                                )
                            )
                            else wayPoints.add(WayPintData(label.toString(), latLng, wayList.size))
                            binding.recyclerWayPoints.isVisible = true
                            binding.shuffle.isVisible = false
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
                        visibleNavigation()
                    }
                }
            },
            onError = { errorMsg ->
                showToast(errorMsg)
            }
        )
    }

    private fun flyCameraToBounds() {
        if (origin == null && destination == null) return
        val latLngList = listOf(origin!!.toLatLng(), destination!!.toLatLng())
        val latLngBounds = LatLngBounds.fromLatLngs(latLngList)
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 200)
        map?.easeCamera(cameraUpdate)
    }

    private fun getForwardGeocode(text: String) {
        geoHelper.geoMapsApi?.getForwardGeocode(
            text = text,
            onSuccess = { peliasResponse ->
                val coordinates = peliasResponse.features.first().geometry.coordinates.toLatLng()
                lifecycleScope.launch {

                    prepareAndStartNavigationNew(coordinates, text)

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
                    prepareAndStartNavigationNew(coordinates, label.toString())
                }
            },
            onError = { errorMsg ->
                showToast(errorMsg)
            }
        )
    }

    private fun prepareAndStartNavigation(coordinates: LatLng) {
        hideKeyboard()
        destination = coordinates.toPoint()
        wayList.clear()
        wayList.add(
            RoutingWaypoint(
                lat = origin!!.latitude(),
                lon = origin!!.longitude()
            )
        )
        wayList.add(
            RoutingWaypoint(
                lat = destination!!.latitude(),
                lon = destination!!.longitude()
            )
        )
        visibleNavigation()
        binding.startRouteLayout.isVisible = true

    }

    private fun prepareAndStartNavigationNew(
        coordinates: LatLng,
        description: String
    ) {
        clearPolylines()
        hideKeyboard()
        when (place) {
            Commons.origin -> {
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

                wayList.add(
                    RoutingWaypoint(
                        lat = destination!!.latitude(),
                        lon = destination!!.longitude()
                    )
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
                binding.recyclerWayPoints.isVisible = true
                binding.shuffle.isVisible = false
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
        binding.startRouteLayout.isVisible = true
        visibleNavigation()
    }

    private fun destroyAll() {
        binding.containerNavigation.visibility = View.GONE
        binding.bottomView.visibility = View.GONE
        binding.startRouteLayout.isVisible = false
        hideKeyboard()
        clearPolylines()
    }

    private fun visibleNavigation() {
        if (origin == null) {
            showToast("Please add starting point")
        } else if (destination == null) {
            showToast("Please add destination point")
        } else {
            binding.containerNavigation.visibility = View.VISIBLE
            viewModel.getDirectionOsrm(costingModel, costingOptions, wayList)
            getDirections()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//            // Inflate the menu resource into the Menu object
//
//            return true
//        }
        menuInflater.inflate(R.menu.turn_side_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle menu item clicks
        return when (item.itemId) {
            R.id.action_shortest -> {
                shortest = true
                setCostingOption(costingModel, shortest)
                viewModel.getDirectionOsrm(costingModel, costingOptions, wayList)
                getDirections()
                true
            }

            R.id.settings -> {
                // Handle about action
                true
            }

            R.id.add_stop -> {
                position = null
                place = Commons.wayPoint
                startNewActivity("")
                true
            }

            R.id.show_maneuver -> {
                showManeuverDialog(routeDistanceResponse?.trip?.legs?.first()?.maneuvers!!)
                true
            }

            R.id.share_location -> {
                // Handle about action
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
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

    private fun convertVectorToBitmap(vectorDrawableId: Int, width: Int, height: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(this, vectorDrawableId)
            ?: throw IllegalArgumentException("Invalid vector drawable ID")
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun convertAndRoundOff(meters: Double): Double {
        val kilometers = meters / 1000

        return String.format("%.2f", kilometers).toDouble()
    }

    private fun validate(): Boolean {
        if (origin == null) {
            showToast("Please select start points")
            return false
        } else if (destination == null) {
            showToast("Please select end points")
            return false
        }
        return true
    }

    private fun startNewActivity(place: String) {
        val intent = Intent(this, FindActivity::class.java)
        intent.putExtra("place", place)
//        intent.putExtra("origin", origins)
        getResult.launch(intent)

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
                    binding.originPoint.text = resultData.toString()

                } else if (place == Commons.destination) {
                    binding.destinationPoint.text = resultData.toString()
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
                    getForwardGeocode(resultData.toString())
                } else {
                    getPlaceDetails(listOf(placeID), place)
                }
                // Handle the result here, e.g., update UI
            }
        }

    }

    private fun moveCameraResult() {
        if (MainActivity.isThreeD) {
            enable3DView()
        } else {
            val latLngBounds = LatLngBounds.Builder()
                .includes(wayList.map { LatLng(it.lat, it.lon) })
                .build()
            val newCameraPosition = CameraPosition.Builder()
                .target(latLngBounds.center)
                .zoom(
                    calculateZoomLevel(
                        latLngBounds,
                        binding.mapView.width,
                        binding.mapView.height
                    ) - 1
                )
                .build()
            map?.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition), 100)
//        map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100))
        }

    }

    private fun calculateZoomLevel(
        latLngBounds: LatLngBounds,
        mapWidth: Int,
        mapHeight: Int
    ): Double {
        val mapCenter = latLngBounds.center
        val northeast = latLngBounds.northEast
        val southwest = latLngBounds.southWest

        val latSpan = northeast.latitude - southwest.latitude
        val lonSpan = northeast.longitude - southwest.longitude

        val latSpanPixels = latSpan * mapHeight / 256
        val lonSpanPixels = lonSpan * mapWidth / 256

        return Math.min(
            ln(mapWidth / lonSpanPixels) / ln(2.0),
            ln(mapHeight / latSpanPixels) / ln(2.0)
        )
    }

    private fun clearPolylines() {

        map?.getStyle { style ->
            style.removeLayer(DOTTED_POLYLINE_LAYER_ID)
            style.removeSource(DOTTED_POLYLINE_SOURCE_ID)
        }
        lineManager?.deleteAll()
        symbolManager?.deleteAll()

    }

    private fun showBottomSheetDialog() {
        val view: View = layoutInflater.inflate(R.layout.map_style_layout, null)

        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(view)

        val satellite: ImageView = view.findViewById(R.id.satellite_view)
        satellite.setOnClickListener {
            bottomSheetDialog.dismiss()
            clearPolylines()
            map?.setStyle(geoHelper.satelliteUrl) { style ->
                clearPolylines()
                lineManager = LineManager(binding.mapView, map!!, style)
                symbolManager = SymbolManager(binding.mapView, map!!, style).apply {
                    iconAllowOverlap = true
                    textAllowOverlap = true
                }
            }
            viewModel.getDirectionOsrm(costingModel, costingOptions, wayList)
            getDirections()
        }
        val default: ImageView = view.findViewById(R.id.default_view)
        default.setOnClickListener {
            bottomSheetDialog.dismiss()
            clearPolylines()
            map?.setStyle(geoHelper.tilesUrl) { style ->
                clearPolylines()
                lineManager = LineManager(binding.mapView, map!!, style)
                symbolManager = SymbolManager(binding.mapView, map!!, style).apply {
                    iconAllowOverlap = true
                    textAllowOverlap = true
                }
            }
            viewModel.getDirectionOsrm(costingModel, costingOptions, wayList)
            getDirections()
        }
        val traffic: ImageView = view.findViewById(R.id.traffic)

        val threeD: ImageView = view.findViewById(R.id.other)
        val threelyt: LinearLayout = view.findViewById(R.id.threeDLyt)
        if (MainActivity.isThreeD) {
            threelyt.background = ContextCompat.getDrawable(this, R.drawable.background_search)
        }
        threeD.setOnClickListener {

            if (threelyt.background == null) {
                enable3DView()
                MainActivity.isThreeD = true
                threelyt.background = ContextCompat.getDrawable(this, R.drawable.background_search)
                bottomSheetDialog.dismiss()
            } else {
                MainActivity.isThreeD = false
                threelyt.background = null
                val newCameraPosition = CameraPosition.Builder()
                    .tilt(0.0) // Set tilt angle to 0 to make it 2D
                    .zoom(12.0) // Adjust zoom level as needed
                    .build()

                map?.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition))
                bottomSheetDialog.dismiss()
            }
        }
        val close: ImageView = view.findViewById(R.id.close_style)

        close.setOnClickListener {
            // Handle button click
            bottomSheetDialog.dismiss() // Dismiss the Bottom Sheet Dialog
        }

        // Show the Bottom Sheet Dialog
        bottomSheetDialog.show()
    }

    private fun enable3DView() {

        val newCameraPosition = CameraPosition.Builder()
            .tilt(60.0) // Tilt angle to make it 3D
            .zoom(14.0) // Adjust zoom level
            .build()


        map?.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition), 100)
    }

    private fun showManeuverDialog(maneuver: List<RouteManeuver>) {
        val view: View = layoutInflater.inflate(R.layout.layout_maneuvers, null)

        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(view)

        val satellite: RecyclerView = view.findViewById(R.id.recycler_maneuver)
        satellite.adapter = maneuverAdapter
        maneuverAdapter.submitList(maneuver)
        maneuverAdapter.setOnItemClicked {

        }

        val close: ImageView = view.findViewById(R.id.close_style)

        close.setOnClickListener {
            // Handle button click
            bottomSheetDialog.dismiss() // Dismiss the Bottom Sheet Dialog
        }

        // Show the Bottom Sheet Dialog
        bottomSheetDialog.show()
    }
//    private fun addMarkers() {
//        routeResponse
//        val lineString = LineString.fromJson("{your-polyline-geojson-string}")
//        val beginShapeIndex = 0  // Replace with your actual index
//        val endShapeIndex = lineString.coordinates().size - 1  // Replace with your actual index
//
//        val coordinates = lineString.coordinates()
//        val startPoint = coordinates[beginShapeIndex]
//        val endPoint = coordinates[endShapeIndex]
//
//        addMarkerAtPoint(startPoint)
//        addMarkerAtPoint(endPoint)
//    }
    private fun setupLegend() {
        val legendContainer: LinearLayout = findViewById(R.id.legendContainer)

        val lineLegendItem = TextView(this).apply {
            text = "Genesys International Corporation Ltd"
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(8, 8, 8, 8)
            setOnClickListener {
                onLegendItemClick(DOTTED_POLYLINE_LAYER_ID)
            }
        }

        legendContainer.addView(lineLegendItem)
    }

    private fun onLegendItemClick(layerId: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.igenesys.com/"))
        startActivity(intent)
        val layer = map?.style?.getLayer(layerId)
        if (layer != null) {
            val visibility = layer.visibility
            layer.setProperties(
                PropertyFactory.visibility(
                    VISIBLE
//                    if (visibility == VISIBLE) NONE else VISIBLE

                )
            )
        }

    }

    override fun getOnBackInvokedDispatcher(): OnBackInvokedDispatcher {
        return super.getOnBackInvokedDispatcher()
        finish()
    }

    private fun setCostingOption(costingModel: CostingModel, shortest: Boolean): CostingOptions {
        costingOptions = when (costingModel) {
            CostingModel.auto -> CostingOptions(auto = AutoCostingOptions(shortest = shortest))
            CostingModel.bicycle -> CostingOptions(bicycle = BicycleCostingOptions(shortest = shortest))
            CostingModel.bus -> CostingOptions(auto = AutoCostingOptions(shortest = shortest))
            CostingModel.truck -> CostingOptions(truck = TruckCostingOptions(shortest = shortest))
            CostingModel.pedestrian -> CostingOptions(pedestrian = PedestrianCostingOptions(shortest = shortest))
            else -> throw IllegalArgumentException("Unknown CostingModel: $costingModel")
        }

        return costingOptions
    }

}
