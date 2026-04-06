package com.artemiod.cursotestingandroid.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun QuantitySelector(
    modifier: Modifier = Modifier,
    quantity: String,
    canDecrease: Boolean,
    canIncrease: Boolean,
    onIncreaseSelected: () -> Unit,
    onDecreaseSelected: () -> Unit
) {

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(
            modifier = Modifier.size(36.dp),
            enabled = canDecrease,
            onClick = { onDecreaseSelected() }
        ) {
            Icon(
                imageVector = Icons.Outlined.Remove,
                contentDescription = "Decrease quantity",
                modifier = Modifier.size(20.dp)
            )
        }

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = quantity,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        IconButton(
            modifier = Modifier.size(36.dp),
            enabled = canIncrease,
            onClick = { onIncreaseSelected() }
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "increase quantity",
                modifier = Modifier.size(20.dp)
            )
        }
    }

}

@Preview(showBackground = true)
@Composable
fun QuantitySelectorPreview() {
    QuantitySelector(
        quantity = "1",
        canDecrease = true,
        canIncrease = false,
        onIncreaseSelected = {},
        onDecreaseSelected = {})
}
