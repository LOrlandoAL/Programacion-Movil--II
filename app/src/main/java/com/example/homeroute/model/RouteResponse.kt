package com.example.homeroute.model

data class RouteResponse(
    val features: List<Feature>
)

data class Feature(
    val geometry: Geometry
)

data class Geometry(
    val coordinates: List<List<Double>>
)
