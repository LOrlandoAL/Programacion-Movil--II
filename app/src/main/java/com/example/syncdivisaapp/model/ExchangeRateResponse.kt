package com.example.syncdivisaapp.model

data class ExchangeRateResponse(
    val base_code: String,
    val conversion_rates: Map<String, Double>
)