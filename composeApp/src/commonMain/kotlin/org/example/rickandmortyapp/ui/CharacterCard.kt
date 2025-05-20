package org.example.rickandmortyapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.example.rickandmortyapp.model.RickCharacter

@Composable
fun CharacterCard(character: RickCharacter) {
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F7FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KamelImage(
                resource = asyncPainterResource(character.image),
                contentDescription = character.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(90.dp)
                    .background(Color.LightGray, RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF00334E)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Species: ${character.species}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF006680)
                )
                Text(
                    text = "Status: ${character.status}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF006680)
                )
            }
        }
    }
}
