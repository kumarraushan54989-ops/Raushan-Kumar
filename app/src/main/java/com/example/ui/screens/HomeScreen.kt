package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.DeliveryAddress
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.ui.components.CategoryStrip
import com.example.ui.components.DeliveryAddressBar
import com.example.ui.components.ProductGridCard
import com.example.ui.components.ProductVisualArtwork
import com.example.ui.components.formatRupees
import com.example.ui.theme.DealGreen
import com.example.ui.theme.ElectricAzure
import com.example.ui.theme.FlashYellow
import com.example.ui.theme.SunburstGold
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  products: List<Product>,
  selectedCategory: ProductCategory,
  wishlistIds: Set<String>,
  activeAddress: DeliveryAddress?,
  cartCount: Int,
  onSelectCategory: (ProductCategory) -> Unit,
  onSearchBarClick: () -> Unit,
  onLocationBarClick: () -> Unit,
  onCartClick: () -> Unit,
  onSellerDeskClick: () -> Unit,
  onAddToCart: (Product) -> Unit,
  onBuyNow: (Product) -> Unit,
  onToggleWishlist: (Product) -> Unit,
  isWideScreen: Boolean,
  modifier: Modifier = Modifier
) {
  var selectedProduct by remember { mutableStateOf<Product?>(null) }
  var secondsLeft by remember { mutableIntStateOf(15824) }

  LaunchedEffect(Unit) {
    while (secondsLeft > 0) {
      delay(1000L)
      secondsLeft--
    }
  }

  val displayedProducts = remember(products, selectedCategory) {
    if (selectedCategory == ProductCategory.ALL) products
    else products.filter { it.category == selectedCategory }
  }
  val columns = if (isWideScreen) 3 else 2
  val productRows = remember(displayedProducts, columns) { displayedProducts.chunked(columns) }

  Column(modifier = modifier.fillMaxSize()) {
    // Flipkart-style vibrant top bar
    Surface(
      color = MaterialTheme.colorScheme.primary,
      shadowElevation = 4.dp
    ) {
      Column(
        modifier = Modifier
          .statusBarsPadding()
          .padding(horizontal = 16.dp, vertical = 10.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = FlashYellow,
              modifier = Modifier.size(34.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Filled.ShoppingBag,
                  contentDescription = "QuickKart",
                  tint = Color(0xFF172337),
                  modifier = Modifier.size(20.dp)
                )
              }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "QuickKart",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Plus • Superfast Delivery",
                style = MaterialTheme.typography.labelSmall,
                color = FlashYellow
              )
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Color.White.copy(alpha = 0.16f),
              modifier = Modifier
                .height(40.dp)
                .clickable(onClick = onSellerDeskClick)
                .testTag("home_seller_desk_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp)
              ) {
                Icon(
                  imageVector = Icons.Filled.Storefront,
                  contentDescription = "Seller Desk",
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Seller",
                  style = MaterialTheme.typography.labelMedium,
                  color = Color.White
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar trigger
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surface,
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onSearchBarClick)
            .testTag("home_search_bar")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.Search,
              contentDescription = "Search products",
              tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Search mobiles, laptops, sneakers, groceries...",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }

    DeliveryAddressBar(
      activeAddress = activeAddress,
      onClick = onLocationBarClick
    )

    LazyColumn(
      contentPadding = PaddingValues(bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      item {
        CategoryStrip(
          selectedCategory = selectedCategory,
          onSelectCategory = onSelectCategory
        )
      }

      // Hero Big Saving Days Banner
      item {
        Box(
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(185.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onSelectCategory(ProductCategory.MOBILES) }
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_hero_big_billion),
            contentDescription = "Big Saving Days mega electronics sale",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.horizontalGradient(
                  colors = listOf(Color(0xD9102A63), Color(0x44102A63), Color.Transparent)
                )
              )
          )
          Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
              .fillMaxSize()
              .padding(18.dp)
          ) {
            Surface(
              color = FlashYellow,
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = "BIG SAVING DAYS • UP TO 55% OFF",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF172337),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
            Column {
              Text(
                text = "5G Smartphones &\nSmart Tech Drops",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Use code QUICK10 for extra 10% instant off",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.92f)
              )
            }
          }
        }
      }

      // Flash Deal Countdown Strip
      item {
        val h = secondsLeft / 3600
        val m = (secondsLeft % 3600) / 60
        val s = secondsLeft % 60
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 12.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.Bolt,
                contentDescription = null,
                tint = Color(0xFFD97706),
                modifier = Modifier.size(22.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "Flash Deals Ending Soon",
                  style = MaterialTheme.typography.titleSmall,
                  color = MaterialTheme.colorScheme.onSecondaryContainer,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Coupons: QUICK10 • SUPER500 • FREEDEL",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
              }
            }
            Surface(
              color = Color(0xFF172337),
              shape = RoundedCornerShape(8.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
              ) {
                Icon(
                  imageVector = Icons.Filled.Timer,
                  contentDescription = null,
                  tint = FlashYellow,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = String.format(Locale.US, "%02d:%02d:%02d", h, m, s),
                  style = MaterialTheme.typography.labelMedium,
                  color = Color.White
                )
              }
            }
          }
        }
      }

      // Dual Spotlight Banners (Audio & Fashion)
      item {
        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          item {
            SpotlightBannerCard(
              imageRes = R.drawable.img_banner_audio,
              tag = "AUDIO FEST",
              title = "ANC & Smartwatches",
              subtitle = "Starting ₹2,999",
              onClick = { onSelectCategory(ProductCategory.ELECTRONICS) }
            )
          }
          item {
            SpotlightBannerCard(
              imageRes = R.drawable.img_banner_fashion,
              tag = "TRENDING STYLE",
              title = "Sneakers & Backpacks",
              subtitle = "Flat 50%+ Off",
              onClick = { onSelectCategory(ProductCategory.FASHION) }
            )
          }
        }
      }

      // Section Heading
      item {
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
        ) {
          Text(
            text = if (selectedCategory == ProductCategory.ALL) "Top Deals For You" else "${selectedCategory.label} Picks",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = "${displayedProducts.size} items",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      items(productRows) { rowProducts ->
        Row(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
        ) {
          rowProducts.forEach { product ->
            ProductGridCard(
              product = product,
              isWishlisted = wishlistIds.contains(product.id),
              onProductClick = { selectedProduct = product },
              onAddToCart = { onAddToCart(product) },
              onToggleWishlist = { onToggleWishlist(product) },
              modifier = Modifier.weight(1f)
            )
          }
          repeat(columns - rowProducts.size) {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }
    }
  }

  val product = selectedProduct
  if (product != null) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = { selectedProduct = null },
      sheetState = sheetState
    ) {
      ProductDetailSheetContent(
        product = product,
        isWishlisted = wishlistIds.contains(product.id),
        activeAddress = activeAddress,
        onToggleWishlist = { onToggleWishlist(product) },
        onAddToCart = {
          onAddToCart(product)
          selectedProduct = null
        },
        onBuyNow = {
          selectedProduct = null
          onBuyNow(product)
        },
        onClose = { selectedProduct = null }
      )
    }
  }
}

@Composable
private fun SpotlightBannerCard(
  imageRes: Int,
  tag: String,
  title: String,
  subtitle: String,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .width(248.dp)
      .height(126.dp)
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
  ) {
    Image(
      painter = painterResource(id = imageRes),
      contentDescription = title,
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize()
    )
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(Color.Transparent, Color(0xCC0F172A))
          )
        )
    )
    Column(
      verticalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier
        .fillMaxSize()
        .padding(12.dp)
    ) {
      Surface(
        color = Color.White.copy(alpha = 0.9f),
        shape = RoundedCornerShape(6.dp)
      ) {
        Text(
          text = tag,
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.Bold,
          color = ElectricAzure,
          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
      }
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          color = Color.White,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.labelMedium,
          color = FlashYellow
        )
      }
    }
  }
}

@Composable
fun ProductDetailSheetContent(
  product: Product,
  isWishlisted: Boolean,
  activeAddress: DeliveryAddress?,
  onToggleWishlist: () -> Unit,
  onAddToCart: () -> Unit,
  onBuyNow: () -> Unit,
  onClose: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp)
      .padding(bottom = 28.dp)
  ) {
    Row(
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      Text(
        text = product.brand,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary
      )
      Row {
        IconButton(onClick = onToggleWishlist) {
          Icon(
            imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = "Toggle wishlist",
            tint = if (isWishlisted) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        IconButton(onClick = onClose) {
          Icon(imageVector = Icons.Filled.Close, contentDescription = "Close details")
        }
      }
    }

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(190.dp)
        .clip(RoundedCornerShape(18.dp))
    ) {
      ProductVisualArtwork(
        imageKey = product.imageKey,
        category = product.category,
        photoBase64 = product.photoBase64,
        modifier = Modifier.fillMaxSize()
      )
    }

    Spacer(modifier = Modifier.height(14.dp))
    Text(
      text = product.title,
      style = MaterialTheme.typography.headlineSmall,
      color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
      Surface(color = DealGreen, shape = RoundedCornerShape(6.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Text(
            text = String.format(Locale.US, "%.1f", product.rating),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(3.dp))
          Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(13.dp)
          )
        }
      }
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "${product.reviewCount} ratings & reviews",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Spacer(modifier = Modifier.height(12.dp))
    Row(verticalAlignment = Alignment.Bottom) {
      Text(
        text = formatRupees(product.price),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.width(10.dp))
      if (product.mrp > product.price) {
        Text(
          text = formatRupees(product.mrp),
          style = MaterialTheme.typography.titleMedium,
          textDecoration = TextDecoration.LineThrough,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "${product.discountPercent}% off",
          style = MaterialTheme.typography.titleSmall,
          color = DealGreen,
          fontWeight = FontWeight.Bold
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))
    Surface(
      color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(12.dp)
      ) {
        Icon(
          imageVector = Icons.Filled.LocalShipping,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = if (activeAddress != null) {
              "Delivery to ${activeAddress.city} ${activeAddress.pincode}"
            } else {
              "Express & Standard Delivery Available"
            },
            style = MaterialTheme.typography.titleSmall
          )
          Text(
            text = "Estimated arrival in ${product.deliveryDays} day(s) • 7-Day Replacement",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))
    Text(
      text = product.description,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    if (product.highlights.isNotEmpty()) {
      Spacer(modifier = Modifier.height(14.dp))
      Text(
        text = "Highlights",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(6.dp))
      product.highlights.forEach { point ->
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(vertical = 3.dp)
        ) {
          Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = DealGreen,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = point, style = MaterialTheme.typography.bodyMedium)
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))
    Surface(
      color = MaterialTheme.colorScheme.secondaryContainer,
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(12.dp)
      ) {
        Icon(
          imageVector = Icons.Filled.LocalOffer,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSecondaryContainer,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Apply QUICK10 or SUPER500 in Cart for extra savings!",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSecondaryContainer
        )
      }
    }

    Spacer(modifier = Modifier.height(18.dp))
    Row(
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      OutlinedButton(
        onClick = onAddToCart,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .weight(1f)
          .height(52.dp)
          .testTag("detail_add_to_cart")
      ) {
        Icon(
          imageVector = Icons.Filled.AddShoppingCart,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text("Add to Cart", style = MaterialTheme.typography.labelLarge)
      }
      Button(
        onClick = onBuyNow,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = SunburstGold,
          contentColor = Color(0xFF172337)
        ),
        modifier = Modifier
          .weight(1f)
          .height(52.dp)
          .testTag("detail_buy_now")
      ) {
        Icon(
          imageVector = Icons.Filled.Bolt,
          contentDescription = null,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text("Buy Now", style = MaterialTheme.typography.labelLarge)
      }
    }
  }
}
