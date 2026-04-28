package fr.sdv.mon_app_api.presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.sdv.mon_app_api.R
import fr.sdv.mon_app_api.presentation.ui.MyError
import fr.sdv.mon_app_api.presentation.viewmodel.MainViewModel

private enum class PokedexTab { INFO, STATS }

@Composable
fun PokedexScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(PokedexTab.INFO) }
    var showSuggestions by remember { mutableStateOf(false) }
    val pokemon = viewModel.pokemon.collectAsStateWithLifecycle().value
    val loading = viewModel.runInProgress.collectAsStateWithLifecycle().value
    val error = viewModel.errorMessage.collectAsStateWithLifecycle().value
    val pokemonNames = viewModel.pokemonNames.collectAsStateWithLifecycle().value
    val pokemonFrenchName = pokemon?.let { viewModel.getPokemonFrenchName(it.name) } ?: ""

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.pokedex),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Text("Pokedex", style = MaterialTheme.typography.headlineSmall, color = Color.White, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth().padding(top = 60.dp, end = 40.dp))

        // Barre de recherche
        val suggestions = pokemonNames
            .filter { it.startsWith(query, ignoreCase = true) || it.contains(query, ignoreCase = true) }
            .take(5)

        Column(modifier = Modifier.offset(x = 60.dp, y = 390.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { newValue ->
                    query = newValue
                    showSuggestions = newValue.isNotEmpty()
                },
                label = { Text("Nom ou ID", color = Color.White, style = MaterialTheme.typography.labelSmall) },
                singleLine = true,
                modifier = Modifier.width(270.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF2A2A2A),
                    unfocusedContainerColor = Color(0xFF2A2A2A),
                    focusedIndicatorColor = Color(0xFF666666),
                    unfocusedIndicatorColor = Color(0xFF555555),
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color(0xFFCCCCCC)
                )
            )

            // Dropdown personnalisé pour les suggestions
            if (showSuggestions && suggestions.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .width(270.dp)
                        .background(Color(0xFF2A2A2A), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                        .border(1.dp, Color(0xFF666666), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        items(suggestions) { suggestion ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        query = suggestion
                                        showSuggestions = false
                                        viewModel.loadPokemon(suggestion)
                                    }
                                    .padding(12.dp)
                            ) {
                                Text(
                                    suggestion,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        // Contenu principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Espaceur pour descendre le sprite
            Spacer(modifier = Modifier.height(170.dp))

            // Image du Pokemon au centre - Hauteur fixe
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                if (loading) {
                    CircularProgressIndicator()
                } else if (pokemon != null) {
                    if (pokemon.sprites.frontDefault != null) {
                        AsyncImage(
                            model = pokemon.sprites.frontDefault,
                            contentDescription = pokemon.name,
                            modifier = Modifier.size(160.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text("Image indisponible", color = Color.White)
                    }
                }
            }

            MyError(errorMessage = error)
            
            // Spacer flexible pour repousser le contenu en bas
            Spacer(modifier = Modifier.weight(1f))

            // Zone info/stats en bas sur fond rouge
            if (pokemon != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE53935), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .padding(16.dp)
                        .heightIn(min = 140.dp, max = 250.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Boutons Infos/Stats
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            onClick = { selectedTab = PokedexTab.INFO },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = if (selectedTab == PokedexTab.INFO) Color(0xFF1565C0) else Color(0xFF455A64)
                            )
                        ) {
                            Text("Infos", color = Color.White)
                        }
                        Button(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            onClick = { selectedTab = PokedexTab.STATS },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = if (selectedTab == PokedexTab.STATS) Color(0xFF1565C0) else Color(0xFF455A64)
                            )
                        ) {
                            Text("Stats", color = Color.White)
                        }
                    }

                    // Contenu Infos ou Stats
                    when (selectedTab) {
                        PokedexTab.INFO -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Nom : $pokemonFrenchName", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                Text("ID : #${pokemon.id}", color = Color.White, style = MaterialTheme.typography.bodyMedium)

                                // Affichage des types avec leurs images
                                Text("Types :", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    pokemon.getTypeNames().forEach { typeName ->
                                        TypeBadge(typeName = typeName, viewModel = viewModel)
                                    }
                                }

                                Text("Taille : ${pokemon.height / 10.0}m", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                                Text("Poids : ${pokemon.weight / 10.0}kg", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        PokedexTab.STATS -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                pokemon.stats.forEach { stat ->
                                    StatBarRow(
                                        name = stat.stat.name,
                                        value = stat.baseStat
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBarRow(name: String, value: Int) {
    val normalized = value.coerceIn(0, 255)
    val ratio = normalized / 255f
    val shortName = getStatShortName(name)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            shortName,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(50.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0x33FFFFFF))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio)
                    .height(12.dp)
                    .background(statColor(normalized))
            )
        }
        Text(
            value.toString(),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.width(30.dp)
        )
    }
}

private fun getStatShortName(statName: String): String = when (statName.lowercase()) {
    "hp" -> "HP"
    "attack" -> "ATT"
    "defense" -> "DEF"
    "sp-atk", "special-attack" -> "SPA"
    "sp-def", "special-defense" -> "SPD"
    "speed" -> "SPE"
    else -> statName.take(3).uppercase()
}

private fun statColor(value: Int): Color = when {
    value < 30 -> Color(0xFF8B0000)    // rouge sombre
    value < 60 -> Color(0xFFFF0000)    // rouge
    value < 90 -> Color(0xFFFF6A00)    // orange sombre
    value < 120 -> Color(0xFFFF9800)   // orange
    value < 150 -> Color(0xFFFFEB3B)   // jaune
    value < 180 -> Color(0xFF2E7D32)   // vert sombre
    value < 210 -> Color(0xFF4CAF50)   // vert
    value < 240 -> Color(0xFF1565C0)   // bleu sombre
    else -> Color(0xFF64B5F6)          // bleu clair
}

@Composable
private fun TypeBadge(typeName: String, viewModel: MainViewModel) {
    val typeImageUrl = viewModel.getTypeImageUrl(typeName)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x44FFFFFF))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (typeImageUrl != null) {
            AsyncImage(
                model = typeImageUrl,
                contentDescription = typeName,
                modifier = Modifier.size(24.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(
                typeName,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}
