package com.example.syncdivisaapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_rates")
data class ExchangeRateEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val exchangeRates: String, // Guardamos las divisas como JSON en un String
    val lastFetch: String, // Fecha y hora en formato ISO 8601
    val nextFetch: String // También en formato ISO 8601
)
