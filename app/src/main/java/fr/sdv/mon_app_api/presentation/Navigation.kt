package fr.sdv.mon_app_api.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CatchingPokemon
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.padding
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import fr.sdv.mon_app_api.presentation.ui.screens.PokedexScreen
import fr.sdv.mon_app_api.presentation.ui.screens.WhosThatPokemonScreen
import fr.sdv.mon_app_api.presentation.viewmodel.MainViewModel
import fr.sdv.mon_app_api.presentation.viewmodel.MainViewModelFactory

private data class FooterDestination(val route: String, val label: String)

private val pokedexDestination = FooterDestination(route = "pokedex", label = "Pokedex")
private val gameDestination = FooterDestination(route = "whos_that_pokemon", label = "Who's that Pokemon ?")
private val footerDestinations = listOf(pokedexDestination, gameDestination)

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {

    val navController = rememberNavController()
    val context = LocalContext.current
    val mainViewModel: MainViewModel = viewModel(factory = MainViewModelFactory(context))
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                footerDestinations.forEach { destination ->
                    val selected = destination.route == currentRoute
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                launchSingleTop = true
                            }
                        },
                        icon = {
                            if (destination.route == pokedexDestination.route) {
                                Icon(Icons.Default.CatchingPokemon, contentDescription = destination.label)
                            } else {
                                Icon(Icons.Default.Help, contentDescription = destination.label)
                            }
                        },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = pokedexDestination.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(pokedexDestination.route) {
                PokedexScreen(viewModel = mainViewModel)
            }
            composable(gameDestination.route) {
                WhosThatPokemonScreen(viewModel = mainViewModel)
            }
        }
    }
}