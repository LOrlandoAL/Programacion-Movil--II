package org.example.rickandmortyapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.rickandmortyapp.model.*
import org.example.rickandmortyapp.network.*

@Composable
fun CharacterScreen() {
    var characters by remember { mutableStateOf<List<RickCharacter>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        characters = getCharacters()
        isLoading = false
    }

    if (isLoading) {
        Loading()
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(characters) { character ->
                CharacterCard(character)
            }
        }
    }
}

@Composable
fun LocationScreen() {
    var locations by remember { mutableStateOf<List<Location>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        locations = getLocations()
        isLoading = false
    }

    if (isLoading) {
        Loading()
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(locations) { location ->
                LocationCard(location)
            }
        }
    }
}

@Composable
fun EpisodeScreen() {
    var episodes by remember { mutableStateOf<List<Episode>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        episodes = getEpisodes()
        isLoading = false
    }

    if (isLoading) {
        Loading()
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(episodes) { episode ->
                EpisodeCard(episode)
            }
        }
    }
}

@Composable
fun Loading() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
