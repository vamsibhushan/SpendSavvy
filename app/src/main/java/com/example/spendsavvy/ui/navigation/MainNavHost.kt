package com.example.spendsavvy.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.spendsavvy.ui.screens.*
import com.google.firebase.auth.FirebaseAuth

object Destinations {
    const val LOGIN = "login"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val NOTIFICATIONS = "notifications"
    const val SIGN_UP = "sign_up"
    const val ADD_EXPENSE = "add_expense"
    const val ADD_INCOME = "add_income"
    const val TRANSACTION_LIST = "transaction_list"
    const val STATS = "stats"
    const val LIMIT = "limit"

}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavHost(
    navController: NavHostController,
    startDestination: String = if (FirebaseAuth.getInstance().currentUser != null) Destinations.HOME else Destinations.LOGIN
) {
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
        composable(Destinations.TRANSACTION_LIST) {
            TransactionListScreen(navController = navController)
        }
        composable(Destinations.LIMIT){
            LimitScreen(navController = navController )
        }

        composable(Destinations.PROFILE) {
            ProfileScreen(onLogout = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate(Destinations.LOGIN) {
                    popUpTo(Destinations.HOME) { inclusive = true }
                }
            })
        }
        composable(Destinations.ADD_EXPENSE) {
            AddTransactionScreen(navController = navController, isIncome = false)
        }
        composable(Destinations.ADD_INCOME){
            AddTransactionScreen(navController = navController, isIncome = true)
        }
        composable(Destinations.STATS) {
            StatsScreen(navController = navController)
        }


        composable(Destinations.NOTIFICATIONS) {
            NotificationScreen(navController = navController)
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
    }
}
