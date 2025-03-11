package com.example.syncdivisaapp.network

import com.example.syncdivisaapp.model.ExchangeRateResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("{apiKey}/latest/USD")  // Asegúrate de que esta ruta es correcta
    suspend fun getExchangeRates(@Path("apiKey") apiKey: String): ExchangeRateResponse
}