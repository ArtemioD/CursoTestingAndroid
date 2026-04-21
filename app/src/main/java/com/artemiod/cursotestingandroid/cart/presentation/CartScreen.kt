package com.artemiod.cursotestingandroid.cart.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.artemiod.cursotestingandroid.cart.domain.model.CartSummary
import com.artemiod.cursotestingandroid.cart.presentation.model.CartItemWithPromotion
import com.artemiod.cursotestingandroid.core.presentation.components.MarketTopAppBar
import com.artemiod.cursotestingandroid.core.presentation.components.QuantitySelector
import com.artemiod.cursotestingandroid.productlist.domain.model.ProductPromotion
import java.text.NumberFormat
import java.util.Currency

@Composable
fun CartScreen(onBack: () -> Unit, cartViewModel: ContractCart = hiltViewModel<CartViewModel>()) {

    val uiState by cartViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        cartViewModel.event.collect { event ->
            when (event) {
                is CartEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = { MarketTopAppBar(title = "Carrito") { onBack() } },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (val state = uiState) {
            CartUiState.Loading -> {
                CartLoadingStateScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is CartUiState.Error -> {
                CartErrorStateScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    state = state,
                    onRetrySelected = { cartViewModel.refresh() }
                )
            }

            is CartUiState.Success -> {
                CartSuccessStateScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    state = state,
                    onIncreaseQuantity = { productId, quantity ->
                        cartViewModel.increaseQuantity(productId, quantity)
                    },
                    onDecreaseQuantity = { productId, quantity ->
                        cartViewModel.decreaseQuantity(productId, quantity)
                    },
                    onRemove = { id ->
                        cartViewModel.removeFromCart(id)
                    }
                )
            }
        }
    }
}

@Composable
fun CartSuccessStateScreen(
    modifier: Modifier = Modifier,
    state: CartUiState.Success,
    onIncreaseQuantity: (String, Int) -> Unit,
    onDecreaseQuantity: (String, Int) -> Unit,
    onRemove: (String) -> Unit,
) {

    val currencyFormatter = remember {
        NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance("USD")
        }
    }

    Column(modifier = modifier.padding(16.dp)) {
        AnimatedContent(state.cartItems.isEmpty()) { isEmpty ->
            if (isEmpty) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(54.dp))
                    Text(text = "\uD83D\uDED2", style = MaterialTheme.typography.displayLarge)
                    Text(
                        text = "Tu carrito está vacío", style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Agrega productos para empezar a comprar",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.surface
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.cartItems, key = { it.cartItem.productId }) { itemWithProduct ->
                        CartItemCard(
                            modifier = Modifier.animateItem(),
                            itemWithProduct = itemWithProduct,
                            currencyFormatter = currencyFormatter,
                            onIncreaseQuantity = { productId, quantity ->
                                onIncreaseQuantity(productId, quantity)
                            },
                            onDecreaseQuantity = { productId, quantity ->
                                onDecreaseQuantity(productId, quantity)
                            },
                            onRemove = { id -> onRemove(id) }
                        )
                    }
                }
            }
        }


        if (state.cartItems.isNotEmpty() && state.summary != null) {
            CartSummaryCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                summary = state.summary,
                currencyFormatter = currencyFormatter
            )
        }
    }
}

@Composable
fun CartSummaryCard(modifier: Modifier, summary: CartSummary, currencyFormatter: NumberFormat) {

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = "Resumen del carrito",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Subtotal",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(text = currencyFormatter.format(summary.subtotal),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            if (summary.discountTotal > 0) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Descuento",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(text = currencyFormatter.format(summary.discountTotal),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                thickness = 1.dp
            )

            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Total",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Text(text = currencyFormatter.format(summary.finalTotal),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

}

@Composable
fun CartItemCard(
    modifier: Modifier,
    itemWithProduct: CartItemWithPromotion,
    currencyFormatter: NumberFormat,
    onIncreaseQuantity: (String, Int) -> Unit,
    onDecreaseQuantity: (String, Int) -> Unit,
    onRemove: (string: String) -> Unit
) {
    val product = itemWithProduct.item.product
    val promotion = itemWithProduct.item.promotion
    val cartItem = itemWithProduct.cartItem

    val unitPrice = when (promotion) {
        is ProductPromotion.Percent -> promotion.discountedPrice
        is ProductPromotion.BuyXPayY -> product.price
        null -> product.price
    }

    val hasDiscount = promotion is ProductPromotion.Percent
    val itemTotal = unitPrice * cartItem.quantity

    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
            onRemove(cartItem.productId)
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    SwipeToDismissBox(
        modifier = modifier,
        state = dismissState,
        enableDismissFromEndToStart = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Eliminar",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)
                    .padding(8.dp),
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.imageUrl).crossfade(true) // Hace una transición suave
                        .build(),
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = android.R.drawable.ic_menu_report_image),
                    placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                    modifier = Modifier
                        .weight(1.5f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                )
                Spacer(modifier = Modifier.width(24.dp))
                Column(
                    modifier = Modifier.weight(3f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        if (hasDiscount) {
                            Text(
                                text = currencyFormatter.format(product.price),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textDecoration = TextDecoration.LineThrough
                            )
                            Text(
                                text = "${currencyFormatter.format(unitPrice)} c/u",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "${currencyFormatter.format(unitPrice)} c/u",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = "Total: ${currencyFormatter.format(itemTotal)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    QuantitySelector(
                        modifier = Modifier.background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ),
                        quantity = cartItem.quantity.toString(),
                        canDecrease = cartItem.quantity > 1,
                        canIncrease = cartItem.quantity < product.stock,
                        onIncreaseSelected = { onIncreaseQuantity(product.id, cartItem.quantity) },
                        onDecreaseSelected = { onDecreaseQuantity(product.id, cartItem.quantity) }
                    )
                }
            }
        }
    }
}

@Composable
fun CartErrorStateScreen(
    modifier: Modifier = Modifier,
    state: CartUiState.Error,
    onRetrySelected: () -> Unit
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error: ${state.message}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onRetrySelected() }) {
            Text(text = "Reintentar")
        }
    }
}

@Composable
fun CartLoadingStateScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

// *** Previews ***

@Preview
@Composable
fun CartScreenPreview() {
    CartScreen(cartViewModel = hiltViewModel<FakeCartViewModel>(), onBack = {})
}

@Preview(showBackground = true)
@Composable
fun CartErrorStateScreenPreview() {
    CartErrorStateScreen(
        state = CartUiState.Error("Error de prueba"),
        modifier = Modifier,
        onRetrySelected = {})
}


@Preview(showBackground = true)
@Composable
fun CartSuccessStateScreenPreviewEmptyCart() {
    CartSuccessStateScreen(
        state = CartUiState.Success(
            summary = CartSummary(subtotal = 20.0, discountTotal = 10.0, finalTotal = 10.0),
            cartItems = emptyList(),
            isLoading = false
        ),
        onIncreaseQuantity = { productId, quantity -> },
        onDecreaseQuantity = { productId, quantity -> },
        onRemove = { id -> }
    )
}

@Preview(showBackground = true)
@Composable
fun CartSuccessStateScreenPreview() {
    CartSuccessStateScreen(
        state = CartUiState.Success(
            summary = CartSummary(subtotal = 20.0, discountTotal = 10.0, finalTotal = 10.0),
            cartItems = cartItem(),
            isLoading = false,
        ),
        onIncreaseQuantity = { productId, quantity -> },
        onDecreaseQuantity = { productId, quantity -> },
        onRemove = { id -> }
    )
}


