package de.virtuellekueche.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.virtuellekueche.app.ui.auth.AuthViewModel
import de.virtuellekueche.app.ui.auth.LoginScreen
import de.virtuellekueche.app.ui.auth.SessionState
import de.virtuellekueche.app.ui.finance.FinanceScreen
import de.virtuellekueche.app.ui.kitchen.KitchenScreen
import de.virtuellekueche.app.ui.recipes.RecipesScreen
import de.virtuellekueche.app.ui.shopping.ShoppingListScreen

private enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    Kueche("kueche", "Küche", Icons.Outlined.Kitchen),
    Rezepte("rezepte", "Rezepte", Icons.AutoMirrored.Outlined.MenuBook),
    Einkauf("einkaufsliste", "Einkaufsliste", Icons.Outlined.ShoppingCart),
    Ausgaben("ausgaben", "Ausgaben", Icons.AutoMirrored.Outlined.ReceiptLong),
}

/** Zeigt je nach Anmeldestatus den Login oder die App mit unterer Navigation. */
@Composable
fun AppRoot(authViewModel: AuthViewModel = viewModel()) {
    val session by authViewModel.session.collectAsStateWithLifecycle()
    val loginState by authViewModel.ui.collectAsStateWithLifecycle()

    when (session) {
        SessionState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        SessionState.SignedOut -> LoginScreen(
            state = loginState,
            onSignIn = authViewModel::signIn,
            onRegister = authViewModel::register,
            onResetPassword = authViewModel::resetPassword,
        )
        is SessionState.SignedIn -> MainScaffold(onSignOut = authViewModel::signOut)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(onSignOut: () -> Unit) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val destination = backStack?.destination
    val currentTab = Tab.entries.firstOrNull { tab ->
        destination?.hierarchy?.any { it.route == tab.route } == true
    } ?: Tab.Kueche

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentTab.label) },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = "Abmelden")
                    }
                },
            )
        },
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = tab == currentTab,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Tab.Kueche.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(Tab.Kueche.route) { KitchenScreen() }
            composable(Tab.Rezepte.route) { RecipesScreen() }
            composable(Tab.Einkauf.route) { ShoppingListScreen() }
            composable(Tab.Ausgaben.route) { FinanceScreen() }
        }
    }
}
