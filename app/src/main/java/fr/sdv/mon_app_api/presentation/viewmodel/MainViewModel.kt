package fr.sdv.mon_app_api.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.sdv.mon_app_api.data.local.PokedexRepository
import fr.sdv.mon_app_api.data.local.TypeEntry
import fr.sdv.mon_app_api.data.remote.KtorPokeApi
import fr.sdv.mon_app_api.data.remote.PokemonEntity
import fr.sdv.mon_app_api.data.remote.PokemonGameEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.Normalizer

class MainViewModel(context: Context) : ViewModel() {
    private val pokedexRepository = PokedexRepository(context)

    // Etat observable du Pokemon charge
    val pokemon = MutableStateFlow<PokemonEntity?>(null)
    val runInProgress = MutableStateFlow(false)
    val errorMessage = MutableStateFlow("")

    // Liste des noms de Pokemon en français
    val pokemonNames = MutableStateFlow<List<String>>(emptyList())
    
    // Table des types avec leurs images
    val types = MutableStateFlow<List<TypeEntry>>(emptyList())

    // État du jeu "Who's that Pokemon?"
    val gameState = MutableStateFlow<GameState>(GameState.Idle)
    val gamePokemon = MutableStateFlow<PokemonGameEntity?>(null)
    val currentGameStep = MutableStateFlow(1) // 1 = Types, 2 = Generation, 3 = Image
    val userGuess = MutableStateFlow("")
    val guessResult = MutableStateFlow<GuessResult>(GuessResult.None)
    val attemptCount = MutableStateFlow(0)
    val showAnswer = MutableStateFlow(false)

    init {
        loadPokemonNamesFr()
        loadTypes()
    }

    private fun loadPokemonNamesFr() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val frenchNames = pokedexRepository.loadPokemonNamesFr()
                    .map { it.name_fr }
                    .sorted()
                pokemonNames.value = frenchNames
            } catch (e: Exception) {
                e.printStackTrace()
                println("Erreur chargement liste Pokemon: ${e.message}")
            }
        }
    }
    
    private fun loadTypes() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                types.value = pokedexRepository.loadTypes()
            } catch (e: Exception) {
                e.printStackTrace()
                println("Erreur chargement types: ${e.message}")
            }
        }
    }
    
    fun getTypeImageUrl(typeName: String): String? {
        return pokedexRepository.getTypeImageUrl(typeName)
    }

    fun getPokemonFrenchName(pokemonEnName: String): String {
        return pokedexRepository.loadPokemonNamesFr()
            .find { it.name_en.equals(pokemonEnName, ignoreCase = true) }?.name_fr
            ?: pokemonEnName.replaceFirstChar { it.uppercaseChar() }
    }

    fun loadPokemon(pokemonNameFr: String) {
        runInProgress.value = true
        errorMessage.value = ""

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (pokemonNameFr.isBlank()) {
                    throw Exception("Il faut renseigner un nom ou un id de Pokemon")
                }

                // Trouver le nom anglais correspondant via la table JSON
                val allPokemon = pokedexRepository.loadPokemonNamesFr()
                val pokemonEnName = allPokemon.find { it.name_fr.equals(pokemonNameFr, ignoreCase = true) }?.name_en
                    ?: pokemonNameFr.lowercase()

                pokemon.value = KtorPokeApi.loadPokemon(pokemonEnName)

            } catch (e: Exception) {
                e.printStackTrace()
                pokemon.value = null
                errorMessage.value = e.message ?: "Une erreur générique"
            } finally {
                runInProgress.value = false
            }
        }
    }

    // Fonctions pour le jeu "Who's that Pokemon?"
    fun startNewGame() {
        gameState.value = GameState.Loading
        errorMessage.value = ""
        currentGameStep.value = 1
        userGuess.value = ""
        guessResult.value = GuessResult.None
        attemptCount.value = 0

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val randomPokemon = KtorPokeApi.loadRandomPokemonGameData()
                gamePokemon.value = randomPokemon
                gameState.value = GameState.Playing
            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage.value = e.message ?: "Erreur lors du chargement du Pokémon"
                gameState.value = GameState.Idle
            }
        }
    }

    fun submitGuess(guessNameFr: String) {
        if (guessNameFr.isBlank()) {
            guessResult.value = GuessResult.Empty
            return
        }

        val current = gamePokemon.value ?: return
        val allPokemon = pokedexRepository.loadPokemonNamesFr()

        // Trouver le nom anglais du guess avec normalisation des accents
        val guessEnName = allPokemon.find {
            it.name_fr.removeAccents().equals(guessNameFr.removeAccents(), ignoreCase = true)
        }?.name_en ?: guessNameFr.lowercase()

        attemptCount.value += 1

        // Vérifier si c'est correct avec normalisation
        if (guessEnName.removeAccents().equals(current.name.removeAccents(), ignoreCase = true)) {
            guessResult.value = GuessResult.Correct(attemptCount.value)
            gameState.value = GameState.Won
        } else {
            guessResult.value = GuessResult.Incorrect
            // Avancer d'une étape si le guess est faux
            if (currentGameStep.value < 3) {
                currentGameStep.value += 1
            }
        }
        userGuess.value = ""
    }

    fun nextStep() {
        if (currentGameStep.value < 3) {
            currentGameStep.value += 1
        }
    }

    fun resetGame() {
        gameState.value = GameState.Idle
        gamePokemon.value = null
        currentGameStep.value = 1
        userGuess.value = ""
        guessResult.value = GuessResult.None
        attemptCount.value = 0
        showAnswer.value = false
    }

    private fun String.removeAccents(): String {
        return Normalizer
            .normalize(this, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
    }
}

enum class GameState {
    Idle, Loading, Playing, Won
}

sealed class GuessResult {
    object None : GuessResult()
    object Empty : GuessResult()
    object Incorrect : GuessResult()
    data class Correct(val attempts: Int) : GuessResult()
}