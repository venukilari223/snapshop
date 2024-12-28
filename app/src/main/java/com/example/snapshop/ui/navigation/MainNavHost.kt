package com.example.snapshop.ui.navigation

import ProductDetailScreen
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

import com.example.snapshop.ui.screens.*
import com.google.firebase.auth.FirebaseAuth

import kotlinx.coroutines.CoroutineScope

object Destinations {
    const val LOGIN = "login"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val SIGN_UP = "sign_up"
    const val CART = "cart"
    const val COMPARE = "compare"
    const val ORDERS = "orders"
    const val PRODUCT_DETAIL = "product/{productId}"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavHost(
    navController: NavHostController,
    startDestination: String = if (FirebaseAuth.getInstance().currentUser != null) 
        Destinations.HOME else Destinations.LOGIN
) {
    val auth = FirebaseAuth.getInstance()

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Destinations.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.LOGIN) { inclusive = true }
                    }
                },
                onSignUp = {
                    navController.navigate(Destinations.SIGN_UP)
                }
            )
        }


        composable(Destinations.HOME) {
            HomeScreen(navController = navController)
        }

        composable(Destinations.PROFILE) {
            ProfileScreen(
                onLogout = {
                    auth.signOut()
                    navController.navigate(Destinations.LOGIN) {
                        popUpTo(Destinations.HOME) { inclusive = true }
                    }
                },
                navController = navController
            )
        }


        composable(Destinations.SIGN_UP) {
            SignUpScreen(
                onSignUpSuccess = {
                    navController.navigate(Destinations.HOME) {
                        popUpTo(Destinations.SIGN_UP) { inclusive = true }
                    }
                },
                navController = navController
            )
        }

        composable(Destinations.CART) {
            CartScreen(
                navController = navController,
                onOrderPlaced = {
                    println("Debug: Order placed, navigating to orders screen")
                    navController.navigate(Destinations.ORDERS) {
                        popUpTo(Destinations.CART) { inclusive = true }
                    }
                }
            )
        }

        composable(Destinations.COMPARE) {
            CompareScreen(navController = navController)
        }

        composable(Destinations.ORDERS) {
            println("Debug: Composing OrderScreen")
            OrderScreen(navController = navController)
        }

        composable(
            route = "product/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: return@composable
            ProductDetailScreen(
                productId = productId,
                navController = navController
            )
        }
    }
}
