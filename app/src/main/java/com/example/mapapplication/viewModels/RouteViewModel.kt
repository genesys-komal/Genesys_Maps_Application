package com.example.mapapplication.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapapplication.helpers.GeoHelper
import com.example.mapapplication.originCode.net.models.CostingModel
import com.example.mapapplication.originCode.net.models.CostingOptions
import com.example.mapapplication.originCode.net.models.DirectionsOptions
import com.example.mapapplication.originCode.net.models.DistanceUnit
import com.example.mapapplication.originCode.net.models.RouteOsrmResponse
import com.example.mapapplication.originCode.net.models.RouteResponse
import com.example.mapapplication.originCode.net.models.RoutingWaypoint
import com.example.mapapplication.originCode.net.models.ValhallaLanguages
import com.google.gson.Gson
import kotlinx.coroutines.launch

class RouteViewModel(private val repository: GeoHelper) : ViewModel() {

    private val _routeOsrmResponse = MutableLiveData<RouteOsrmResponse>()
    val routeOsrmResponse: LiveData<RouteOsrmResponse> get() = _routeOsrmResponse

    private val _routeResponse = MutableLiveData<RouteResponse>()
    val routeResponse: LiveData<RouteResponse> get() = _routeResponse

    private val _loadingState = MutableLiveData<Boolean>()
    val loadingState: LiveData<Boolean> get() = _loadingState

    private val _errorState = MutableLiveData<String>()
    val errorState: LiveData<String> get() = _errorState

    fun getDirectionOsrm(
        costingModel: CostingModel,
        costingOptions: CostingOptions,
        wayList: List<RoutingWaypoint>? = null
    ) {
        if (wayList.isNullOrEmpty()) {
            _errorState.value = "way list empty"
            return
        }
        if (costingModel == null) {
            _errorState.value = "costingModel empty"
            return
        }
        if (costingOptions == null) {
            _errorState.value = "costingOptions empty"
            return
        }
        val directionsOptions = DirectionsOptions(
            DistanceUnit.km,
            ValhallaLanguages.enMinusUS,
            DirectionsOptions.DirectionsType.instructions
        )
        _loadingState.value = true
        repository.geoMapsApi?.getDirectionsOsrm(locations = wayList!!,
            costing = costingModel,
            directionsOptions = directionsOptions,
            costingOptions = costingOptions,
            onSuccess = { routeOsrmResponse ->

                val gson = Gson()
                Log.d("routeResponsess", gson.toJson(routeOsrmResponse))
                viewModelScope.launch {
                    _loadingState.value = false

                    _routeOsrmResponse.value = routeOsrmResponse

                }

            },
            onError = { error ->

                viewModelScope.launch {
                    _loadingState.value = false
                    _errorState.value = error
                }
            }
        )
    }

    fun getDirection(
        costingModel: CostingModel,
        costingOptions: CostingOptions,
        wayList: List<RoutingWaypoint>? = null
    ) {
        if (wayList.isNullOrEmpty()) {
            _errorState.value = "way list empty"
            return
        }
        if (costingModel == null) {
            _errorState.value = "costingModel empty"
            return
        }
        if (costingOptions == null) {
            _errorState.value = "costingOptions empty"
            return
        }
        val directionsOptions = DirectionsOptions(
            DistanceUnit.km,
            ValhallaLanguages.enMinusUS,
            DirectionsOptions.DirectionsType.instructions
        )
        _loadingState.value = true
        repository.geoMapsApi?.getDirections(locations = wayList!!,
            costing = costingModel,
            directionsOptions = directionsOptions,
            costingOptions = costingOptions,
            onSuccess = { routeResponse ->

                val gson = Gson()
                Log.d("routeResponsess", gson.toJson(routeResponse))
                viewModelScope.launch {
                    _loadingState.value = false

                    _routeResponse.value = routeResponse

                }

            },
            onError = { error ->

                viewModelScope.launch {
                    _loadingState.value = false
                    _errorState.value = error
                }
            }
        )
    }

}