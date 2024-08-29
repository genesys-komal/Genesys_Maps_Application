package com.example.mapapplication.activity

import android.content.ClipDescription
import android.content.Intent
import android.gesture.Prediction
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.mapapplication.common.BaseActivity
import com.example.mapapplication.common.Constants
import com.example.mapapplication.common.doOnTextChangedWithDebounce
import com.example.mapapplication.databinding.ActivityFindBinding
import com.example.mapapplication.navigation.TurnNavigationActivity.Companion.current
import kotlinx.coroutines.launch

class FindActivity : BaseActivity() {
    private lateinit var binding: ActivityFindBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        setContentView(R.layout.activity_find)
//        val ss: LatLng? = intent.getSerializableExtra<LatLng>("origin",LatLng::class.java)

        val ss1: String? = intent.getStringExtra("place")
        if (!ss1.isNullOrBlank()) {
            binding.editTextStart.setText(ss1)
            autoComplete(ss1)
        }
        setupView()
    }
    inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
        SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
    }

    private fun setupView() = binding.apply {
        recyclerViewSearch.adapter = adapter

        clearText.setOnClickListener {
            editTextStart.clearFocus()
            editTextStart.text.clear()
        }
        backToTurn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            finish()
        }

        editTextStart.doOnTextChangedWithDebounce { text ->
            if (text.toString().isNotEmpty()) {
                autoComplete(text.toString())
            }
        }

        adapter.setOnItemClicked { prediction ->
            sendIntent(prediction.description, placeId = prediction.placeId.toString())
            /*  destroyAll()
              //You can use getPlaceDetails or getForwardGeocode
              if (prediction.placeId != null) {
                  getPlaceDetails(listOf(prediction.placeId!!))
              } else {
                  getForwardGeocode(prediction.description)
              }*/
        }
    }

    private fun sendIntent(description: String,placeId: String){
        val resultIntent = Intent()
        resultIntent.putExtra(Constants.DESCRIPTION.toString(), description?:null)
        resultIntent.putExtra(Constants.PLACEID.toString(), placeId?:null)
        setResult(RESULT_OK, resultIntent)
        finish()
    }
    private fun autoComplete(input: String) {
        geoHelper.geoMapsApi?.autoComplete(
            text = input,
            focusPointLat = current?.latitude(),
            focusPointLon = current?.longitude(),
            onSuccess = { autoCompleteResponse ->
                lifecycleScope.launch {
                    binding.recyclerViewSearch.isVisible =
                        autoCompleteResponse.predictions.isNotEmpty()
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

}