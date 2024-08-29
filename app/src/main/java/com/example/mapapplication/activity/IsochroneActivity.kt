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
    private lateinit var timeSlider: SeekBar
    private lateinit var intervalStepSlider: SeekBar
    private lateinit var denoiseSlider: SeekBar
    private lateinit var generalizeSlider: SeekBar
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

    private fun showisochrone(style: Style) {
// Define the JSON response
        val jsonResponse = """
        {"features":[{"geometry":{"coordinates":[[[72.859014,19.169874],[72.85417,19.140922],[72.843656,19.142077],[72.846014,19.157616],[72.841383,19.149708],[72.835471,19.150621],[72.833709,19.145077],[72.838014,19.141773],[72.84227,19.143333],[72.841585,19.139648],[72.846134,19.136077],[72.842524,19.135077],[72.846403,19.128689],[72.840947,19.12801],[72.846627,19.126077],[72.841467,19.124077],[72.844705,19.122077],[72.842043,19.121077],[72.844572,19.117077],[72.846014,19.117592],[72.850619,19.128077],[72.850784,19.131307],[72.854262,19.130325],[72.854596,19.118077],[72.850451,19.119077],[72.854331,19.116077],[72.854014,19.111309],[72.860502,19.115077],[72.855413,19.117077],[72.8586,19.128077],[72.855719,19.132077],[72.855859,19.140233],[72.869311,19.136374],[72.867014,19.126213],[72.870388,19.128077],[72.871014,19.137484],[72.875014,19.127423],[72.880024,19.130068],[72.876537,19.130601],[72.88002,19.132083],[72.875481,19.134077],[72.87709,19.145153],[72.873014,19.140626],[72.864014,19.141696],[72.866847,19.147077],[72.864612,19.150675],[72.856761,19.150824],[72.858366,19.165077],[72.862266,19.166077],[72.859014,19.169874]]],"type":"Polygon"},"properties":{"color":"#00ffff","contour":5.0,"fill":"#00ffff","fillColor":"#00ffff","fillOpacity":0.33,"metric":"distance","opacity":0.33},"type":"Feature"},{"geometry":{"coordinates":[[[72.864014,19.149501],[72.860669,19.148423],[72.861576,19.14264],[72.866672,19.14642],[72.864014,19.149501]]],"type":"Polygon"},"properties":{"color":"#00ffff","contour":5.0,"fill":"#00ffff","fillColor":"#00ffff","fillOpacity":0.33,"metric":"time","opacity":0.33},"type":"Feature"},{"geometry":{"coordinates":[[72.86888,19.150102],[72.865835,19.147389]],"type":"MultiPoint"},"properties":{},"type":"Feature"},{"geometry":{"coordinates":[72.869014,19.150077],"type":"Point"},"properties":{},"type":"Feature"}],"id":"valhalla_isochrones","type":"FeatureCollection"}
        """

        // Parse the JSON response
        val jsonObject = JSONObject(jsonResponse)
        val features = jsonObject.getJSONArray("features")

        // Add features to the map
        val geoJsonSource = GeoJsonSource("isochrone-source", jsonResponse)
        style.addSource(geoJsonSource)
        for (i in 0 until features.length()) {
            val feature = features.getJSONObject(i)
            val geometry = feature.getJSONObject("geometry")
            val coordinates = geometry.getJSONArray("coordinates")

            // Check if the feature is a Polygon
            if (geometry.getString("type") == "Polygon") {
                val properties = feature.getJSONObject("properties")
                val fillColor = properties.optString("fillColor", "#8C0B8A")
                val fillOpacity = properties.optDouble("fillOpacity", 0.5).toFloat()

                // Create and add a FillLayer for each feature
                val fillLayer = FillLayer("isochrone-layer-$i", "isochrone-source").apply {
                    setProperties(
                        fillColor(fillColor),
                        fillOpacity(fillOpacity),


                        )
                }
                style.addLayer(fillLayer)
            }
        }
        /*  // Parse the JSON response
          val jsonObject = JSONObject(jsonResponse)
          val features = jsonObject.getJSONArray("features")

          // Add features to the map
          val geoJsonSource = GeoJsonSource("isochrone-source", jsonResponse)

          // Add source to map style
          style.addSource(geoJsonSource)

          // Create and add a FillLayer
          val fillLayer = FillLayer("isochrone-layer", "isochrone-source").apply {
              setProperties(
                  fillColor("#E84511"),
                  fillOpacity(0.5f)
              )
          }
          style.addLayer(fillLayer)*/

    }

    private fun fetchIsochroneData(style: Style) {
        // Sample isochrone data (replace with actual API call)
        val isochroneGeoJson = """
        {
          "type": "FeatureCollection",
          "features": [
            {
              "type": "Feature",
              "geometry": {
                "type": "Polygon",
                "coordinates": [
                  [
                    [72.874749,19.172857],[72.873493,19.172435],[72.874651,19.172336],[72.874536,19.170435],[72.872466,19.168719]
                  ,[72.8723,19.166435],[72.871073,19.165112],[72.870429,19.165115],[72.869749,19.166956],[72.866354,19.16604],[72.865749,19.167851],[72.865161,19.166024],[72.862204,19.165889],[72.861749,19.16829],[72.86146,19.166725],[72.857749,19.165735],[72.857168,19.164016],[72.855018,19.162167],[72.852749,19.163013],[72.85194,19.162435],[72.851295,19.161435],[72.854061,19.160746],[72.854005,19.159435],[72.850985,19.15967],[72.850749,19.160988],[72.849749,19.16068],[72.849644,19.159435],[72.850433,19.159119],[72.850472,19.157435],[72.851397,19.156435],[72.849937,19.154248],[72.846975,19.154209],[72.846102,19.153083],[72.844749,19.153376],[72.843865,19.152435],[72.844435,19.150121],[72.84667,19.149435],[72.846075,19.148109],[72.844958,19.147226],[72.843569,19.147254],[72.842749,19.149275],[72.839749,19.14739],[72.837749,19.14355],[72.836749,19.14384],[72.834417,19.141435],[72.835575,19.140261],[72.835749,19.137058],[72.837749,19.135138],[72.838749,19.137125],[72.840749,19.13704],[72.841749,19.13593],[72.843124,19.13581],[72.843111,19.134073],[72.84084,19.131435],[72.842749,19.129736],[72.844385,19.1308],[72.846788,19.130474],[72.845127,19.127435],[72.846749,19.125578],[72.848749,19.125231],[72.849094,19.127435],[72.850749,19.129071],[72.851584,19.128435],[72.851586,19.127599],[72.850869,19.127435],[72.851749,19.126624],[72.852072,19.128112],[72.853453,19.128139],[72.852143,19.122829],[72.855036,19.121722],[72.855749,19.120129],[72.85593,19.121255],[72.85822,19.121906],[72.859435,19.120435],[72.858598,19.119435],[72.859488,19.117696],[72.858683,19.117368],[72.859749,19.117117],[72.860749,19.118172],[72.861477,19.117435],[72.85936,19.114435],[72.863749,19.114813],[72.864749,19.113266],[72.867749,19.115045],[72.870749,19.114354],[72.870915,19.116435],[72.871749,19.117189],[72.872749,19.115375],[72.874749,19.116249],[72.874911,19.117274],[72.876828,19.118357],[72.877518,19.119667],[72.879126,19.119811],[72.880133,19.119052],[72.88251,19.123675],[72.883749,19.123995],[72.882613,19.126572],[72.885061,19.126747],[72.88615,19.125836],[72.886749,19.123808],[72.889749,19.125647],[72.892052,19.124738],[72.892749,19.123727],[72.894563,19.124435],[72.893796,19.126481],[72.892749,19.125961],[72.890541,19.127435],[72.89043,19.129754],[72.895191,19.131435],[72.893749,19.131592],[72.891383,19.134068],[72.891198,19.136435],[72.891749,19.13703],[72.892749,19.136119],[72.8943,19.136986],[72.894805,19.13538],[72.895749,19.13732],[72.896786,19.137398],[72.896862,19.138435],[72.894515,19.140435],[72.894755,19.141441],[72.89392,19.141606],[72.894301,19.144435],[72.893079,19.144765],[72.893196,19.145989],[72.894749,19.146224],[72.895236,19.147435],[72.892817,19.149503],[72.891846,19.151532],[72.891993,19.153678],[72.890675,19.154361],[72.891749,19.154869],[72.89204,19.156144],[72.89371,19.156435],[72.893408,19.157435],[72.891749,19.15661],[72.889122,19.158435],[72.889749,19.160129],[72.890749,19.159206],[72.89284,19.159345],[72.89368,19.159435],[72.893703,19.160435],[72.891898,19.161584],[72.889312,19.160997],[72.889218,19.162967],[72.891594,19.163435],[72.888749,19.163687],[72.886749,19.165646],[72.885601,19.165584]
                  ]
                ]
              },
              "properties": {
                "color": "#060DF7"
              }
            }
          ]
        }
        """.trimIndent()

        // Convert JSONObject to GeoJsonSource
        val geoJsonSource = GeoJsonSource("isochrone-source", isochroneGeoJson)

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
             /*               isochroneResponse.features.map {

                            setProperties(fillColor("#E84511"),
                                fillOpacity(it.properties?.fillOpacity))}

//                        }*/
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
    private fun moveCameraResult() {
        val latLngBounds = LatLngBounds.Builder()
            .includes(wayList.map { LatLng(it.lat, it.lon) })
            .build()

        val newCameraPosition = CameraPosition.Builder()
            .target(latLngBounds.center)
//            .zoom(12.0)
            .build()
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100))
    }
    private fun plotIsochrone(data: String, lat: Double, lng: Double) {
        mapboxMap.getStyle { style ->
            val geoJsonSource = GeoJsonSource("isochrone-source", data)

            // Add source to map style
            style.addSource(geoJsonSource)
            val gson = Gson()
            val datan = gson.toJson(data)
            // Create and add a FillLayer
            val fillLayer = FillLayer("isochrone-layer", "isochrone-source").apply {
                setProperties(
                    fillColor("#E84511"),
                    fillOpacity(0.5f)
                )
            }
            style.addLayer(fillLayer)
        }
    }

    private fun plotMarker(lat: Double, lng: Double) {
        mapboxMap.getStyle { style ->
            if (symbolManager == null) {
                symbolManager = SymbolManager(mapView, mapboxMap, style).apply {
                    iconAllowOverlap = true
                    textAllowOverlap = true
                }
            }

            symbolManager?.deleteAll()
            symbolManager?.create(
                SymbolOptions()
                    .withLatLng(LatLng(lat, lng))
                    .withIconImage("marker-icon")
                    .withIconSize(1.3f)
                    .withTextField("Origin")
                    .withTextOffset(arrayOf(0f, 1.5f))
                    .withTextSize(12f)
            )
        }
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        setMapStyle(geoHelper.tilesUrl, mapboxMap)
    }
    private fun flyCameraToBounds(latLng: LatLng) {
        val latLngList = listOf(latLng)
        val latLngBounds = LatLngBounds.fromLatLngs(latLngList)
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 200)
        mapboxMap.easeCamera(cameraUpdate)
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

            // Optionally set a custom marker image
            // style.addImage("marker-icon", BitmapFactory.decodeResource(resources, R.drawable.your_marker_icon))

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
//            fetchIsochroneData1()
            fetchIsochroneData1(style)
//            showisochrone(style)
        }
    }

    override fun getOnBackInvokedDispatcher(): OnBackInvokedDispatcher {
        return super.getOnBackInvokedDispatcher()
        finish()
    }
}

