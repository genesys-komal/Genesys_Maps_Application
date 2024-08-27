package com.example.mapapplication.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapapplication.helpers.GeoHelper
import com.example.mapapplication.originCode.net.models.GoogPlacesAutocompleteResponse
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.launch

class MainViewModel(private val repository: GeoHelper) : ViewModel() {

    private val _autoCompleteResult = MutableLiveData<GoogPlacesAutocompleteResponse>()
    val autoCompleteResult: LiveData<GoogPlacesAutocompleteResponse> get() = _autoCompleteResult
     val _latlon = MutableLiveData<LatLng>()
    val latlon: LiveData<LatLng> get() = _latlon


    private val _loadingState = MutableLiveData<Boolean>()
    val loadingState: LiveData<Boolean> get() = _loadingState

    private val _errorState = MutableLiveData<String>()
    val errorState: LiveData<String> get() = _errorState

    fun autoCompleteWithType(
        input: String,
        type: String,
        origin: Point? = null
    ) {
        if (input.isBlank()) {
            _errorState.value = "Please add search text"
            return
        }

        _loadingState.value = true
        repository.geoMapsApi?.autoCompleteWithType(
            text = input,
            type = type,
            focusPointLat = origin?.latitude(),
            focusPointLon = origin?.longitude(),
            onSuccess = { autoCompleteResponse ->
                viewModelScope.launch {
                    _loadingState.value = false
                    if (autoCompleteResponse.predictions.isEmpty()) {
                        _errorState.value = "No results found"
                    } else {
                        _autoCompleteResult.value = autoCompleteResponse
                    }
                }
            },
            onError = { errorMsg ->
                viewModelScope.launch {
                    _loadingState.value = false
                    _errorState.value = errorMsg
                }
            }
        )
    }
}
