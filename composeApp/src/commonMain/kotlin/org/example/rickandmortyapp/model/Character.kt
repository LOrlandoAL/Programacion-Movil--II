package org.example.rickandmortyapp.model

import kotlinx.serialization.Serializable

@Serializable
data class CharacterResponse(
    val info: Info,
    val results: List<RickCharacter>
)

@Serializable
data class Info(val count: Int, val pages: Int)

@Serializable
data class RickCharacter(
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val gender: String,
    val image: String
)
