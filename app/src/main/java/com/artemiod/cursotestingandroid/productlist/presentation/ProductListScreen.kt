package com.artemiod.cursotestingandroid.productlist.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artemiod.cursotestingandroid.cart.presentation.CartUiState
import com.artemiod.cursotestingandroid.cart.presentation.CartViewModel
import com.artemiod.cursotestingandroid.cart.presentation.ContractCart
import com.artemiod.cursotestingandroid.cart.presentation.FakeCartViewModel
import com.artemiod.cursotestingandroid.productlist.presentation.components.FiltersMenu
import com.artemiod.cursotestingandroid.productlist.presentation.components.HomeTopAppBar
import com.artemiod.cursotestingandroid.productlist.presentation.components.ProductItem

@Composable
fun ProductListScreen(
    productListViewModel: ContractProductList = hiltViewModel<ProductListViewModel>(),
    cartViewModel: ContractCart = hiltViewModel<CartViewModel>(),
    navigateToSettings: () -> Unit,
    navigateToProductDetail: (String) -> Unit,
    navigateToCart: () -> Unit
) {

    val uiState by productListViewModel.uiState.collectAsStateWithLifecycle()
    val cartUiState by cartViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val filtersVisible by productListViewModel.filterVisible.collectAsStateWithLifecycle()

    val cartItemCount = remember(cartUiState) {
        when (val state =cartUiState) {
            is CartUiState.Success -> {
                state.cartItems.sumOf { it.cartItem.quantity }
            }
            else -> 0
        }
    }


    // para mostrar eventos
    LaunchedEffect(Unit) {
        productListViewModel.events.collect { event ->
            when (event) {
                is ProductListEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            HomeTopAppBar(
                filtersVisible = filtersVisible,
                cartItemCount = cartItemCount,
                onFilterSelected = { showFilters ->
                    productListViewModel.setFilterVisible(showFilters)
                },
                onSettingsSelected = { navigateToSettings() },
                onCartSelected = { navigateToCart() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        when (val state = uiState) {
            is ProductListUIState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ProductListUIState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Error: ${state.message}",
                        fontSize = 30.sp,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            is ProductListUIState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    AnimatedVisibility(
                        visible = filtersVisible,
                        // animaciones para la visibilidad
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        FiltersMenu(
                            state = state,
                            onCategorySelected = { category ->
                                productListViewModel.setCategory(category)
                            },
                            onSortSelected = { sortOption ->
                                productListViewModel.setSortOption(sortOption)
                            }
                        )
                    }
                    Text(
                        text = "${state.products.size} productos",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    if (state.products.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "\uD83D\uDD0D ",
                                    style = MaterialTheme.typography.displayMedium
                                )
                                Text(
                                    text = "NO HAY PRODUCTOS",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.tertiary
                                )

                            }
                        }
                    } else {
                        LazyColumn {
                            items(state.products) { item ->
                                ProductItem(
                                    item = item,
                                    onClick = { navigateToProductDetail(it.product.id) }
                                )
                            }
                        }
                    }

                }
            }

        }
    }
}

@Preview
@Composable
fun ProductListScreenPreview() {
    ProductListScreen(
        productListViewModel = hiltViewModel<FakeProductListViewModel>(),
        cartViewModel = hiltViewModel<FakeCartViewModel>(),
        navigateToSettings = {},
        navigateToProductDetail = {},
        navigateToCart = {}
    )
}