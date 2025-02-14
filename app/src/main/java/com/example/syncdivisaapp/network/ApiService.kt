package com.example.syncdivisaapp.network

import com.example.syncdivisaapp.model.ExchangeRateResponse
import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("v6/e752ff2208ffa575854247e8/latest/USD")
    fun getExchangeRates(): Call<ExchangeRateResponse>
}