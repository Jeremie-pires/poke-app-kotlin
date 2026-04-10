package fr.sdv.mon_app_api.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import fr.sdv.mon_app_api.presentation.ui.MyError
import fr.sdv.mon_app_api.presentation.viewmodel.MainViewModel
import kotlin.random.Random

@Composable
fun WhosThatPokemonScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val gamePokemon = viewModel.pokemon.collectAsStateWithLifecycle().value
    val loading = viewModel.runInProgress.collectAsStateWithLifecycle().value
    val error = viewModel.errorMessage.collectAsStateWithLifecycle().value

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Who's that Pokemon ?", style = MaterialTheme.typography.headlineSmall)
        Text("Version API-first : on valide d'abord les donnees pour le futur jeu d'indices.")

        Button(
            onClick = { viewModel.loadPokemon(Random.nextInt(1, 1026).toString()) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Charger un Pokemon aleatoire")
        }

        MyError(errorMessage = error)

        if (loading) {
            CircularProgressIndicator()
        }

        if (gamePokemon != null) {
            Text("(Debug API) Reponse jeu recue:")
            Text("Nom : ${gamePokemon.name}")
            Text("Generation : -")
            Text("Types : ${gamePokemon.getTypeNames().joinToString(", ")}")
            Text("Cri URL : ${gamePokemon.cries.latest ?: gamePokemon.cries.legacy ?: "-"}")
            Text("Sprite URL : ${gamePokemon.sprites.frontDefault ?: "-"}")
        }
    }
}

