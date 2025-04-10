package com.example.homeroute.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.homeroute.api.OpenRouteServiceApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MapViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            val api = Retrofit.Builder()
                .baseUrl("https://api.openrouteservice.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OpenRouteServiceApi::class.java)
            return MapViewModel(api) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
