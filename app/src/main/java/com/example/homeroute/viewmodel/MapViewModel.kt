package com.example.homeroute.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homeroute.api.OpenRouteServiceApi
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

class MapViewModel(
    private val api: OpenRouteServiceApi
) : ViewModel() {

    private val _route = mutableStateOf<List<LatLng>>(emptyList())
    val route: State<List<LatLng>> = _route

    fun getRoute(apiKey: String, start: LatLng, end: LatLng) {
        viewModelScope.launch {
            try {
                val startCoords = "${start.longitude},${start.latitude}"
                val endCoords = "${end.longitude},${end.latitude}"

                val response = api.getRoute(apiKey, startCoords, endCoords)

                val points = response.features.first().geometry.coordinates.map {
                    LatLng(it[1], it[0])
                }

                _route.value = points

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
