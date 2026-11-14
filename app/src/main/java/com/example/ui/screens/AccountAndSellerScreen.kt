package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.DeliveryAddress
import com.example.model.OrderRecord
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.model.UserProfile
import com.example.ui.components.ProductVisualArtwork
import com.example.ui.components.encodePickedImageToBase64
import com.example.ui.components.formatRupees
import com.example.ui.viewmodel.UiState
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(
  profileState: UiState<UserProfile?>,
  addressesState: UiState<List<DeliveryAddress>>,
  ordersState: UiState<List<OrderRecord>>,
  userEmail: String,
  onOpenAddressPicker: () -> Unit,
  onDeleteAddress: (String) -> Unit,
  onOpenSellerDesk: () -> Unit,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier
) {
  val profile = (profileState as? UiState.Success)?.data
  val addresses = (addressesState as? UiState.Success)?.data.orEmpty()
  val orders = (ordersState as? UiState.Success)?.data.orEmpty()

  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
    modifier = modifier.fillMaxSize().statusBarsPadding()
  ) {
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp)
        ) {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(54.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "User avatar",
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.size(30.dp)
              )
            }
          }
          Spacer(modifier = Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = profile?.displayName?.ifBlank { "QuickKart Shopper" } ?: "QuickKart Shopper",
              style = MaterialTheme.typography.titleLarge,
              color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
              text = userEmail.ifBlank { profile?.email.orEmpty() },
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "${orders.size} Orders • ${addresses.size} Saved Map Pins",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.secondary
            )
          }
        }
      }
    }

    // Seller Desk CTA ("customer mere order mar sake")
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.Storefront,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Seller Studio & Order Desk",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Publish your own products to the QuickKart storefront and advance customer order delivery steps.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
          )
          Spacer(modifier = Modifier.height(12.dp))
          Button(
            onClick = onOpenSellerDesk,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("open_seller_desk_button")
          ) {
            Text("Open Seller Desk")
          }
        }
      }
    }

    // Saved Google Maps Addresses
    item {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text("Saved Map Addresses", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        OutlinedButton(
          onClick = onOpenAddressPicker,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.height(48.dp)
        ) {
          Icon(imageVector = Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Pin New")
        }
      }
    }

    if (addresses.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Text(
            text = "No saved delivery pins yet. Tap 'Pin New' to choose a doorstep location on Google Maps.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
          )
        }
      }
    } else {
      items(addresses, key = { it.id }) { addr ->
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.LocationOn,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${addr.label} • ${addr.recipientName}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "${addr.streetAddress}, ${addr.city} - ${addr.pincode}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            IconButton(
              onClick = { onDeleteAddress(addr.id) },
              modifier = Modifier.size(48.dp)
            ) {
              Icon(
                imageVector = Icons.Filled.DeleteOutline,
                contentDescription = "Delete address",
                tint = MaterialTheme.colorScheme.error
              )
            }
          }
        }
      }
    }

    item {
      OutlinedButton(
        onClick = onSignOut,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("sign_out_button")
      ) {
        Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Sign Out", style = MaterialTheme.typography.titleSmall)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDeskScreen(
  sellerProducts: List<Product>,
  allProducts: List<Product>,
  ordersState: UiState<List<OrderRecord>>,
  onAddProduct: (title: String, brand: String, category: String, price: Long, mrp: Long, description: String, badge: String, photoBase64: String) -> Unit,
  onUpdateProductPricing: (productId: String, price: Long, mrp: Long, inStock: Boolean, photoBase64: String?) -> Unit,
  onDeleteProduct: (String) -> Unit,
  onAdvanceOrderStatus: (OrderRecord) -> Unit,
  onOpenOrderDetail: (String) -> Unit,
  onBack: () -> Unit
) {
  BackHandler(onBack = onBack)

  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var title by remember { mutableStateOf("") }
  var brand by remember { mutableStateOf("") }
  var category by remember { mutableStateOf(ProductCategory.ELECTRONICS.label) }
  var priceText by remember { mutableStateOf("") }
  var mrpText by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var pickedPhotoBase64 by remember { mutableStateOf("") }
  var isEncodingPhoto by remember { mutableStateOf(false) }
  var photoError by remember { mutableStateOf<String?>(null) }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      isEncodingPhoto = true
      photoError = null
      scope.launch {
        val encoded = encodePickedImageToBase64(context, uri)
        isEncodingPhoto = false
        if (encoded != null) {
          pickedPhotoBase64 = encoded
        } else {
          photoError = "Could not load selected image. Try another photo."
        }
      }
    }
  }

  var showAllOrders by remember { mutableStateOf(false) }
  var showAllCatalog by remember { mutableStateOf(sellerProducts.isEmpty()) }
  var editingProduct by remember { mutableStateOf<Product?>(null) }

  val allOrders = (ordersState as? UiState.Success)?.data.orEmpty()
  val activeOrders = allOrders.filter {
    val s = OrderStatus.fromKey(it.status)
    s != OrderStatus.DELIVERED && s != OrderStatus.CANCELLED
  }
  val displayedOrders = if (showAllOrders) allOrders else activeOrders
  val totalRevenue = allOrders
    .filter { OrderStatus.fromKey(it.status) != OrderStatus.CANCELLED }
    .sumOf { it.totalAmount }
  val catalogList = if (showAllCatalog) allProducts else sellerProducts

  Column(modifier = Modifier.fillMaxSize()) {
    TopAppBar(
      title = { Text("Seller Studio") },
      navigationIcon = {
        IconButton(onClick = onBack) {
          Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
      }
    )

    Column(
      verticalArrangement = Arrangement.spacedBy(14.dp),
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .navigationBarsPadding()
        .padding(16.dp)
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        SellerStatCard(
          label = "Active Orders",
          value = "${activeOrders.size}",
          modifier = Modifier.weight(1f)
        )
        SellerStatCard(
          label = "Store Sales",
          value = formatRupees(totalRevenue),
          modifier = Modifier.weight(1f)
        )
        SellerStatCard(
          label = "Store Items",
          value = "${allProducts.size}",
          modifier = Modifier.weight(1f)
        )
      }

      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.AddBusiness,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("List New Store Product", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          }
          Spacer(modifier = Modifier.height(10.dp))

          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Product Name") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("seller_title_input")
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = brand,
            onValueChange = { brand = it },
            label = { Text("Brand / Store Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))

          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ProductCategory.entries.filter { it != ProductCategory.ALL }) { cat ->
              AssistChip(
                onClick = { category = cat.label },
                label = {
                  Text(
                    text = cat.label,
                    fontWeight = if (category == cat.label) FontWeight.Bold else FontWeight.Normal,
                    color = if (category == cat.label) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                  )
                }
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
              value = priceText,
              onValueChange = { priceText = it.filter { ch -> ch.isDigit() } },
              label = { Text("Deal Price (₹)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
              value = mrpText,
              onValueChange = { mrpText = it.filter { ch -> ch.isDigit() } },
              label = { Text("MRP (₹)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier.weight(1f)
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Product Description") },
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(modifier = Modifier.height(10.dp))
          val pickedCat = remember(category) {
            ProductCategory.entries.firstOrNull { it.label.equals(category, ignoreCase = true) }
              ?: ProductCategory.ELECTRONICS
          }
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            modifier = Modifier
              .fillMaxWidth()
              .clickable(enabled = !isEncodingPhoto) {
                photoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              }
              .testTag("pick_product_photo_button")
          ) {
            if (isEncodingPhoto) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp)
              ) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Preparing product photo...", style = MaterialTheme.typography.bodyMedium)
              }
            } else if (pickedPhotoBase64.isNotBlank()) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(10.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(10.dp))
                ) {
                  ProductVisualArtwork(
                    imageKey = pickedCat.label.lowercase(),
                    category = pickedCat,
                    photoBase64 = pickedPhotoBase64,
                    modifier = Modifier.fillMaxSize()
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Product photo added",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                  Text(
                    text = "Customers will see this photo in store",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                IconButton(
                  onClick = { pickedPhotoBase64 = "" },
                  modifier = Modifier.size(48.dp)
                ) {
                  Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Remove selected photo",
                    tint = MaterialTheme.colorScheme.error
                  )
                }
              }
            } else {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp)
              ) {
                Surface(
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                  modifier = Modifier.size(44.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.Filled.AddAPhoto,
                      contentDescription = "Add product photo",
                      tint = MaterialTheme.colorScheme.primary
                    )
                  }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Add Product Photo (Gallery)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "Tap to select item photo so customers can preview before buying",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }

          if (photoError != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = photoError.orEmpty(),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.error
            )
          }

          Spacer(modifier = Modifier.height(12.dp))
          val price = priceText.toLongOrNull() ?: 0L
          val mrp = (mrpText.toLongOrNull() ?: price).coerceAtLeast(price)
          Button(
            onClick = {
              onAddProduct(title, brand, category, price, mrp, description, "Seller Pick", pickedPhotoBase64)
              title = ""
              brand = ""
              priceText = ""
              mrpText = ""
              description = ""
              pickedPhotoBase64 = ""
              photoError = null
            },
            enabled = title.trim().length >= 2 && price > 0L && !isEncodingPhoto,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("publish_seller_product_button")
          ) {
            Text("Publish to QuickKart Store")
          }
        }
      }

      // Customer Orders Section
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Customer Orders (${displayedOrders.size})",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FilterChip(
            selected = !showAllOrders,
            onClick = { showAllOrders = false },
            label = { Text("Active (${activeOrders.size})") }
          )
          FilterChip(
            selected = showAllOrders,
            onClick = { showAllOrders = true },
            label = { Text("All (${allOrders.size})") }
          )
        }
      }

      if (displayedOrders.isEmpty()) {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Text(
            text = "No matching customer orders yet. Orders placed from Cart & Checkout appear here with customer address and live tracking.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
          )
        }
      } else {
        displayedOrders.forEach { order ->
          val statusEnum = OrderStatus.fromKey(order.status)
          val canAdvance = statusEnum != OrderStatus.DELIVERED && statusEnum != OrderStatus.CANCELLED
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "${order.orderNumber} • ${formatRupees(order.totalAmount)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "${order.paymentMethod} • ${order.deliverySpeed} • ${order.itemCount} item(s)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Surface(
                  color = MaterialTheme.colorScheme.primaryContainer,
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Text(
                    text = statusEnum.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(8.dp))
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Filled.Phone,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "${order.customerName} • ${order.customerPhone}",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Medium
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Row(verticalAlignment = Alignment.Top) {
                Icon(
                  imageVector = Icons.Filled.LocationOn,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "${order.deliveryAddress}, ${order.deliveryCity} - ${order.deliveryPincode}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = order.itemsSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
              )

              Spacer(modifier = Modifier.height(10.dp))
              Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                OutlinedButton(
                  onClick = { onOpenOrderDetail(order.id) },
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("seller_view_order_${order.id}")
                ) {
                  Icon(imageVector = Icons.Filled.LocalShipping, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("View / Track")
                }
                if (canAdvance) {
                  Button(
                    onClick = { onAdvanceOrderStatus(order) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                      .weight(1f)
                      .height(48.dp)
                      .testTag("seller_dispatch_order_${order.id}")
                  ) {
                    Text("Dispatch Next")
                  }
                }
              }
            }
          }
        }
      }

      // Catalog Price & Stock Management
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Manage Prices & Stock (${catalogList.size})",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FilterChip(
            selected = !showAllCatalog,
            onClick = { showAllCatalog = false },
            label = { Text("Added (${sellerProducts.size})") }
          )
          FilterChip(
            selected = showAllCatalog,
            onClick = { showAllCatalog = true },
            label = { Text("All (${allProducts.size})") }
          )
        }
      }

      if (catalogList.isEmpty()) {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Text(
            text = "No seller-added items yet. Switch to 'All' above to edit prices on any QuickKart product, or publish a new item above.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
          )
        }
      } else {
        catalogList.forEach { prod ->
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(56.dp)
                  .clip(RoundedCornerShape(10.dp))
              ) {
                ProductVisualArtwork(
                  imageKey = prod.imageKey,
                  category = prod.category,
                  photoBase64 = prod.photoBase64,
                  modifier = Modifier.fillMaxSize()
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = prod.title,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "${prod.brand} • ${prod.categoryName}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Text(
                    text = formatRupees(prod.price),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                  if (prod.mrp > prod.price) {
                    Text(
                      text = formatRupees(prod.mrp),
                      style = MaterialTheme.typography.bodySmall,
                      textDecoration = TextDecoration.LineThrough,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                  Surface(
                    color = if (prod.inStock) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(6.dp)
                  ) {
                    Text(
                      text = if (prod.inStock) "In Stock" else "Out of Stock",
                      style = MaterialTheme.typography.labelSmall,
                      color = if (prod.inStock) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                  }
                }
              }
              Spacer(modifier = Modifier.width(8.dp))
              OutlinedButton(
                onClick = { editingProduct = prod },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .height(48.dp)
                  .testTag("edit_price_${prod.id}")
              ) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit")
              }
              if (prod.sellerListed) {
                IconButton(
                  onClick = { onDeleteProduct(prod.id) },
                  modifier = Modifier.size(48.dp)
                ) {
                  Icon(
                    imageVector = Icons.Filled.DeleteOutline,
                    contentDescription = "Delete product",
                    tint = MaterialTheme.colorScheme.error
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  val targetProduct = editingProduct
  if (targetProduct != null) {
    EditProductPriceDialog(
      product = targetProduct,
      onDismiss = { editingProduct = null },
      onSave = { newPrice, newMrp, inStock, updatedPhoto ->
        onUpdateProductPricing(targetProduct.id, newPrice, newMrp, inStock, updatedPhoto)
        editingProduct = null
      }
    )
  }
}

@Composable
private fun SellerStatCard(
  label: String,
  value: String,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun EditProductPriceDialog(
  product: Product,
  onDismiss: () -> Unit,
  onSave: (price: Long, mrp: Long, inStock: Boolean, photoBase64: String?) -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var priceInput by remember(product.id) { mutableStateOf(product.price.toString()) }
  var mrpInput by remember(product.id) { mutableStateOf(product.mrp.toString()) }
  var inStock by remember(product.id) { mutableStateOf(product.inStock) }
  var editedPhotoBase64 by remember(product.id) { mutableStateOf(product.photoBase64) }
  var photoModified by remember(product.id) { mutableStateOf(false) }
  var isEncoding by remember { mutableStateOf(false) }

  val dialogPhotoLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      isEncoding = true
      scope.launch {
        val encoded = encodePickedImageToBase64(context, uri)
        isEncoding = false
        if (encoded != null) {
          editedPhotoBase64 = encoded
          photoModified = true
        }
      }
    }
  }

  val parsedPrice = priceInput.toLongOrNull() ?: 0L
  val parsedMrp = (mrpInput.toLongOrNull() ?: parsedPrice).coerceAtLeast(parsedPrice)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Edit Product Details",
        style = MaterialTheme.typography.titleLarge
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = product.title,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .clip(RoundedCornerShape(10.dp))
          ) {
            ProductVisualArtwork(
              imageKey = product.imageKey,
              category = product.category,
              photoBase64 = editedPhotoBase64,
              modifier = Modifier.fillMaxSize()
            )
          }
          OutlinedButton(
            onClick = {
              dialogPhotoLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            enabled = !isEncoding && !product.id.startsWith("cust_"),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .testTag("dialog_change_photo_button")
          ) {
            Icon(imageVector = Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(if (isEncoding) "Loading..." else if (editedPhotoBase64.isBlank()) "Add Photo" else "Change Photo")
          }
        }
        OutlinedTextField(
          value = priceInput,
          onValueChange = { priceInput = it.filter { ch -> ch.isDigit() } },
          label = { Text("Deal Price (₹)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("dialog_price_input")
        )
        OutlinedTextField(
          value = mrpInput,
          onValueChange = { mrpInput = it.filter { ch -> ch.isDigit() } },
          label = { Text("MRP (₹)") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("dialog_mrp_input")
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Available In Stock", style = MaterialTheme.typography.bodyLarge)
          Switch(
            checked = inStock,
            onCheckedChange = { inStock = it },
            modifier = Modifier.testTag("dialog_stock_switch")
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(parsedPrice, parsedMrp, inStock, if (photoModified) editedPhotoBase64 else null)
        },
        enabled = parsedPrice > 0L && !isEncoding,
        modifier = Modifier
          .height(48.dp)
          .testTag("save_price_button")
      ) {
        Text("Save")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.height(48.dp)
      ) {
        Text("Cancel")
      }
    }
  )
}
