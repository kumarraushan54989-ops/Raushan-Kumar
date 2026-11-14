package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.OrderLineItem
import com.example.model.OrderRecord
import com.example.model.OrderStatus
import com.example.ui.components.OrderTrackingMapCard
import com.example.ui.components.formatRupees
import com.example.ui.theme.DealGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
  order: OrderRecord?,
  lineItems: List<OrderLineItem>,
  onAdvanceStatus: (OrderRecord) -> Unit,
  onCancelOrder: (OrderRecord) -> Unit,
  onBack: () -> Unit
) {
  BackHandler(onBack = onBack)

  Column(modifier = Modifier.fillMaxSize()) {
    TopAppBar(
      title = { Text(order?.orderNumber ?: "Order Tracking") },
      navigationIcon = {
        IconButton(onClick = onBack) {
          Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to orders")
        }
      }
    )

    if (order == null) {
      Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator()
      }
      return
    }

    val status = OrderStatus.fromKey(order.status)
    val isTerminal = status == OrderStatus.DELIVERED || status == OrderStatus.CANCELLED

    Column(
      verticalArrangement = Arrangement.spacedBy(14.dp),
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .navigationBarsPadding()
        .padding(16.dp)
    ) {
      OrderTrackingMapCard(order = order)

      // Order Status Timeline
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Delivery Timeline",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(12.dp))

          if (status == OrderStatus.CANCELLED) {
            Surface(
              color = MaterialTheme.colorScheme.errorContainer,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "This order has been cancelled.",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(12.dp)
              )
            }
          } else {
            val steps = listOf(
              OrderStatus.PLACED,
              OrderStatus.PACKED,
              OrderStatus.SHIPPED,
              OrderStatus.OUT_FOR_DELIVERY,
              OrderStatus.DELIVERED
            )
            val currentIdx = steps.indexOf(status).coerceAtLeast(0)
            steps.forEachIndexed { idx, step ->
              val done = idx <= currentIdx
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
              ) {
                Surface(
                  shape = CircleShape,
                  color = if (done) DealGreen else MaterialTheme.colorScheme.surfaceVariant,
                  modifier = Modifier.size(26.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    if (done) {
                      Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                      )
                    }
                  }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                  text = step.label,
                  style = MaterialTheme.typography.titleSmall,
                  color = if (done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                  fontWeight = if (idx == currentIdx) FontWeight.Bold else FontWeight.Normal
                )
              }
              if (idx < steps.lastIndex) {
                Box(
                  modifier = Modifier
                    .padding(start = 12.dp)
                    .width(2.dp)
                    .height(14.dp)
                    .background(if (idx < currentIdx) DealGreen else MaterialTheme.colorScheme.outline)
                )
              }
            }
          }

          if (!isTerminal) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Button(
                onClick = { onAdvanceStatus(order) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
                  .testTag("advance_order_status_button")
              ) {
                Icon(imageVector = Icons.Filled.FastForward, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Next Step", style = MaterialTheme.typography.labelLarge)
              }
              OutlinedButton(
                onClick = { onCancelOrder(order) },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .height(48.dp)
                  .testTag("cancel_order_button")
              ) {
                Icon(
                  imageVector = Icons.Filled.Cancel,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.error,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cancel", color = MaterialTheme.colorScheme.error)
              }
            }
          }
        }
      }

      // Items Ordered
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Items & Receipt", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          }
          Spacer(modifier = Modifier.height(10.dp))
          if (lineItems.isEmpty()) {
            Text(text = order.itemsSummary, style = MaterialTheme.typography.bodyMedium)
          } else {
            lineItems.forEach { item ->
              Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 6.dp)
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(text = item.title, style = MaterialTheme.typography.titleSmall)
                  Text(
                    text = "${item.brand} • Qty: ${item.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Text(
                  text = formatRupees(item.price * item.quantity),
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
          HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
          ReceiptLine("Deliver To", "${order.customerName} (${order.customerPhone})")
          ReceiptLine("Address", "${order.deliveryAddress}, ${order.deliveryCity} - ${order.deliveryPincode}")
          ReceiptLine("Payment", "${order.paymentMethod} • ${order.deliverySpeed}")
          if (order.discount > 0) {
            ReceiptLine("Coupon Savings (${order.couponCode})", "- ${formatRupees(order.discount)}")
          }
          ReceiptLine("Total Paid", formatRupees(order.totalAmount), bold = true)
        }
      }
    }
  }
}

@Composable
private fun ReceiptLine(label: String, value: String, bold: Boolean = false) {
  Row(
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp)
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.width(12.dp))
    Text(
      text = value,
      style = if (bold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodySmall,
      fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium
    )
  }
}
