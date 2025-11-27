package com.example.spendsavvy.ui.screens

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.spendsavvy.ui.navigation.Destinations
import com.example.spendsavvy.ui.navigation.MainNavHost

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(intent: Intent) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route


    // Check if the activity was launched from a notification
    val navigateTo = intent.getStringExtra("navigate_to")
    navigateTo?.let {
        if (it == Destinations.NOTIFICATIONS) {
            // Ensure that the navigation graph is set before navigating
            LaunchedEffect(Unit) {
                navController.navigate(Destinations.NOTIFICATIONS)
            }
        }
    }

    // List of screens that should show the bottom navigation bar
    val screensWithBottomNavBar = listOf(
        Destinations.HOME,
        Destinations.PROFILE,
        Destinations.NOTIFICATIONS,
        Destinations.ADD_EXPENSE,
        Destinations.ADD_INCOME,
        Destinations.TRANSACTION_LIST,
        Destinations.STATS,
        Destinations.LIMIT
    )

    Scaffold(bottomBar = {
        if (currentRoute in screensWithBottomNavBar) {
            BottomNavigationBar(navController = navController)
        }
    }) { innerPadding ->
        Box(
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            MainNavHost(navController = navController)

        }


    }
}

 