package fr.sdv.mon_app_api.data.remote

import io.ktor.client.call.body
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.random.Random

object KtorPokeApi {
    private const val POKEMON_URL = "https://pokeapi.co/api/v2/pokemon/"
    private const val SPECIES_URL = "https://pokeapi.co/api/v2/pokemon-species/"
    private const val MAX_POKEMON_ID = 1025

    private val client = HttpClient {
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println(message)
                }
            }
            level = LogLevel.INFO
        }
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true }, contentType = ContentType.Any)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 5000
        }
    }

    suspend fun loadPokemon(pokemonName: String): PokemonEntity {
        val query = pokemonName.trim().lowercase()
        require(query.isNotBlank()) { "Le nom ou l'id du Pokemon ne peut pas etre vide." }

        return client.get(POKEMON_URL + query).body()
    }

    suspend fun loadPokemonSpecies(pokemonId: Int): PokemonSpeciesEntity {
        require(pokemonId > 0) { "L'id du Pokemon doit etre > 0." }
        return client.get(SPECIES_URL + pokemonId).body()
    }

    suspend fun loadRandomPokemonGameData(): PokemonGameEntity {
        val randomId = Random.nextInt(1, MAX_POKEMON_ID + 1)
        val pokemon = loadPokemon(randomId.toString())
        val species = loadPokemonSpecies(pokemon.id)

        return PokemonGameEntity(
            id = pokemon.id,
            name = pokemon.name,
            types = pokemon.getTypeNames(),
            cryUrl = pokemon.cries.latest ?: pokemon.cries.legacy,
            generation = species.generation.name,
            spriteUrl = pokemon.sprites.frontDefault
        )
    }
}

@Serializable
data class PokemonEntity(
    val name: String,
    val id: Int,
    val height: Int,
    val weight: Int,
    val stats: List<PokemonStatEntity>,
    val types: List<PokemonTypeEntity>,
    val cries: PokemonCriesEntity,
    val sprites: PokemonSpritesEntity
) {
    fun getBaseStats(): Map<String, Int> = stats.associate { it.stat.name to it.baseStat }

    fun getTypeNames(): List<String> = types.sortedBy { it.slot }.map { it.type.name }

    fun getResume() = """
        Pokemon: $name (#$id)
        Taille: $height | Poids: $weight
        Types: ${getTypeNames().joinToString(", ").ifBlank { "-" }}
        Stats de base: ${getBaseStats().entries.joinToString { "${it.key}=${it.value}" }}
    """.trimIndent()
}

@Serializable
data class PokemonCriesEntity(
    val latest: String? = null,
    val legacy: String? = null
)

@Serializable
data class PokemonSpritesEntity(
    @SerialName("front_default") val frontDefault: String? = null
)

@Serializable
data class PokemonStatEntity(
    @SerialName("base_stat") val baseStat: Int,
    val stat: NamedApiResource
)

@Serializable
data class PokemonTypeEntity(
    val slot: Int,
    val type: NamedApiResource
)

@Serializable
data class NamedApiResource(
    val name: String,
    val url: String
)

@Serializable
data class PokemonSpeciesEntity(
    val generation: NamedApiResource
)

data class PokemonGameEntity(
    val id: Int,
    val name: String,
    val types: List<String>,
    val cryUrl: String?,
    val generation: String,
    val spriteUrl: String?
)

