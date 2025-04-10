package com.example.homeroute.api

import com.example.homeroute.model.RouteResponse
import retrofit2.http.GET
import retrofit2.http.Query
interface OpenRouteServiceApi {
    @GET("v2/directions/driving-car")
    suspend fun getRoute(
        @Query("api_key") apiKey: String,
        @Query("start") start: String,
        @Query("end") end: String
    ): RouteResponse
}
