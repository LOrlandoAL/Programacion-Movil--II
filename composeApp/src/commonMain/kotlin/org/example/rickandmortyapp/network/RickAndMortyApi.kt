package org.example.rickandmortyapp.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.example.rickandmortyapp.model.*

val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

suspend fun getCharacters(page: Int = 1): List<RickCharacter> {
    val response: CharacterResponse = httpClient.get("https://rickandmortyapi.com/api/character/?page=$page").body()
    return response.results
}

suspend fun getLocations(page: Int = 1): List<Location> {
    val response: LocationResponse = httpClient.get("https://rickandmortyapi.com/api/location/?page=$page").body()
    return response.results
}

suspend fun getEpisodes(page: Int = 1): List<Episode> {
    val response: EpisodeResponse = httpClient.get("https://rickandmortyapi.com/api/episode/?page=$page").body()
    return response.results
}
