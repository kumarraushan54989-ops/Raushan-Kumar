package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.model.WishlistItem
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ProductVisualArtwork
import com.example.ui.components.formatRupees
import com.example.ui.viewmodel.UiState

@Composable
fun WishlistScreen(
  wishlistState: UiState<List<WishlistItem>>,
  onMoveToCart: (WishlistItem) -> Unit,
  onRemoveFromWishlist: (WishlistItem) -> Unit,
  onExploreClick: () -> Unit,
  productPhotos: Map<String, String> = emptyMap(),
  modifier: Modifier = Modifier
) {
  when (wishlistState) {
    UiState.Loading -> {
      Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {
        CircularProgressIndicator()
      }
    }
    is UiState.Error -> {
      EmptyStateCard(
        title = "Could not load wishlist",
        subtitle = wishlistState.message,
        buttonLabel = "Browse Deals",
        onAction = onExploreClick,
        modifier = modifier.fillMaxSize()
      )
    }
    is UiState.Success -> {
      val items = wishlistState.data
      if (items.isEmpty()) {
        EmptyStateCard(
          title = "Your Wishlist is Empty",
          subtitle = "Tap the heart icon on any product to save it for later.",
          buttonLabel = "Discover Products",
          onAction = onExploreClick,
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
              text = "Saved Wishlist (${items.size})",
              style = MaterialTheme.typography.headlineSmall
            )
          }
          items(items, key = { it.id }) { item ->
            val cat = ProductCategory.entries.firstOrNull { it.label == item.category } ?: ProductCategory.ELECTRONICS
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
                    photoBase64 = productPhotos[item.productId].orEmpty(),
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Button(
                      onClick = { onMoveToCart(item) },
                      shape = RoundedCornerShape(10.dp),
                      modifier = Modifier.height(48.dp)
                    ) {
                      Icon(imageVector = Icons.Filled.AddShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text("Move to Cart")
                    }
                    IconButton(
                      onClick = { onRemoveFromWishlist(item) },
                      modifier = Modifier.size(48.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Remove from wishlist",
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
    }
  }
}
