package com.example.mapapplication.common

import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.mapapplication.adapter.SearchSuggestionAdapter
import com.example.mapapplication.helpers.GeoHelper
import com.example.mapapplication.helpers.LocationHelper
import com.example.mapapplication.helpers.MapHelper
import kotlinx.coroutines.launch

open class BaseActivity : AppCompatActivity() {

    val geoHelper: GeoHelper by lazy { GeoHelper() }
    val locationHelper: LocationHelper by lazy { LocationHelper(this) }
    val mapHelper: MapHelper by lazy { MapHelper() }
    val adapter: SearchSuggestionAdapter by lazy { SearchSuggestionAdapter() }

    fun showToast(msg: String?, duration: Int? = null) {
        lifecycleScope.launch {
            Toast.makeText(this@BaseActivity, msg, duration ?: Toast.LENGTH_SHORT).show()
        }
    }
}