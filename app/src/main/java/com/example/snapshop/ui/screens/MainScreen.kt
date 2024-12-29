package com.example.snapshop.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.snapshop.ui.navigation.MainNavHost
import com.example.snapshop.ui.navigation.Destinations


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val screensWithBottomNavBar = listOf(
        Destinations.HOME,
        Destinations.PROFILE,
        Destinations.CART,
        Destinations.COMPARE,
        Destinations.ORDERS,
        Destinations.PRODUCT_DETAIL
    )

    Scaffold(
        snackbarHost = { 
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            if (currentRoute in screensWithBottomNavBar) {
                BottomNavigationBar(navController = navController)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            MainNavHost(
                navController = navController,

            )
        }
    }
}
