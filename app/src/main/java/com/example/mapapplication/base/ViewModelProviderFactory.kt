package com.example.mapapplication.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mapapplication.helpers.GeoHelper
import com.example.mapapplication.viewModels.MainViewModel
import com.example.mapapplication.viewModels.RouteViewModel

class ViewModelProviderFactory(private val apiHelper: GeoHelper) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(apiHelper) as T
        }

        if (modelClass.isAssignableFrom(RouteViewModel::class.java)) {
            return RouteViewModel(apiHelper) as T
        }
//
//        if (modelClass.isAssignableFrom(NavigationViewModel::class.java)) {
//            return NavigationViewModel(AppRepository(apiHelper)) as T
//        }
        throw IllegalArgumentException("Unknown class name")
//        return super.create(modelClass)
    }
}