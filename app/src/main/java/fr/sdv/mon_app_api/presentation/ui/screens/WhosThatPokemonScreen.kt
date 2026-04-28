package fr.sdv.mon_app_api.presentation.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.sdv.mon_app_api.presentation.ui.MyError
import fr.sdv.mon_app_api.presentation.viewmodel.GameState
import fr.sdv.mon_app_api.presentation.viewmodel.GuessResult
import fr.sdv.mon_app_api.presentation.viewmodel.MainViewModel
import java.text.Normalizer

// Fonction pour normaliser les accents
fun String.removeAccents(): String {
    return Normalizer
        .normalize(this, Normalizer.Form.NFD)
        .replace(Regex("\\p{M}"), "")
}

@Composable
fun WhosThatPokemonScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val gameState = viewModel.gameState.collectAsStateWithLifecycle().value
    val gamePokemon = viewModel.gamePokemon.collectAsStateWithLifecycle().value
    val currentStep = viewModel.currentGameStep.collectAsStateWithLifecycle().value
    val userGuess = viewModel.userGuess.collectAsStateWithLifecycle().value
    val guessResult = viewModel.guessResult.collectAsStateWithLifecycle().value
    val attemptCount = viewModel.attemptCount.collectAsStateWithLifecycle().value
    val error = viewModel.errorMessage.collectAsStateWithLifecycle().value
    val pokemonNames = viewModel.pokemonNames.collectAsStateWithLifecycle().value
    val showAnswer = viewModel.showAnswer.collectAsStateWithLifecycle().value

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Who's that Pokemon ?",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )

            MyError(errorMessage = error)

            when (gameState) {
                GameState.Idle -> {
                    Button(
                        onClick = { viewModel.startNewGame() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935)
                        )
                    ) {
                        Text("🎮 Démarrer un nouveau jeu")
                    }
                }

                GameState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                GameState.Playing -> {
                    if (gamePokemon != null) {
                        GameContent(
                            gamePokemon = gamePokemon,
                            currentStep = currentStep,
                            userGuess = userGuess,
                            guessResult = guessResult,
                            attemptCount = attemptCount,
                            pokemonNames = pokemonNames,
                            showAnswer = showAnswer,
                            viewModel = viewModel
                        )
                    }
                }

                GameState.Won -> {
                    if (gamePokemon != null) {
                        WinScreen(
                            gamePokemon = gamePokemon,
                            attemptCount = attemptCount,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GameContent(
    gamePokemon: fr.sdv.mon_app_api.data.remote.PokemonGameEntity,
    currentStep: Int,
    userGuess: String,
    guessResult: GuessResult,
    attemptCount: Int,
    pokemonNames: List<String>,
    showAnswer: Boolean,
    viewModel: MainViewModel
) {
    var showSuggestions by remember { mutableStateOf(false) }

    // Affichage des étapes disponibles
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Étape 1: Types
        StepIndicator(stepNumber = 1, title = "Types", isActive = currentStep >= 1)
        if (currentStep >= 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                gamePokemon.types.forEach { typeName ->
                    TypeBadge(typeName = typeName, viewModel = viewModel)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Étape 2: Génération
        StepIndicator(stepNumber = 2, title = "Génération", isActive = currentStep >= 2)
        if (currentStep >= 2) {
            Text(
                "Génération: ${gamePokemon.generation.uppercase()}",
                modifier = Modifier.padding(start = 20.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Étape 3: Image (ombre ou claire si réponse affichée)
        StepIndicator(stepNumber = 3, title = "Silhouette", isActive = currentStep >= 3)
        if (currentStep >= 3 && gamePokemon.spriteUrl != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(start = 20.dp)
                    .background(Color(0xFF1A1A1A), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = gamePokemon.spriteUrl,
                    contentDescription = gamePokemon.name,
                    modifier = Modifier.size(100.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = if (showAnswer) null else ColorFilter.tint(Color.Black.copy(alpha = 0.8f))
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Barre de recherche pour le guess
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Tentative #$attemptCount - Qui est ce Pokémon ?",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = userGuess,
            onValueChange = { newValue ->
                viewModel.userGuess.value = newValue
                showSuggestions = newValue.isNotEmpty()
            },
            label = { Text("Votre réponse...", color = Color.White) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
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

        // Suggestions
        val suggestions = pokemonNames
            .filter { pokemonName ->
                val normalizedPokemonName = pokemonName.removeAccents().lowercase()
                val normalizedInput = userGuess.removeAccents().lowercase()
                normalizedPokemonName.startsWith(normalizedInput) || normalizedPokemonName.contains(normalizedInput)
            }
            .take(5)

        if (showSuggestions && suggestions.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2A2A2A), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                    .border(1.dp, Color(0xFF666666), RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 150.dp)
                ) {
                    items(suggestions) { suggestion ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.userGuess.value = suggestion
                                    showSuggestions = false
                                    viewModel.submitGuess(suggestion)
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

    Spacer(modifier = Modifier.height(12.dp))

    // Bouton Valider
    Button(
        onClick = { viewModel.submitGuess(userGuess) },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF1565C0)
        ),
        enabled = userGuess.isNotEmpty()
    ) {
        Text("✓ Valider la réponse")
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Bouton Afficher la réponse
    Button(
        onClick = { viewModel.showAnswer.value = true },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF666666)
        ),
        enabled = !showAnswer
    ) {
        Text("👁️ Afficher la réponse")
    }

    // Affichage de la réponse
    if (showAnswer) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2E7D32), RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "La réponse est :",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
                Text(
                    gamePokemon.name.uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
    when (guessResult) {
        is GuessResult.Incorrect -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFF6F00), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "❌ Incorrect !",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                    if (currentStep < 3) {
                        Text(
                            "Indice suivant révélé →",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }
        }
        is GuessResult.Empty -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF666666), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    "⚠️ Veuillez entrer un nom",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
        }
        else -> {}
    }
}

@Composable
private fun WinScreen(
    gamePokemon: fr.sdv.mon_app_api.data.remote.PokemonGameEntity,
    attemptCount: Int,
    viewModel: MainViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF4CAF50), RoundedCornerShape(12.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "🎉 Bravo ! 🎉",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Text(
            "C'était ${gamePokemon.name.uppercase()} !",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        if (gamePokemon.spriteUrl != null) {
            AsyncImage(
                model = gamePokemon.spriteUrl,
                contentDescription = gamePokemon.name,
                modifier = Modifier.size(120.dp),
                contentScale = ContentScale.Fit
            )
        }

        Text(
            "Trouvé en $attemptCount tentative${if (attemptCount > 1) "s" else ""} ! ",
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        when (attemptCount) {
            1 -> Text("💎 Parfait !", style = MaterialTheme.typography.bodyMedium, color = Color.White)
            2 -> Text("🌟 Excellent !", style = MaterialTheme.typography.bodyMedium, color = Color.White)
            3 -> Text("👍 Bien joué !", style = MaterialTheme.typography.bodyMedium, color = Color.White)
            else -> Text("💪 Pas mal !", style = MaterialTheme.typography.bodyMedium, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.startNewGame() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2E7D32)
            )
        ) {
            Text("🎮 Rejouer", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun StepIndicator(stepNumber: Int, title: String, isActive: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = if (isActive) Color(0xFF1565C0) else Color(0xFF555555),
                    shape = RoundedCornerShape(50)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$stepNumber",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge
            )
        }
        Text(
            title,
            color = if (isActive) Color.White else Color(0xFF999999),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun TypeBadge(typeName: String, viewModel: MainViewModel) {
    val typeImageUrl = viewModel.getTypeImageUrl(typeName)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0x44FFFFFF))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (typeImageUrl != null) {
            AsyncImage(
                model = typeImageUrl,
                contentDescription = typeName,
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(
                typeName,
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
        }
    }
}

