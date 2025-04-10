package com.example.homeroute.utils

import android.annotation.SuppressLint
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.tasks.await

@SuppressLint("MissingPermission")
suspend fun FusedLocationProviderClient.getCurrentLocation(): Location? {
    return try {
        lastLocation.await()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
