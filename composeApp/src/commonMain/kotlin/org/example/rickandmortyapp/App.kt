package org.example.rickandmortyapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.rickandmortyapp.ui.*

enum class TabItem(val title: String) {
    Characters("Personajes"),
    Locations("Lugares"),
    Episodes("Episodios")
}

@Composable
fun App() {
    var selectedTab by remember { mutableStateOf(TabItem.Characters) }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF0F0F5)
        ) {
            Column(modifier = Modifier
                .fillMaxSize()
                .padding(top = 0.dp)
            ) {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    indicator = { tabPositions ->
                        Box(
                            Modifier
                                .tabIndicatorOffset(tabPositions[selectedTab.ordinal])
                                .height(5.dp)
                                .background(Color(0xFF2E7D32)) // Verde oscuro
                        )
                    },
                    edgePadding = 0.dp,
                    divider = {}
                ) {
                    TabItem.values().forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab.ordinal == index,
                            onClick = { selectedTab = tab },
                            modifier = Modifier.fillMaxWidth(),
                            text = {
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    Text(
                                        tab.title,
                                        fontWeight = if (selectedTab.ordinal == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            },
                            selectedContentColor = Color(0xFFB2FF59),
                            unselectedContentColor = Color(0xFFC8E6C9)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (selectedTab) {
                    TabItem.Characters -> CharacterScreen()
                    TabItem.Locations -> LocationScreen()
                    TabItem.Episodes -> EpisodeScreen()
                }
            }
        }
    }
}
