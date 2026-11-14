package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.OrderRecord
import com.example.model.OrderStatus
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.formatRupees
import com.example.ui.theme.DealGreen
import com.example.ui.viewmodel.UiState

@Composable
fun OrdersScreen(
  ordersState: UiState<List<OrderRecord>>,
  onOrderClick: (String) -> Unit,
  onShopNowClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  when (ordersState) {
    UiState.Loading -> {
      Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {
        CircularProgressIndicator()
      }
    }
    is UiState.Error -> {
      EmptyStateCard(
        title = "Unable to load orders",
        subtitle = ordersState.message,
        buttonLabel = "Shop Now",
        onAction = onShopNowClick,
        modifier = modifier.fillMaxSize()
      )
    }
    is UiState.Success -> {
      val orders = ordersState.data
      if (orders.isEmpty()) {
        EmptyStateCard(
          title = "No Orders Placed Yet",
          subtitle = "Once you place an order, track your delivery live on Google Maps right here.",
          buttonLabel = "Explore Deals",
          onAction = onShopNowClick,
          modifier = modifier.fillMaxSize()
        )
      } else {
        LazyColumn(
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = modifier.fillMaxSize().statusBarsPadding()
        ) {
          item {
            Text(
              text = "My Orders (${orders.size})",
              style = MaterialTheme.typography.headlineSmall,
              modifier = Modifier.padding(bottom = 4.dp)
            )
          }
          items(orders, key = { it.id }) { order ->
            OrderSummaryCard(
              order = order,
              onClick = { onOrderClick(order.id) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun OrderSummaryCard(
  order: OrderRecord,
  onClick: () -> Unit
) {
  val status = OrderStatus.fromKey(order.status)
  val badgeColor = when (status) {
    OrderStatus.DELIVERED -> DealGreen
    OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
    OrderStatus.OUT_FOR_DELIVERY -> Color(0xFFD97706)
    else -> MaterialTheme.colorScheme.primary
  }

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("order_card_${order.id}")
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.LocalShipping,
            contentDescription = null,
            tint = badgeColor,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = order.orderNumber,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )
        }
        Surface(
          color = badgeColor.copy(alpha = 0.14f),
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = status.label,
            style = MaterialTheme.typography.labelMedium,
            color = badgeColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = order.itemsSummary,
        style = MaterialTheme.typography.bodyMedium,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${order.deliveryCity} • ${order.paymentMethod}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = formatRupees(order.totalAmount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Track order"
          )
        }
      }
    }
  }
}
