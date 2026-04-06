package com.artemiod.cursotestingandroid.productlist.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artemiod.cursotestingandroid.productlist.domain.model.SortOption
import com.artemiod.cursotestingandroid.productlist.presentation.ProductListUIState


@Composable
fun FiltersMenu(
    modifier: Modifier = Modifier,
    state: ProductListUIState.Success,
    onCategorySelected: (String?) -> Unit,
    onSortSelected: (SortOption) -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Categorías",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.selectedCategory == null,
                    onClick = { onCategorySelected(null) },
                    label = {
                        Text(
                            text = "Todas",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
                state.categories.forEach { category ->
                    FilterChip(
                        selected = category.equals(state.selectedCategory, ignoreCase = true),
                        onClick = { onCategorySelected(category) },
                        label = {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    )
                }
            }

            HorizontalDivider()
            Text(
                text = "Ordenar por",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.sortOption == SortOption.PRICE_ASC,
                    onClick = { onSortSelected(SortOption.PRICE_ASC) },
                    label = {
                        Text(
                            text = "Precio ↑",
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = state.sortOption == SortOption.PRICE_DESC,
                    onClick = { onSortSelected(SortOption.PRICE_DESC) },
                    label = {
                        Text(
                            text = "Precio ↓",
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = state.sortOption == SortOption.DISCOUNT,
                    onClick = { onSortSelected(SortOption.DISCOUNT) },
                    label = {
                        Text(
                            text = "Descuento",
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview
@Composable
fun FiltersMenuPreview() {
    val categories = listOf("Frutas", "Verduras", "Carnes", "Lácteos", "Panadería")
    FiltersMenu(
        state = ProductListUIState.Success(
            products = emptyList(),
            categories = categories,
            selectedCategory = null,
            sortOption = SortOption.NONE
        ),
        onCategorySelected = {},
        onSortSelected = {}
    )
}