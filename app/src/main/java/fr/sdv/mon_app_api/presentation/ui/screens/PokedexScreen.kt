package fr.sdv.mon_app_api.presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
    val pokemon = viewModel.pokemon.collectAsStateWithLifecycle().value
    val loading = viewModel.runInProgress.collectAsStateWithLifecycle().value
    val error = viewModel.errorMessage.collectAsStateWithLifecycle().value

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.pokedex),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Pokedex", style = MaterialTheme.typography.headlineSmall, color = Color.White)

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Nom ou ID (ex: ditto, 132)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { viewModel.loadPokemon(query) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Rechercher")
            }

            MyError(errorMessage = error)

            if (loading) {
                CircularProgressIndicator()
            }

            if (pokemon != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xCC111111))
                        .border(2.dp, Color.White, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (pokemon.sprites.frontDefault != null) {
                        AsyncImage(
                            model = pokemon.sprites.frontDefault,
                            contentDescription = pokemon.name,
                            modifier = Modifier.size(180.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text("Image indisponible", color = Color.White)
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        modifier = Modifier.height(36.dp),
                        onClick = { selectedTab = PokedexTab.INFO }
                    ) {
                        Text("Infos")
                    }
                    Button(
                        modifier = Modifier.height(36.dp),
                        onClick = { selectedTab = PokedexTab.STATS }
                    ) {
                        Text("Stats")
                    }
                }

                when (selectedTab) {
                    PokedexTab.INFO -> {
                        Text("Nom : ${pokemon.name}", color = Color.White)
                        Text("Pokedex ID : ${pokemon.id}", color = Color.White)
                        Text("Types : ${pokemon.getTypeNames().joinToString(", ")}", color = Color.White)
                        Text("Taille : ${pokemon.height}", color = Color.White)
                        Text("Poids : ${pokemon.weight}", color = Color.White)
                    }

                    PokedexTab.STATS -> {
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

@Composable
private fun StatBarRow(name: String, value: Int) {
    val normalized = value.coerceIn(0, 255)
    val ratio = normalized / 255f

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("$name : $value", color = Color.White)
        Box(
            modifier = Modifier
                .width(255.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0x33000000))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(ratio)
                    .height(12.dp)
                    .background(statColor(normalized))
            )
        }
    }
}

private fun statColor(value: Int): Color = when {
    value < 30 -> Color(0xFF8B0000) // rouge sombre
    value < 60 -> Color(0xFFFF0000) // rouge
    value < 90 -> Color(0xFFFF6A00) // orange sombre
    value < 120 -> Color(0xFFFF9800) // orange
    value < 150 -> Color(0xFFFFEB3B) // jaune
    value < 180 -> Color(0xFF2E7D32) // vert sombre
    value < 210 -> Color(0xFF4CAF50) // vert
    value < 240 -> Color(0xFF1565C0) // bleu sombre
    else -> Color(0xFF64B5F6) // bleu clair
}

