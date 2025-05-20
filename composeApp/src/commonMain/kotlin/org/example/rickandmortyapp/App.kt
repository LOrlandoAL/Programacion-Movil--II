package org.example.rickandmortyapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.rickandmortyapp.model.RickCharacter
import org.example.rickandmortyapp.network.getCharacters
import org.example.rickandmortyapp.ui.CharacterCard

@Composable
fun App() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF0F0F5) // Fondo suave neutro
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Rick and Morty (${getPlatform().name})",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                var characters by remember { mutableStateOf<List<RickCharacter>>(emptyList()) }
                var isLoading by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    characters = getCharacters()
                    isLoading = false
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(characters) { character ->
                            CharacterCard(character)
                        }
                    }
                }
            }
        }
    }
}
