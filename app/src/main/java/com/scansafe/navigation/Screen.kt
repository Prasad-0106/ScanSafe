package com.scansafe.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Scanner : Screen("scanner")
    object History : Screen("history")
    object Favourites : Screen("favourites")
    object Profile : Screen("profile")
    object ProductResult : Screen("product_result/{barcode}") {
        fun createRoute(barcode: String) = "product_result/$barcode"
    }
}
