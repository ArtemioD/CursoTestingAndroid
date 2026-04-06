package com.artemiod.cursotestingandroid.core.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.artemiod.cursotestingandroid.cart.presentation.CartScreen
import com.artemiod.cursotestingandroid.detail.presentation.ProductDetailScreen
import com.artemiod.cursotestingandroid.productlist.presentation.ProductListScreen
import com.artemiod.cursotestingandroid.settings.presentation.SettingsScreen


@Composable
fun NavGraph() {

    val backStack = rememberNavBackStack(Screen.ProductList)
    val entries = entryProvider<NavKey> {

        entry<Screen.ProductList> {
            ProductListScreen(
                navigateToSettings = { backStack.add(Screen.Setting) },
                navigateToProductDetail = { backStack.add(Screen.ProductDetail(productId = it)) },
                navigateToCart = { backStack.add(Screen.Cart) }
            )
        }

        entry<Screen.Cart> {
            CartScreen(onBack = { backStack.removeLastOrNull() })
        }

        entry<Screen.Setting> {
            SettingsScreen(onBack = { backStack.removeLastOrNull() })
        }

        entry<Screen.ProductDetail> { route ->
            ProductDetailScreen(
                productId = route.productId,
                onBack = { backStack.removeLastOrNull() })
        }
    }

    NavDisplay(
        backStack = backStack,
        entryProvider = entries,
        onBack = { backStack.removeLastOrNull() }
    )

}