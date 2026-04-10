package fr.sdv.mon_app_api.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.sdv.mon_app_api.data.remote.KtorPokeApi
import fr.sdv.mon_app_api.data.remote.PokemonEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    // Etat observable du Pokemon charge
    val pokemon = MutableStateFlow<PokemonEntity?>(null)
    val runInProgress = MutableStateFlow(false)
    val errorMessage = MutableStateFlow("")

    fun loadPokemon(pokemonName: String) {
        runInProgress.value = true
        errorMessage.value = ""

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (pokemonName.isBlank()) {
                    throw Exception("Il faut renseigner un nom ou un id de Pokemon")
                }

                pokemon.value = KtorPokeApi.loadPokemon(pokemonName)

            } catch (e: Exception) {
                e.printStackTrace()
                pokemon.value = null
                errorMessage.value = e.message ?: "Une erreur générique"
            } finally {
                runInProgress.value = false
            }
        }
    }
}