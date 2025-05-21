package org.example.rickandmortyapp.model

import kotlinx.serialization.Serializable

@Serializable
data class LocationResponse(
    val info: Info,
    val results: List<Location>
)

@Serializable
data class Location(
    val id: Int,
    val name: String,
    val type: String,
    val dimension: String
)
