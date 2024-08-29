package com.example.mapapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.mapapplication.activity.IsochroneActivity
import com.example.mapapplication.originCode.net.models.Coordinate
import com.example.mapapplication.adapter.CategoryAdapter
import com.example.mapapplication.adapter.WayPintData
import com.example.mapapplication.base.ViewModelProviderFactory
import com.example.mapapplication.common.BaseActivity
import com.example.mapapplication.common.Commons
import com.example.mapapplication.common.doOnTextChangedWithDebounce
import com.example.mapapplication.common.hideKeyboard
import com.example.mapapplication.common.showKeyboard
import com.example.mapapplication.common.updateText
import com.example.mapapplication.common.vibrateDevice
import com.example.mapapplication.databinding.ActivityMainBinding
import com.example.mapapplication.helpers.GeoHelper
import com.example.mapapplication.helpers.addMarker
import com.example.mapapplication.helpers.toLatLng
import com.example.mapapplication.models.CategoryList
import com.example.mapapplication.navigation.TurnNavigationActivity
import com.example.mapapplication.originCode.GeoMapsConfiguration
import com.example.mapapplication.originCode.net.models.RoutingWaypoint
import com.example.mapapplication.viewModels.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.permissions.PermissionsListener
import com.mapbox.mapboxsdk.location.permissions.PermissionsManager
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.LineManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.expressions.Expression.get
import com.mapbox.mapboxsdk.style.expressions.Expression.match
import com.mapbox.mapboxsdk.style.expressions.Expression.stop
import com.mapbox.mapboxsdk.style.expressions.Expression.zoom
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer
import com.mapbox.mapboxsdk.style.layers.Layer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth
import com.mapbox.mapboxsdk.style.layers.RasterLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.style.sources.RasterDemSource
import com.mapbox.mapboxsdk.style.sources.RasterSource
import com.mapbox.mapboxsdk.style.sources.Source
import com.mapbox.mapboxsdk.style.sources.VectorSource
import com.mapbox.turf.TurfMeasurement.center
import kotlinx.coroutines.launch
import java.lang.System.setProperties
import java.util.regex.Pattern

class MainActivity : BaseActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMainBinding
    private var lineManager: LineManager? = null
    private var origin: Point? = null
    private var category = arrayListOf<CategoryList>()

    private lateinit var  viewModel: MainViewModel
    private var map: MapboxMap? = null
    private var points: LatLng? = null
    private var place = ""

    private var originText = ""
    private var catName: String = ""
    private var symbolManager: SymbolManager? = null
    private val categoryAdapter: CategoryAdapter by lazy { CategoryAdapter() }
    companion object {
        public var isThreeD = false
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.mapView.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelProviderFactory(geoHelper)).get(MainViewModel::class.java)
        checkPermissions()
        setupObservers()
        setupView()
        initView()
    }

    private fun initView() {
        binding.fabGetDirection.setOnClickListener {
            if (points != null) {
                startTurnActivity(points!!)
            } else {
                showToast("Please choose a destination point")
            }
        }
        binding.isochrone.setOnClickListener {
            if (points != null) {
                val intent = Intent(this, IsochroneActivity::class.java)
                intent.putExtra(Commons.address, points)
                intent.putExtra(Commons.place, points)
                startActivity(intent)
            } else {
                showToast("Please choose a destination point")
            }
        }
    }

    private fun startTurnActivity(point: LatLng) {
        val intent = Intent(this, TurnNavigationActivity::class.java)
        intent.putExtra("address", point)
        intent.putExtra(Commons.place, originText)
        startActivity(intent)
    }

    private fun checkPermissions() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            binding.mapView.getMapAsync(this)
        } else {
            locationHelper.permissionsManager = PermissionsManager(object : PermissionsListener {
                override fun onExplanationNeeded(permissionsToExplain: List<String>) {
                    Toast.makeText(
                        this@MainActivity,
                        "You need to accept location permissions.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onPermissionResult(granted: Boolean) {
                    if (granted) {
                        binding.mapView.getMapAsync(this@MainActivity)
                    } else {
                        finish()
                    }
                }
            })

            locationHelper.permissionsManager?.requestLocationPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationHelper.permissionsManager
            ?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onMapReady(map: MapboxMap) {
        this.map = map
        setMapStyle(geoHelper.tilesUrl, map)

    }

    private fun setMapStyle(styles: String, map: MapboxMap) {
        map?.setStyle(styles) { style ->
//            val overlayView: CustomOverlayView = CustomOverlayView(this, map)
//            binding.mapView.addView(overlayView)
//            locationHelper.mockLocation(map)
            lineManager = LineManager(binding.mapView, map, style)
            symbolManager = SymbolManager(binding.mapView, map, style).apply {
                iconAllowOverlap = true
            }


            style.addImage(
                "myMarker",
                BitmapFactory.decodeResource(resources, R.drawable.maplibre_marker_icon_default)
            )


            map.addOnMapClickListener { latLng ->
                vibrateDevice()
                points = latLng

                place = Commons.destination
                getReverseGeocode(latLng)
//                getNearestRoads(latLng)
                true
            }
            getLocation(style)
        }

    }
    private fun setupObservers() {
        viewModel.autoCompleteResult.observe(this) { result ->
            result?.let {

                        binding.recyclerViewSearch.isVisible = true
//                    binding.recyclerViewSearch.isVisible = autoCompleteResponse.predictions.isNotEmpty()
                        adapter.submitList(it.predictions)

                        if (it.predictions.isEmpty()) {
                            showToast("No results found")
                        }

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
            locationHelper.setupLocationComponent(map!!.locationComponent, style)

            origin = Point.fromLngLat(
                locationHelper.lastLocation?.longitude ?: 0.0,
                locationHelper.lastLocation?.latitude ?: 0.0
            )
            place = Commons.origin
            origin?.toLatLng()?.let { getReverseGeocode(it) }
        }
    }

    private fun setupView() = binding.apply {
        category.add(
            CategoryList(
                "Health",
                resources.getDrawable(R.drawable.baseline_local_hospital_24)
            )
        )
        category.add(
            CategoryList(
                "Education",
                resources.getDrawable(R.drawable.baseline_menu_book_24)
            )
        )
        category.add(
            CategoryList(
                "Transport",
                resources.getDrawable(R.drawable.baseline_directions_bus_24)
            )
        )
        category.add(CategoryList("Accommodation", resources.getDrawable(R.drawable.baseline_house_siding_24)))


        recyclerCategory.adapter = categoryAdapter
        categoryAdapter.submitList(category)
        categoryAdapter.setOnItemClicked { prediction ->
            symbolManager?.deleteAll()
            binding.editTextSearch.text.clear()
            binding.bottomView.isVisible = false
            recyclerViewSearch.isVisible = false
            catName = prediction.name.lowercase()
        }

        recyclerViewSearch.adapter = adapter
        imageViewClose.setOnClickListener {
            imageViewClose.isVisible = false
            imageSearch.isVisible = true
            symbolManager?.deleteAll()
            editTextSearch.text.clear()
            bottomView.isVisible = false
            binding.imageViewClose.isVisible = false
            editTextSearch.clearFocus()
            adapter.submitList(emptyList())
            recyclerViewSearch.isVisible = false
            hideKeyboard()
        }

        imageSearch.setOnClickListener {
            if (!editTextSearch.text.isNullOrEmpty()) {
                getReverseGeocode(points!!)
//                getNearestRoads(points!!)
            } else {
                showToast("Please enter destination")
            }
        }

        editTextSearch.setOnClickListener {
            editTextSearch.requestFocus()
            showKeyboard()
        }
        containerSearch.setOnClickListener {
            editTextSearch.requestFocus()
            showKeyboard()
        }

        editTextSearch.doOnTextChangedWithDebounce { text ->
            if (text.toString().isNotEmpty()) {
                if (!isDouble(text.toString())){
                recyclerViewSearch.isVisible = true
                viewModel.autoCompleteWithType(text.toString(),catName,origin)
                }
            } else {
                recyclerViewSearch.isVisible = false
//                binding.imageSearch.isVisible = true
            }
        }
        //search adapter list item click listener
        adapter.setOnItemClicked { prediction ->
            symbolManager?.deleteAll()
            binding.editTextSearch.text.clear()
            recyclerViewSearch.isVisible = false
//            getForwardGeocode(prediction.description)
            if (prediction.placeId != null) {
                getPlaceDetails(listOf(prediction.placeId!!), "")
            } else {
                getForwardGeocode(prediction.description, "")
            }
        }

        //searcg adapter list item direction button click listner
        adapter.getDirectionClicked { prediction ->
            symbolManager?.deleteAll()
            binding.editTextSearch.text.clear()
            recyclerViewSearch.isVisible = false
//            getForwardGeocode(prediction.description)
            if (prediction.placeId != null) {
                getPlaceDetails(listOf(prediction.placeId!!), Commons.destination)
            } else {
                getForwardGeocode(prediction.description, Commons.destination)
            }
        }
//to recentre the map
        fabRecenter.setOnClickListener {
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(origin!!.toLatLng(), 12.0))

        }
        //to add styles on the map
        fabStyles.setOnClickListener {
            showBottomSheetDialog()
        }


    }


    private fun getPlaceDetails(ids: List<String>, place: String) {
        geoHelper.geoMapsApi?.getPlaceDetails(
            ids = ids,
            onSuccess = { peliasResponse ->
                val coordinates = peliasResponse.features.first().geometry.coordinates.toLatLng()
                lifecycleScope.launch {
                    binding.recyclerViewSearch.isVisible = false
                    hideKeyboard()
                    mapHelper.moveCameraToResult(map, coordinates)
                    symbolManager?.addMarker(coordinates)
                    points = coordinates
                    val text = peliasResponse.features.first().properties?.get("label").toString()
                    binding.editTextSearch.updateText(text)
                    binding.imageSearch.isVisible = false
                    binding.editTextSearch.clearFocus()
                    binding.imageViewClose.isVisible = true
                    binding.bottomView.isVisible = true
                    binding.timeDistance.text =
                        "$text \n (Lat: ${coordinates.latitude},Long: ${coordinates.longitude})"
                }
            },
            onError = { errorMsg ->
                showToast(errorMsg)
            }
        )
    }
    private fun enable3DView1() {

        val newCameraPosition = CameraPosition.Builder()
            .tilt(60.0) // Tilt angle to make it 3D
            .zoom(14.0) // Adjust zoom level
            .build()


        map?.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition), 100)
    }

    private fun getNearestRoads(latLng: LatLng) {
        geoHelper.geoMapsApi?.getNearestRoads(
            locations = listOf(Coordinate(latLng.latitude, latLng.longitude)),
            onSuccess = { locateObjects ->
                lifecycleScope.launch {
                    val desc = locateObjects.flatMap { it.edges ?: listOf() }.map { edge ->
                        binding.timeDistance.text =  "Nearest road is on ${edge.sideOfStreet} with percent along ${edge.percentAlong}"
                    }
//                    Alert.alert {
//                        title = "Nearest Road"
//                        description = desc.joinToString("\n")
//                        alertContext = this@MainActivity
//                    }
                }
            },
            onError = { errorMsg ->
                showToast(errorMsg)
            }
        )
    }


    private fun getReverseGeocode(latLng: LatLng) {
        geoHelper.geoMapsApi?.getReverseGeocode(
            pointLat = latLng.latitude,
            pointLon = latLng.longitude,
            onSuccess = { peliasResponse ->
                lifecycleScope.launch {


                    val properties = peliasResponse.features.first().properties
                    val label = properties?.get("label") // ex. label -> Panbidi Shop, Mumbai

                    if (place == Commons.origin) {
                        originText = label.toString()
                    } else if (place == Commons.destination) {

                        symbolManager?.let {
                            it.deleteAll()
                            it.addMarker(latLng)
                        }
                        binding.editTextSearch.updateText("$label")
                        binding.editTextSearch.clearFocus()
                        binding.imageViewClose.isVisible = true
                        binding.imageSearch.visibility = View.GONE
                        binding.imageSearch.isVisible = false
                        binding.bottomView.isVisible = true
                        binding.timeDistance.text = "$label \n (Lat: ${latLng.latitude},Long: ${latLng.longitude})"

                    }

                }
            },
            onError = { errorMsg ->
                showToast(errorMsg)
            }
        )
    }

    private fun getForwardGeocode(text: String, place: String) {
        geoHelper.geoMapsApi?.getForwardGeocode(
            text = text,
            onSuccess = { peliasResponse ->
                val coordinates = peliasResponse.features.first().geometry.coordinates.toLatLng()
                if (place.isNotBlank()) {

                } else {
                    lifecycleScope.launch {
                        binding.recyclerViewSearch.isVisible = false
                        hideKeyboard()
                        mapHelper.moveCameraToResult(map, coordinates)
                        symbolManager?.addMarker(coordinates)
                        points = coordinates
                        binding.editTextSearch.updateText(text)
                        binding.imageSearch.isVisible = false
                        binding.editTextSearch.clearFocus()
                        binding.imageViewClose.isVisible = true
                        binding.bottomView.isVisible = true
                        binding.timeDistance.text = "$text \n (Lat: ${coordinates.latitude},Long: ${coordinates.longitude})"
                    }
                }
            },
            onError = { errorMsg ->
                showToast(errorMsg)
            }
        )
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
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

    private fun showBottomSheetDialog() {
        // Inflate the layout for the Bottom Sheet
        val view: View = layoutInflater.inflate(R.layout.map_style_layout, null)

        // Create the Bottom Sheet Dialog
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(view)

        // Handle interactions within the Bottom Sheet
        val satellite: ImageView = view.findViewById(R.id.satellite_view)
        satellite.setOnClickListener {
            setMapStyle(geoHelper.satelliteUrl, map!!)
            bottomSheetDialog.dismiss()
        }
        val default: ImageView = view.findViewById(R.id.default_view)
        default.setOnClickListener {
            setMapStyle(geoHelper.tilesUrl, map!!)
            bottomSheetDialog.dismiss()
        }

        val threeD: ImageView = view.findViewById(R.id.other)
        val threelyt: LinearLayout = view.findViewById(R.id.threeDLyt)
        if (isThreeD ) {
            threelyt.background = ContextCompat.getDrawable(this, R.drawable.background_search)
        }
        threeD.setOnClickListener {

            if (threelyt.background == null) {
                enable3DView1()
                isThreeD = true
                threelyt.background = ContextCompat.getDrawable(this, R.drawable.background_search)
                bottomSheetDialog.dismiss()
            } else {
                isThreeD = false
                threelyt.background = null
                val newCameraPosition = CameraPosition.Builder()
                    .tilt(0.0) // Set tilt angle to 0 to make it 2D
                    .zoom(12.0) // Adjust zoom level as needed
                    .build()

                map?.animateCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition))
                bottomSheetDialog.dismiss()
            }
        }
        val traffic: ImageView = view.findViewById(R.id.traffic)
        traffic.setOnClickListener {
            binding.mapView.getMapAsync { mapboxMap ->
                mapboxMap.setStyle(geoHelper.tilesUrl) { style ->
//                    addCustomTileLayer1(style)
                }

            }

            bottomSheetDialog.dismiss()
        }
        val close: ImageView = view.findViewById(R.id.close_style)

        close.setOnClickListener {
            // Handle button click
            bottomSheetDialog.dismiss() // Dismiss the Bottom Sheet Dialog
        }

        // Show the Bottom Sheet Dialog
        bottomSheetDialog.show()
    }

    fun isLatLong(text: String): Boolean {
        // Define the regex pattern for latitude and longitude
        val latLongPattern = Pattern.compile("^(-?\\d+(\\.\\d+)?),\\s*(-?\\d+(\\.\\d+)?)$")

        // Match the pattern with the input text
        val matcher = latLongPattern.matcher(text)

        if (matcher.matches()) {
            val latitude = matcher.group(1).toDoubleOrNull()
            val longitude = matcher.group(3).toDoubleOrNull()

            // Check if latitude and longitude are within valid ranges
            if (latitude != null && longitude != null) {
                if (latitude in -90.0..90.0 && longitude in -180.0..180.0) {
                    points = Point.fromLngLat(longitude, latitude).toLatLng()
                    return true
                }
            }
        }
        return false
    }

    private fun addCustomTileLayer1(style: Style) {
        val customTileUrl =
            "https://api.genesysmap.com/api/v1/traffic/data/12/10/733/475.pbf?api_key=4FBC5BFE7E34C58F414519B43972C"

        // Add a VectorSource
        val vectorSource = VectorSource("custom-tile-source", customTileUrl)
        style.addSource(vectorSource)

        // Add a VectorLayer
        val vectorLayer = LineLayer("custom-tile-layer", "custom-tile-source").apply {
            setProperties(
                lineColor("#8C0B8A"),
                lineOpacity(0.3f),
                lineWidth(6f)
            )
        }

        style.addLayer(vectorLayer)
    }
    private fun addCustomTileLayer(style: Style) {
        val customTileUrl =
            "https://api.genesysmap.com/api/v1/traffic/data/12/10/733/475.pbf?api_key=4FBC5BFE7E34C58F414519B43972C"
        val rasterSource = RasterSource("custom-tile-source", customTileUrl, 256)
        style.addSource(rasterSource)
        val rasterLayer = RasterLayer("custom-tile-layer", "custom-tile-source").apply { setProperties(
            lineColor("#8C0B8A"),
            lineOpacity(0.3f),
            lineWidth(6f)
        ) }

        style.addLayer(
            rasterLayer
        )

    }

    private fun isDouble(text: String): Boolean {
        return try {
            text.toDouble()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }

    fun toggleTrafficLayer(mapView: MapView, trafficLayerAdded: Boolean, TRAFFIC_TILES_URL: String, API_KEY: String) {

        if (!trafficLayerAdded) {
//            addTrafficLayer(map!!, TRAFFIC_TILES_URL, API_KEY)
            val latLngList = listOf(LatLng(12.976654848114677, 77.6050138207973))
            val latLngBounds = LatLngBounds.fromLatLngs(latLngList)
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 200)
            map?.easeCamera(cameraUpdate)
//            showTrafficMenu(mapView.context as Activity)
        } else {
            removeTrafficLayer(map!!)
//            hideTrafficMenu(mapView.context as Activity, mapView.findViewById(R.id.traffic_menu))
        }
    }

//    private fun addTrafficLayer(mapboxMap: MapboxMap, TRAFFIC_TILES_URL: String, API_KEY: String) {
//        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
//        val tilesUrlTemplate = "$TRAFFIC_TILES_URL/$hour/{z}/{x}/{y}.pbf?api_key=$API_KEY"
//
//        map!!.setStyle(geoHelper.tilesUrl) {
//            map!!.getStyle { style ->
//                style.addSource(VectorSource("traffic", tilesUrlTemplate))
//
//                style.addLayer(
//                    LineLayer("traffic-layer", "traffic").apply {
//                        setProperties(
//                            PropertyFactory.lineColor(
//                                match(
//                                    get(""),"",
//                                    stop(20,""), stop(10,""), stop(5,"")
//                                )
//                               /* match(
//                                    Expression.get("speed_raw"),
//                                    Expression.stop(20, "#00FF00"),
//                                    Expression.stop(10, "#FFFF00"),
//                                    Expression.stop(5, "#FFA500"),
//                                    "#FF0000"
//                                )*/
//                            ),
//                            PropertyFactory.lineWidth(
//                                Expression.match(
//                                    Expression.get("road_class"),
//                                    Expression.stop("tertiary", 5),
//                                    Expression.stop("secondary", 5),
//                                    Expression.stop("residential", 5),
//                                    5
//                                )
//                            )
//                        )
//                    }
//                )
//            }
//        }
//    }

    fun removeTrafficLayer(mapboxMap: MapboxMap) {
        mapboxMap.getStyle { style ->
            if (style.getLayer("traffic-layer") != null) {
                style.removeLayer("traffic-layer")
            }
            if (style.getSource("traffic") != null) {
                style.removeSource("traffic")
            }
        }
    }

}