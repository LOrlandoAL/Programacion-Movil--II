package org.example.rickandmortyapp.network

import org.example.rickandmortyapp.model.CharacterResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.example.rickandmortyapp.model.RickCharacter

val httpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

suspend fun getCharacters(page: Int = 1): List<RickCharacter> {
    val response: CharacterResponse = httpClient.get("https://rickandmortyapi.com/api/character/?page=$page").body()
    return response.results
}
