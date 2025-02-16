package com.example.syncdivisaapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_rates")
data class ExchangeRateEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val currency: String,
    val rate: Double
)