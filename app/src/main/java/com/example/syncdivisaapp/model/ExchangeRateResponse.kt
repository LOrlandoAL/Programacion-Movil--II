package com.example.syncdivisaapp.model

data class ExchangeRateResponse(
    val conversion_rates: Map<String, Double> // La API devuelve un JSON con una lista de divisas
)