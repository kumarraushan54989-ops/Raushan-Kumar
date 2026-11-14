package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CatalogData
import com.example.model.CartItem
import com.example.model.DeliveryAddress
import com.example.model.ProductCategory
import com.example.ui.components.DeliveryPinMapPicker
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ProductVisualArtwork
import com.example.ui.components.formatRupees
import com.example.ui.theme.DealGreen
import com.example.ui.theme.SunburstGold
import com.example.ui.viewmodel.PriceBreakdown
import com.example.ui.viewmodel.UiState

@Composable
fun CartScreen(
  cartState: UiState<List<CartItem>>,
  priceBreakdown: PriceBreakdown,
  appliedCoupon: String,
  activeAddress: DeliveryAddress?,
  onIncrement: (CartItem) -> Unit,
  onDecrement: (CartItem) -> Unit,
  onRemove: (CartItem) -> Unit,
  onApplyCoupon: (String) -> Unit,
  onClearCoupon: () -> Unit,
  onChangeAddressClick: () -> Unit,
  onCheckoutClick: () -> Unit,
  onExploreClick: () -> Unit,
  productPhotos: Map<String, String> = emptyMap(),
  modifier: Modifier = Modifier
) {
  var couponInput by remember(appliedCoupon) { mutableStateOf(appliedCoupon) }

  when (cartState) {
    UiState.Loading -> {
      Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {
        CircularProgressIndicator()
      }
    }
    is UiState.Error -> {
      EmptyStateCard(
        title = "Could not load cart",
        subtitle = cartState.message,
        buttonLabel = "Browse Store",
        onAction = onExploreClick,
        modifier = modifier.fillMaxSize()
      )
    }
    is UiState.Success -> {
      val items = cartState.data
      if (items.isEmpty()) {
        EmptyStateCard(
          title = "Your QuickKart Bag is Empty",
          subtitle = "Add smartphones, audio gear, sneakers, or daily essentials to place your order.",
          buttonLabel = "Shop Deals Now",
          onAction = onExploreClick,
          modifier = modifier.fillMaxSize()
        )
      } else {
        Column(modifier = modifier.fillMaxSize()) {
          Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = if (activeAddress != null) {
                    "Deliver to ${activeAddress.label}: ${activeAddress.city} - ${activeAddress.pincode}"
                  } else {
                    "No delivery pin selected yet"
                  },
                  style = MaterialTheme.typography.titleSmall,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = activeAddress?.streetAddress ?: "Tap Change to pin your address on Google Maps",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
              OutlinedButton(
                onClick = onChangeAddressClick,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(48.dp)
              ) {
                Text(if (activeAddress == null) "Pin Map" else "Change")
              }
            }
          }

          LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
          ) {
            items(items, key = { it.id }) { item ->
              CartItemCard(
                item = item,
                photoBase64 = productPhotos[item.productId].orEmpty(),
                onIncrement = { onIncrement(item) },
                onDecrement = { onDecrement(item) },
                onRemove = { onRemove(item) }
              )
            }

            item {
              Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
              ) {
                Column(modifier = Modifier.padding(14.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Filled.LocalOffer,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Apply Super Coupon", style = MaterialTheme.typography.titleSmall)
                  }
                  Spacer(modifier = Modifier.height(8.dp))
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    OutlinedTextField(
                      value = couponInput,
                      onValueChange = { couponInput = it.uppercase() },
                      placeholder = { Text("Enter QUICK10") },
                      singleLine = true,
                      shape = RoundedCornerShape(10.dp),
                      modifier = Modifier
                        .weight(1f)
                        .testTag("coupon_input")
                    )
                    Button(
                      onClick = {
                        if (appliedCoupon.isNotEmpty() && couponInput == appliedCoupon) {
                          onClearCoupon()
                          couponInput = ""
                        } else {
                          onApplyCoupon(couponInput)
                        }
                      },
                      shape = RoundedCornerShape(10.dp),
                      modifier = Modifier
                        .height(52.dp)
                        .testTag("apply_coupon_button")
                    ) {
                      Text(if (appliedCoupon.isNotEmpty() && couponInput == appliedCoupon) "Remove" else "Apply")
                    }
                  }
                  Spacer(modifier = Modifier.height(8.dp))
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CatalogData.promoCoupons.keys.forEach { code ->
                      AssistChip(
                        onClick = {
                          couponInput = code
                          onApplyCoupon(code)
                        },
                        label = { Text(code, style = MaterialTheme.typography.labelSmall) }
                      )
                    }
                  }
                }
              }
            }

            item {
              PriceSummaryCard(priceBreakdown = priceBreakdown)
            }
          }

          // Sticky Place Order Footer
          Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 10.dp,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
              Column {
                Text(
                  text = formatRupees(priceBreakdown.totalPayable),
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.Bold
                )
                if (priceBreakdown.totalSavings > 0) {
                  Text(
                    text = "Saving ${formatRupees(priceBreakdown.totalSavings)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = DealGreen,
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }
              Button(
                onClick = onCheckoutClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = SunburstGold,
                  contentColor = Color(0xFF172337)
                ),
                modifier = Modifier
                  .height(52.dp)
                  .testTag("proceed_checkout_button")
              ) {
                Text(
                  text = "Proceed to Checkout",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CartItemCard(
  item: CartItem,
  onIncrement: () -> Unit,
  onDecrement: () -> Unit,
  onRemove: () -> Unit,
  photoBase64: String = ""
) {
  val cat = ProductCategory.entries.firstOrNull { it.label.equals(item.category, ignoreCase = true) }
    ?: ProductCategory.ELECTRONICS
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(84.dp)
          .clip(RoundedCornerShape(12.dp))
      ) {
        ProductVisualArtwork(
          imageKey = item.imageKey,
          category = cat,
          photoBase64 = photoBase64,
          modifier = Modifier.fillMaxSize()
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = item.brand,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary
        )
        Text(
          text = item.title,
          style = MaterialTheme.typography.titleSmall,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = formatRupees(item.price),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          if (item.mrp > item.price) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = formatRupees(item.mrp),
              style = MaterialTheme.typography.bodySmall,
              textDecoration = TextDecoration.LineThrough,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              IconButton(
                onClick = onDecrement,
                modifier = Modifier.size(48.dp)
              ) {
                Icon(imageVector = Icons.Filled.Remove, contentDescription = "Decrease quantity")
              }
              Text(
                text = "${item.quantity}",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 6.dp)
              )
              IconButton(
                onClick = onIncrement,
                modifier = Modifier.size(48.dp)
              ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Increase quantity")
              }
            }
          }
          IconButton(onClick = onRemove, modifier = Modifier.size(48.dp)) {
            Icon(
              imageVector = Icons.Filled.DeleteOutline,
              contentDescription = "Remove from cart",
              tint = MaterialTheme.colorScheme.error
            )
          }
        }
      }
    }
  }
}

@Composable
fun PriceSummaryCard(priceBreakdown: PriceBreakdown) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = "Price Details",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(10.dp))
      PriceRow("Total MRP", formatRupees(priceBreakdown.mrpTotal))
      PriceRow("Catalog Discount", "- ${formatRupees(priceBreakdown.productSavings)}", DealGreen)
      if (priceBreakdown.couponDiscount > 0) {
        PriceRow("Coupon Discount", "- ${formatRupees(priceBreakdown.couponDiscount)}", DealGreen)
      }
      PriceRow(
        "Delivery Charges",
        if (priceBreakdown.deliveryFee == 0L) "FREE" else formatRupees(priceBreakdown.deliveryFee),
        if (priceBreakdown.deliveryFee == 0L) DealGreen else MaterialTheme.colorScheme.onSurface
      )
      HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Total Payable",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = formatRupees(priceBreakdown.totalPayable),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
      }
      if (priceBreakdown.totalSavings > 0) {
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
          color = MaterialTheme.colorScheme.tertiaryContainer,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "You will save ${formatRupees(priceBreakdown.totalSavings)} on this order!",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun PriceRow(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
  Row(
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp)
  ) {
    Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(text = value, style = MaterialTheme.typography.bodyMedium, color = valueColor, fontWeight = FontWeight.SemiBold)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
  addressesState: UiState<List<DeliveryAddress>>,
  activeAddress: DeliveryAddress?,
  priceBreakdown: PriceBreakdown,
  deliverySpeed: String,
  paymentMethod: String,
  isPlacingOrder: Boolean,
  onSelectAddress: (String) -> Unit,
  onOpenAddressPicker: () -> Unit,
  onSelectDeliverySpeed: (String) -> Unit,
  onSelectPaymentMethod: (String) -> Unit,
  onPlaceOrder: () -> Unit,
  onBack: () -> Unit
) {
  BackHandler(onBack = onBack)

  val addressList = (addressesState as? UiState.Success)?.data.orEmpty()

  Column(modifier = Modifier.fillMaxSize()) {
    TopAppBar(
      title = { Text("Checkout & Order") },
      navigationIcon = {
        IconButton(onClick = onBack) {
          Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to cart")
        }
      }
    )

    Column(
      verticalArrangement = Arrangement.spacedBy(14.dp),
      modifier = Modifier
        .weight(1f)
        .verticalScroll(rememberScrollState())
        .padding(16.dp)
    ) {
      // Delivery Address Selection
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("1. Delivery Address (Google Maps)", style = MaterialTheme.typography.titleMedium)
            OutlinedButton(
              onClick = onOpenAddressPicker,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .height(48.dp)
                .testTag("open_map_picker_button")
            ) {
              Icon(imageVector = Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Pin New")
            }
          }

          if (addressList.isEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "No address pinned yet. Tap 'Pin New' to select your delivery pin on Google Maps.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.error
            )
          } else {
            Spacer(modifier = Modifier.height(8.dp))
            addressList.forEach { addr ->
              val selected = addr.id == activeAddress?.id
              Surface(
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                  1.5.dp,
                  if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                ),
                color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
                  .clickable { onSelectAddress(addr.id) }
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(10.dp)
                ) {
                  RadioButton(selected = selected, onClick = { onSelectAddress(addr.id) })
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "${addr.label} • ${addr.recipientName}",
                      style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                      text = "${addr.streetAddress}, ${addr.city} - ${addr.pincode}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
              }
            }
          }
        }
      }

      // Delivery Speed
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("2. Delivery Speed", style = MaterialTheme.typography.titleMedium)
          Spacer(modifier = Modifier.height(8.dp))
          listOf(
            Triple("STANDARD", "Standard Delivery (1-2 Days)", "Free above ₹499"),
            Triple("EXPRESS", "QuickKart Express (60 Mins)", "₹49 or Free above ₹2,500")
          ).forEach { (key, title, sub) ->
            val selected = deliverySpeed == key
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectDeliverySpeed(key) }
                .padding(vertical = 6.dp)
            ) {
              RadioButton(selected = selected, onClick = { onSelectDeliverySpeed(key) })
              Icon(
                imageVector = if (key == "EXPRESS") Icons.Filled.Bolt else Icons.Filled.LocalShipping,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        }
      }

      // Payment Method
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("3. Payment Method", style = MaterialTheme.typography.titleMedium)
          Spacer(modifier = Modifier.height(8.dp))
          listOf(
            Triple("UPI", "UPI Instant Pay (GPay / PhonePe)", Icons.Filled.QrCode2),
            Triple("CARD", "Credit / Debit Card", Icons.Filled.CreditCard),
            Triple("COD", "Cash / Pay on Delivery", Icons.Filled.Payments)
          ).forEach { (key, label, icon) ->
            val selected = paymentMethod == key
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectPaymentMethod(key) }
                .padding(vertical = 6.dp)
            ) {
              RadioButton(selected = selected, onClick = { onSelectPaymentMethod(key) })
              Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.width(10.dp))
              Text(label, style = MaterialTheme.typography.titleSmall)
            }
          }
        }
      }

      PriceSummaryCard(priceBreakdown = priceBreakdown)
    }

    Surface(
      color = MaterialTheme.colorScheme.surface,
      shadowElevation = 10.dp
    ) {
      Button(
        onClick = onPlaceOrder,
        enabled = !isPlacingOrder && activeAddress != null,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = SunburstGold,
          contentColor = Color(0xFF172337)
        ),
        modifier = Modifier
          .fillMaxWidth()
          .navigationBarsPadding()
          .padding(16.dp)
          .height(54.dp)
          .testTag("confirm_place_order_button")
      ) {
        if (isPlacingOrder) {
          CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
        } else {
          Icon(imageVector = Icons.Filled.CheckCircle, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Place Order • ${formatRupees(priceBreakdown.totalPayable)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressMapPickerScreen(
  defaultRecipientName: String,
  onSaveAddress: (DeliveryAddress) -> Unit,
  onBack: () -> Unit
) {
  BackHandler(onBack = onBack)

  var label by remember { mutableStateOf("Home") }
  var recipientName by remember { mutableStateOf(defaultRecipientName.ifBlank { "QuickKart Customer" }) }
  var phone by remember { mutableStateOf("9876543210") }
  var streetAddress by remember { mutableStateOf("42 Indiranagar 100 Ft Road") }
  var city by remember { mutableStateOf("Bengaluru") }
  var pincode by remember { mutableStateOf("560038") }
  var latitude by remember { mutableDoubleStateOf(12.9784) }
  var longitude by remember { mutableDoubleStateOf(77.6408) }
  var isDefault by remember { mutableStateOf(true) }

  Column(modifier = Modifier.fillMaxSize()) {
    TopAppBar(
      title = { Text("Pin Delivery Location") },
      navigationIcon = {
        IconButton(onClick = onBack) {
          Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
      }
    )

    Column(
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier
        .weight(1f)
        .verticalScroll(rememberScrollState())
        .navigationBarsPadding()
        .padding(16.dp)
    ) {
      DeliveryPinMapPicker(
        selectedLat = latitude,
        selectedLng = longitude,
        onLocationSelected = { lat, lng, cityHint ->
          latitude = lat
          longitude = lng
          if (!cityHint.isNullOrBlank()) {
            city = cityHint
            pincode = when (cityHint) {
              "Mumbai" -> "400053"
              "New Delhi" -> "110001"
              "Hyderabad" -> "500081"
              else -> "560038"
            }
          }
        }
      )

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Home", "Work", "Other").forEach { tag ->
          AssistChip(
            onClick = { label = tag },
            label = {
              Text(
                text = tag,
                fontWeight = if (label == tag) FontWeight.Bold else FontWeight.Normal,
                color = if (label == tag) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
              )
            }
          )
        }
      }

      OutlinedTextField(
        value = recipientName,
        onValueChange = { recipientName = it },
        label = { Text("Recipient Name") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )
      OutlinedTextField(
        value = phone,
        onValueChange = { phone = it },
        label = { Text("Mobile Number") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
      )
      OutlinedTextField(
        value = streetAddress,
        onValueChange = { streetAddress = it },
        label = { Text("Flat / House No., Street, Landmark") },
        modifier = Modifier.fillMaxWidth()
      )
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = city,
          onValueChange = { city = it },
          label = { Text("City") },
          singleLine = true,
          modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
          value = pincode,
          onValueChange = { pincode = it },
          label = { Text("Pincode") },
          singleLine = true,
          modifier = Modifier.weight(1f)
        )
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .clickable { isDefault = !isDefault }
      ) {
        Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
        Text("Set as default delivery address", style = MaterialTheme.typography.bodyMedium)
      }

      Button(
        onClick = {
          onSaveAddress(
            DeliveryAddress(
              label = label,
              recipientName = recipientName,
              phone = phone,
              streetAddress = streetAddress,
              city = city,
              pincode = pincode,
              latitude = latitude,
              longitude = longitude,
              isDefault = isDefault
            )
          )
        },
        enabled = recipientName.isNotBlank() && phone.length >= 5 && streetAddress.length >= 3 && city.isNotBlank() && pincode.length >= 4,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("save_address_button")
      ) {
        Text("Save Pinned Address", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      }
    }
  }
}
