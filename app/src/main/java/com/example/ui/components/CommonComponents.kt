package com.example.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Blender
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FaceRetouchingNatural
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LaptopMac
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.theme.DealGreen
import com.example.ui.theme.ElectricAzure
import com.example.ui.theme.SunburstGold
import java.text.NumberFormat
import java.util.Locale

fun formatRupees(amount: Long): String {
  val formatter = NumberFormat.getNumberInstance(Locale.Builder().setLanguage("en").setRegion("IN").build())
  return "₹${formatter.format(amount)}"
}

fun categoryIcon(category: ProductCategory): ImageVector = when (category) {
  ProductCategory.ALL -> Icons.Filled.Widgets
  ProductCategory.MOBILES -> Icons.Filled.PhoneAndroid
  ProductCategory.ELECTRONICS -> Icons.Filled.Devices
  ProductCategory.FASHION -> Icons.Filled.Checkroom
  ProductCategory.HOME -> Icons.Filled.Chair
  ProductCategory.APPLIANCES -> Icons.Filled.Blender
  ProductCategory.BEAUTY -> Icons.Filled.FaceRetouchingNatural
  ProductCategory.GROCERY -> Icons.Filled.LocalGroceryStore
}

@Composable
fun ProductVisualArtwork(
  imageKey: String,
  category: ProductCategory,
  photoBase64: String = "",
  modifier: Modifier = Modifier
) {
  val customBitmap = remember(photoBase64) { decodeBase64ToImageBitmap(photoBase64) }
  if (customBitmap != null) {
    Image(
      bitmap = customBitmap,
      contentDescription = "${category.label} product photo",
      contentScale = ContentScale.Crop,
      modifier = modifier
    )
    return
  }
  val lowerKey = imageKey.lowercase()
  when {
    lowerKey == "audio" || lowerKey == "watch" -> {
      Image(
        painter = painterResource(id = R.drawable.img_banner_audio),
        contentDescription = "${category.label} showcase",
        contentScale = ContentScale.Crop,
        modifier = modifier
      )
    }
    lowerKey == "fashion" -> {
      Image(
        painter = painterResource(id = R.drawable.img_banner_fashion),
        contentDescription = "Fashion item showcase",
        contentScale = ContentScale.Crop,
        modifier = modifier
      )
    }
    lowerKey == "mobiles" -> {
      Image(
        painter = painterResource(id = R.drawable.img_hero_big_billion),
        contentDescription = "Mobile device showcase",
        contentScale = ContentScale.Crop,
        modifier = modifier
      )
    }
    else -> {
      val (startColor, endColor, icon) = when (category) {
        ProductCategory.MOBILES -> Triple(Color(0xFF1A56DB), Color(0xFF4F8BFF), Icons.Filled.PhoneAndroid)
        ProductCategory.ELECTRONICS -> when (lowerKey) {
          "laptop" -> Triple(Color(0xFF1F2937), Color(0xFF4B5563), Icons.Filled.LaptopMac)
          "watch" -> Triple(Color(0xFF0F766E), Color(0xFF2DD4BF), Icons.Filled.Watch)
          else -> Triple(Color(0xFF2563EB), Color(0xFF60A5FA), Icons.Filled.Headphones)
        }
        ProductCategory.FASHION -> Triple(Color(0xFF0E7490), Color(0xFF22D3EE), Icons.Filled.Checkroom)
        ProductCategory.HOME -> Triple(Color(0xFF9A3412), Color(0xFFFB923C), Icons.Filled.Chair)
        ProductCategory.APPLIANCES -> Triple(Color(0xFF4338CA), Color(0xFF818CF8), Icons.Filled.Blender)
        ProductCategory.BEAUTY -> Triple(Color(0xFFBE185D), Color(0xFFF472B6), Icons.Filled.FaceRetouchingNatural)
        ProductCategory.GROCERY -> Triple(Color(0xFF15803D), Color(0xFF4ADE80), Icons.Filled.LocalGroceryStore)
        ProductCategory.ALL -> Triple(ElectricAzure, Color(0xFF60A5FA), Icons.Filled.Devices)
      }
      Box(
        modifier = modifier.background(Brush.linearGradient(listOf(startColor, endColor))),
        contentAlignment = Alignment.Center
      ) {
        Surface(
          shape = CircleShape,
          color = Color.White.copy(alpha = 0.2f),
          modifier = Modifier.size(60.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = icon,
              contentDescription = category.label,
              tint = Color.White,
              modifier = Modifier.size(34.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun DeliveryAddressBar(
  activeAddress: DeliveryAddress?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("delivery_location_bar")
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
      Icon(
        imageVector = Icons.Filled.LocationOn,
        contentDescription = "Delivery location",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(18.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = if (activeAddress != null) {
          "Deliver to ${activeAddress.label} - ${activeAddress.city} ${activeAddress.pincode}"
        } else {
          "Pin delivery location on Google Maps"
        },
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f)
      )
      Icon(
        imageVector = Icons.Filled.KeyboardArrowDown,
        contentDescription = "Change delivery address",
        tint = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}

@Composable
fun CategoryStrip(
  selectedCategory: ProductCategory,
  onSelectCategory: (ProductCategory) -> Unit,
  modifier: Modifier = Modifier
) {
  LazyRow(
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = modifier
  ) {
    items(ProductCategory.entries) { category ->
      val selected = category == selectedCategory
      FilterChip(
        selected = selected,
        onClick = { onSelectCategory(category) },
        label = {
          Text(
            text = category.label,
            style = MaterialTheme.typography.labelMedium
          )
        },
        leadingIcon = {
          Icon(
            imageVector = categoryIcon(category),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
        },
        colors = FilterChipDefaults.filterChipColors(
          selectedContainerColor = MaterialTheme.colorScheme.primary,
          selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
          selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier.testTag("category_chip_${category.name.lowercase()}")
      )
    }
  }
}

@Composable
fun ProductGridCard(
  product: Product,
  isWishlisted: Boolean,
  onProductClick: () -> Unit,
  onAddToCart: () -> Unit,
  onToggleWishlist: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onProductClick)
      .testTag("product_card_${product.id}")
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(128.dp)
          .clip(RoundedCornerShape(12.dp))
      ) {
        ProductVisualArtwork(
          imageKey = product.imageKey,
          category = product.category,
          photoBase64 = product.photoBase64,
          modifier = Modifier.fillMaxSize()
        )
        if (product.badge.isNotBlank()) {
          Surface(
            color = SunburstGold,
            shape = RoundedCornerShape(bottomEnd = 10.dp),
            modifier = Modifier.align(Alignment.TopStart)
          ) {
            Text(
              text = product.badge,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF172337),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
        IconButton(
          onClick = onToggleWishlist,
          modifier = Modifier
            .align(Alignment.TopEnd)
            .size(48.dp)
            .testTag("wishlist_btn_${product.id}")
        ) {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
            modifier = Modifier.size(32.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = if (isWishlisted) "Remove from wishlist" else "Save to wishlist",
                tint = if (isWishlisted) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = product.brand,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.Verified,
            contentDescription = "QuickKart Assured",
            tint = ElectricAzure,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(2.dp))
          Text(
            text = "Assured",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Spacer(modifier = Modifier.height(3.dp))
      Text(
        text = product.title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.height(40.dp)
      )

      Spacer(modifier = Modifier.height(6.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
          color = DealGreen,
          shape = RoundedCornerShape(6.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = String.format(Locale.US, "%.1f", product.rating),
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
              imageVector = Icons.Filled.Star,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(11.dp)
            )
          }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "(${product.reviewCount})",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = formatRupees(product.price),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(6.dp))
        if (product.mrp > product.price) {
          Text(
            text = formatRupees(product.mrp),
            style = MaterialTheme.typography.bodySmall,
            textDecoration = TextDecoration.LineThrough,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
          )
        }
      }

      if (product.discountPercent > 0) {
        Text(
          text = "${product.discountPercent}% off • Free Delivery",
          style = MaterialTheme.typography.labelSmall,
          color = DealGreen,
          fontWeight = FontWeight.SemiBold
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Button(
        onClick = onAddToCart,
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("add_to_cart_${product.id}")
      ) {
        Icon(
          imageVector = Icons.Filled.AddShoppingCart,
          contentDescription = null,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Add to Cart",
          style = MaterialTheme.typography.labelLarge
        )
      }
    }
  }
}

@Composable
fun EmptyStateCard(
  title: String,
  subtitle: String,
  buttonLabel: String,
  onAction: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = modifier
      .fillMaxWidth()
      .padding(24.dp)
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_empty_cart),
      contentDescription = title,
      contentScale = ContentScale.Fit,
      modifier = Modifier
        .size(170.dp)
        .clip(RoundedCornerShape(24.dp))
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = title,
      style = MaterialTheme.typography.headlineSmall,
      color = MaterialTheme.colorScheme.onBackground
    )
    Spacer(modifier = Modifier.height(6.dp))
    Text(
      text = subtitle,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(18.dp))
    Button(
      onClick = onAction,
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.height(48.dp)
    ) {
      Text(text = buttonLabel, style = MaterialTheme.typography.labelLarge)
    }
  }
}
