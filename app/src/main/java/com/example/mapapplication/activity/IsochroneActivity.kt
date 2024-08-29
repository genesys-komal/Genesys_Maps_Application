package com.example.mapapplication.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.window.OnBackInvokedDispatcher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.mapapplication.R
import com.example.mapapplication.adapter.WayPintData
import com.example.mapapplication.common.BaseActivity
import com.example.mapapplication.common.Commons
import com.example.mapapplication.common.doOnTextChangedWithDebounce
import com.example.mapapplication.common.hideKeyboard
import com.example.mapapplication.common.updateText
import com.example.mapapplication.databinding.ActivityIsochroneBinding
import com.example.mapapplication.helpers.toLatLng
import com.example.mapapplication.helpers.toPoint
import com.example.mapapplication.navigation.TurnNavigationActivity
import com.example.mapapplication.originCode.net.models.*
import com.google.gson.Gson
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.LineManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillOpacity
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.coroutines.launch
import org.json.JSONObject


class IsochroneActivity : BaseActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityIsochroneBinding
    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private var wayList = ArrayList<RoutingWaypoint>()
//    private var wayList = ArrayList<RoutingWaypoint>()
    private lateinit var searchInput: EditText
    private lateinit var searchResults: RecyclerView
    private var timeRange: Double = 0.0
    private var intervalSteps: Double = 0.0
    private var denoise: Double = 0.0
    private var generalize: Double = 0.0
    private var origin: Point? = null
    private var focusPoint: Point? = null
    private var contours = ArrayList<Contour>()
    private var costingModel: IsochroneCostingModel = IsochroneCostingModel.auto
    private lateinit var costingOptions: CostingOptions
    private var selectedCoords: LatLng? = null
    private var symbolManager: SymbolManager? = null
    private var lineManager: LineManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIsochroneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize MapView
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Retrieve starting coordinates
        val startLatLng: LatLng? = intent.getParcelableExtra(Commons.address)
        costingOptions = CostingOptions(auto = AutoCostingOptions())
        startLatLng?.let {
            wayList.add(RoutingWaypoint(lat = it.latitude, lon = it.longitude))

        }

        startLatLng?.let {
            getReverseGeocode(it)
            }


        // Setup UI components
        searchInput = binding.editTextSearch
        searchResults = binding.recyclerViewSearch
        binding.recyclerViewSearch.adapter = adapter
        binding.settingsIsochrone.setOnClickListener {
            binding.bottomView.isVisible = true
        }
        setupSeekBars()
        setupSearchInputListener()
        binding.isoClose.setOnClickListener {
            binding.bottomView.isVisible = false
            mapboxMap.getStyle { style ->
                fetchIsochroneData1(style)
            }
        }
    }

    private fun clearPolylines() {

        mapboxMap.getStyle { style ->
            style.removeLayer("isochrone-layer")
            style.removeSource("isochrone-source")
        }
        symbolManager?.deleteAll()
        lineManager?.deleteAll()
    }

    private fun setupSeekBars() {
        binding.rangeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                contours.clear()
                binding.stepSeekBar.setProgress(progress)
                binding.stepValue.setText(progress.toString())
                binding.rangeTextView.setText(progress.toString())
                generateContours(
                    progress,
                    binding.stepValue.text.toString().toInt()
                )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: Add logic when user starts dragging the SeekBar
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Optional: Add logic when user stops dragging the SeekBar
            }
        })
        binding.seekBarDenoise.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                binding.valueDenoise.setText((progress / 100.0).toString())
                denoise = (progress / 100.0).toDouble()

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: Add logic when user starts dragging the SeekBar
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Optional: Add logic when user stops dragging the SeekBar
            }
        })
        binding.stepSeekBar.setOnSeekBarChangeListener(
            createSeekBarChangeListener(binding.stepValue) {
                contours.clear()
                generateContours(
                    binding.rangeTextView.text.toString().toInt(),
                    binding.stepValue.text.toString().toInt()
                )
            })

        binding.seekBarGeneralize.setOnSeekBarChangeListener(createSeekBarChangeListener(
            binding.valueGeneralize
        ) {
            generalize = binding.valueGeneralize.text.toString().toDouble()

        })
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
                        val label = properties?.get("label").toString()
                        binding.editTextSearch.setText(label)

                    }
                }
            },
            onError = { errorMsg ->
                showToast(errorMsg)
            }
        )
    }
    private fun createSeekBarChangeListener(
        textView: TextView,
        onProgressChanged: () -> Unit
    ): SeekBar.OnSeekBarChangeListener {
        return object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textView.text = progress.toString()
                onProgressChanged()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }
    }

    private fun setupSearchInputListener() {
        binding.walkLayout.setOnClickListener {
            costingModel = IsochroneCostingModel.pedestrian
            costingOptions =
                CostingOptions(pedestrian = PedestrianCostingOptions())
            mapboxMap.getStyle { style ->
                fetchIsochroneData1(style)
            }
            binding.pedistrianImage.background = ContextCompat.getDrawable(this@IsochroneActivity, R.drawable.ractangle_blue)
            binding.busImage.background = ContextCompat.getDrawable(
                this@IsochroneActivity,
                R.drawable.background_map_search
            )
            binding.carImage.background = ContextCompat.getDrawable(
                this@IsochroneActivity,
                R.drawable.background_map_search
            )
            binding.truckImage.background = ContextCompat.getDrawable(
                this@IsochroneActivity,
                R.drawable.background_map_search
            )
            binding.bikeImage.background = ContextCompat.getDrawable(
                this@IsochroneActivity,
                R.drawable.background_map_search
            )


        }
        binding.busLayout.setOnClickListener {
            costingModel = IsochroneCostingModel.bus
            costingOptions = CostingOptions(AutoCostingOptions())
            mapboxMap.getStyle { style ->
                fetchIsochroneData1(style)
            }

            binding.pedistrianImage.background = ContextCompat.getDrawable(
                this@IsochroneActivity,
                R.drawable.background_map_search
            )
            binding.busImage.background =
                ContextCompat.getDrawable(this@IsochroneActivity, R.drawable.ractangle_blue)
            binding.carImage.background = ContextCompat.getDrawable(
                this@IsochroneActivity,
                R.drawable.background_map_search
            )
            binding.truckImage.background = ContextCompat.getDrawable(
                this@IsochroneActivity,
                R.drawable.background_map_search
            )
            binding.bikeImage.background = ContextCompat.getDrawable(
                this@IsochroneActivity,
                R.drawable.background_map_search
            )

        }
        binding.truckLayout.setOnClickListener {

            costingModel = IsochroneCostingModel.truck
            costingOptions = CostingOptions(truck = TruckCostingOptions())
            mapboxMap.getStyle { style ->
                fetchIsochroneData1(style)
            }
            binding.pedistrianImage.background = ContextCompat.getDrawable(
                this@IsochroneActivity,
                R.drawable.background_map_search
            )
            binding.busImage.background = ContextCompat.getDrawable(
                this@IsochroneActivity,
                R.drawable.background_map_search
            )
            binding.carImage.background = ContextCompat.getDrawable(
                this@IsochroneActivity,
                R.drawable.background_map_search
            )
            binding.truckImage.background =
                ContextCompat.getDrawable(this@IsochroneActivity, R.drawable.ractangle_blue)
            binding.bikeImage.background = ContextCompat.getDrawable(
                this@IsochroneActivity,
                R.drawable.background_map_search
            )

        }
        binding.bicycleLayout.setOnClickListener {

            costingModel = IsochroneCostingModel.bicycle
            costingOptions =
                CostingOptions(bicycle = BicycleCostingOptions())
            mapboxMap.getStyle { style ->
                fetchIsochroneData1(style)
            }
            binding.pedistrianImage.background = ContextCompat.getDrawable(
                this@IsochroneActivity,
                R.drawable.background_map_search
            )
            binding.busImage.background = ContextCompat.getDrawable(
                this@IsochroneActivity,
                R.drawable.background_map_search
            )
            binding.carImage.background = ContextCompat.getDrawable(
                this@IsochroneActivity,
                R.drawable.background_map_search
            )
            binding.truckImage.background = ContextCompat.getDrawable(
                this@IsochroneActivity,
                R.drawable.background_map_search
            )
            binding.bikeImage.background =
                ContextCompat.getDrawable(this@IsochroneActivity, R.drawable.ractangle_blue)

        }
        binding.carLayout.setOnClickListener {

            costingModel = IsochroneCostingModel.auto
            costingOptions = CostingOptions(auto = AutoCostingOptions())
            mapboxMap.getStyle { style ->
                fetchIsochroneData1(style)
            }
            binding.pedistrianImage.background = ContextCompat.getDrawable(
                this@IsochroneActivity,
                R.drawable.background_map_search
            )
            binding.busImage.background = ContextCompat.getDrawable(
                this@IsochroneActivity,
                R.drawable.background_map_search
            )
            binding.carImage.background =
                ContextCompat.getDrawable(this@IsochroneActivity, R.drawable.ractangle_blue)
            binding.truckImage.background = ContextCompat.getDrawable(
                this@IsochroneActivity,
                R.drawable.background_map_search
            )
            binding.bikeImage.background = ContextCompat.getDrawable(
                this@IsochroneActivity,
                R.drawable.background_map_search
            )

        }

        binding.editTextSearch.doOnTextChangedWithDebounce {
            fetchSearchResults(it.toString())
        }
        binding.imageViewClose.setOnClickListener {
            binding.editTextSearch.text.clear()
        }

        //search adapter list item click listener
        adapter.setOnItemClicked { prediction ->
            symbolManager?.deleteAll()
            binding.editTextSearch.text.clear()
            binding.recyclerViewSearch.isVisible = false
//            getForwardGeocode(prediction.description)
            if (prediction.placeId != null) {
                getPlaceDetails(listOf(prediction.placeId!!), "")
            } else {
                getForwardGeocode(prediction.description, "")
            }
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
//                    symbolManager?.addMarker(coordinates)
                    val text = peliasResponse.features.first().properties?.get("label").toString()
                    binding.editTextSearch.updateText(text)
                    binding.imageSearch.isVisible = false
                    binding.editTextSearch.clearFocus()
                    binding.imageViewClose.isVisible = true
                    wayList.clear()
                    wayList.add(
                        RoutingWaypoint(
                            lat = coordinates.latitude,
                            lon = coordinates.longitude
                        )
                    )
                    focusPoint = coordinates.toPoint()
                    mapboxMap.getStyle {
                        fetchIsochroneData1(it)
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
//                        mapHelper.moveCameraToResult(mapboxMap, coordinates)
//                        symbolManager?.addMarker(coordinates)
                        binding.editTextSearch.updateText(text)
                        binding.imageSearch.isVisible = false
                        binding.editTextSearch.clearFocus()
                        binding.imageViewClose.isVisible = true
                        wayList.clear()
                        wayList.add(
                            RoutingWaypoint(
                                lat = coordinates.latitude,
                                lon = coordinates.longitude
                            )
                        )
                        focusPoint = coordinates.toPoint()
                        mapboxMap.getStyle {
                            fetchIsochroneData1(it)
                        }
                    }
                }
            },
            onError = { errorMsg ->
                showToast(errorMsg)
            }
        )
    }

    private fun fetchSearchResults(input: String) {
        geoHelper.geoMapsApi?.autoComplete(
            text = input,
            focusPointLat = origin?.latitude(),
            focusPointLon = origin?.longitude(),
            onSuccess = { autoCompleteResponse ->
                lifecycleScope.launch {
                    binding.recyclerViewSearch.isVisible = true
                    adapter.submitList(autoCompleteResponse.predictions)

                    if (autoCompleteResponse.predictions.isEmpty()) {
                        showToast("No results found")
                    }
                }
            },
            onError = { errorMsg ->
                showToast(errorMsg)
            }
        )
    }

    private fun generateContours(time: Int, stepInterval: Int): List<Contour> {
        contours.clear()
        if (time == stepInterval) {
            contours.add(Contour(time = time.toDouble()))
//            return contours
        } else {
            var currentTime = time
            while (currentTime > 0) {
                contours.add(Contour(time = currentTime.toDouble()))
                currentTime -= stepInterval
                if (currentTime < stepInterval && currentTime > 0) {
                    contours.add(Contour(time = currentTime.toDouble()))
                    break
                }
            }
//            return contours
        }

        return contours
    }

    private fun fetchIsochroneData1(style: Style) {
        clearPolylines()
        geoHelper.geoMapsApi?.getIsochrone(
            wayList,
            costing = costingModel, true,
            denoise = denoise,
            generalize = generalize,
            show_locations = true,
            contours = contours,
            costingOptions = costingOptions,
            directionsOptions = DirectionsOptions(units = DistanceUnit.km),
            id = "valhalla_isochrones",
            onSuccess = { isochroneResponse ->
                val gson1 = Gson()
                Log.d("routeResponsessIsoc", isochroneResponse.toString())
//                showToast("routeResponsessIsoc"+ isochroneResponse.toString())
                val dataNew = gson1.toJson(isochroneResponse)
                Log.d("routeResponsessIsocjson", dataNew.toString())
                runOnUiThread(Runnable {
                    val geoJsonSource = GeoJsonSource("isochrone-source", dataNew.toString())

                    // Add source to map style
                    style.addSource(geoJsonSource)

                    // Create and add a FillLayer
                    val fillLayer = FillLayer("isochrone-layer", "isochrone-source").apply {
                        setProperties(

                            fillColor("#E84511"),
                            fillOpacity(0.5f)
                        )
                    }
                    style.addLayer(fillLayer)
                    mapboxMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            focusPoint!!.toLatLng(),
                            12.0
                        )
                    )

                })

            },
            onError = { error ->
                showToast(error)
            }
        )
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        setMapStyle(geoHelper.tilesUrl, mapboxMap)
    }
    private fun setMapStyle(styles: String, map: MapboxMap) {
        denoise = binding.valueDenoise.text.toString().toDouble()
        generalize = binding.valueGeneralize.text.toString().toDouble()
        intervalSteps = binding.stepValue.text.toString().toDouble()
        timeRange = binding.rangeTextView.text.toString().toDouble()
        generateContours(
            binding.rangeTextView.text.toString().toInt(),
            binding.stepValue.text.toString().toInt()
        )
        map.setStyle(styles) { style ->
            lineManager = LineManager(mapView, map, style)
            symbolManager = SymbolManager(mapView, map, style).apply {
                iconAllowOverlap = true
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
            locationHelper.setupLocationComponent(mapboxMap.locationComponent, style)

            origin = Point.fromLngLat(
                locationHelper.lastLocation?.longitude ?: 0.0,
                locationHelper.lastLocation?.latitude ?: 0.0
            )
            focusPoint = origin
            fetchIsochroneData1(style)
        }
    }

    override fun getOnBackInvokedDispatcher(): OnBackInvokedDispatcher {
        return super.getOnBackInvokedDispatcher()
        finish()
    }
}

