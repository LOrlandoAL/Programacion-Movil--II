package org.example.rickandmortyapp.model

import kotlinx.serialization.Serializable

@Serializable
data class EpisodeResponse(
    val info: Info,
    val results: List<Episode>
)

@Serializable
data class Episode(
    val id: Int,
    val name: String,
    val air_date: String,
    val episode: String
)
