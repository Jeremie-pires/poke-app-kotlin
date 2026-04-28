package fr.sdv.mon_app_api.data.local

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PokemonNameEntry(
    val id: Int,
    val name_en: String,
    val name_fr: String
)

@Serializable
data class PokedexData(
    val pokemon: List<PokemonNameEntry>
)

@Serializable
data class TypeEntry(
    val name: String,
    val image: String
)

@Serializable
data class TypesData(
    val types: List<TypeEntry>
)

class PokedexRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    
    private var cachedPokemonNames: List<PokemonNameEntry>? = null
    private var cachedTypes: List<TypeEntry>? = null
    
    fun loadPokemonNamesFr(): List<PokemonNameEntry> {
        if (cachedPokemonNames != null) {
            return cachedPokemonNames!!
        }
        
        return try {
            val inputStream = context.assets.open("pokedex_fr.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val data = json.decodeFromString<PokedexData>(jsonString)
            cachedPokemonNames = data.pokemon
            data.pokemon
        } catch (e: Exception) {
            e.printStackTrace()
            println("Erreur chargement pokedex_fr.json: ${e.message}")
            emptyList()
        }
    }
    
    fun loadTypes(): List<TypeEntry> {
        if (cachedTypes != null) {
            return cachedTypes!!
        }
        
        return try {
            val inputStream = context.assets.open("types.json")
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val data = json.decodeFromString<TypesData>(jsonString)
            cachedTypes = data.types
            data.types
        } catch (e: Exception) {
            e.printStackTrace()
            println("Erreur chargement types.json: ${e.message}")
            emptyList()
        }
    }
    
    fun getTypeImageUrl(typeName: String): String? {
        return try {
            val types = loadTypes()
            val frenchTypeName = englishToFrenchTypeMap[typeName.lowercase()] ?: typeName
            types.find { it.name.equals(frenchTypeName, ignoreCase = true) }?.image
        } catch (e: Exception) {
            null
        }
    }
    
    companion object {
        private val englishToFrenchTypeMap = mapOf(
            "normal" to "Normal",
            "fighting" to "Combat",
            "flying" to "Vol",
            "poison" to "Poison",
            "ground" to "Sol",
            "rock" to "Roche",
            "bug" to "Insecte",
            "ghost" to "Spectre",
            "steel" to "Acier",
            "fire" to "Feu",
            "water" to "Eau",
            "grass" to "Plante",
            "electric" to "Électrik",
            "psychic" to "Psy",
            "ice" to "Glace",
            "dragon" to "Dragon",
            "dark" to "Ténèbres",
            "fairy" to "Fée"
        )
    }
}


