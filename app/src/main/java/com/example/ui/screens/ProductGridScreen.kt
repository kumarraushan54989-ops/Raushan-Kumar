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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.DeliveryAddress
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.model.SortOption
import com.example.ui.components.CategoryStrip
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ProductGridCard
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductGridScreen(
  productsState: UiState<List<Product>>,
  searchQuery: String,
  selectedCategory: ProductCategory,
  sortOption: SortOption,
  wishlistIds: Set<String>,
  activeAddress: DeliveryAddress?,
  onSearchQueryChange: (String) -> Unit,
  onSelectCategory: (ProductCategory) -> Unit,
  onSelectSort: (SortOption) -> Unit,
  onAddToCart: (Product) -> Unit,
  onBuyNow: (Product) -> Unit,
  onToggleWishlist: (Product) -> Unit,
  onRetrySync: () -> Unit,
  isWideScreen: Boolean,
  modifier: Modifier = Modifier
) {
  var selectedProduct by remember { mutableStateOf<Product?>(null) }
  val columns = if (isWideScreen) 3 else 2

  Column(modifier = modifier.fillMaxSize()) {
    Surface(
      color = MaterialTheme.colorScheme.surface,
      shadowElevation = 2.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier
          .statusBarsPadding()
          .padding(top = 12.dp, bottom = 6.dp)
      ) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = onSearchQueryChange,
          placeholder = { Text("Search mobiles, headphones, fashion...") },
          leadingIcon = { Icon(imageVector = Icons.Filled.Search, contentDescription = null) },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { onSearchQueryChange("") }) {
                Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear search")
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .testTag("catalog_search_input")
        )

        Spacer(modifier = Modifier.height(6.dp))
        CategoryStrip(
          selectedCategory = selectedCategory,
          onSelectCategory = onSelectCategory
        )

        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(SortOption.entries) { option ->
            FilterChip(
              selected = option == sortOption,
              onClick = { onSelectSort(option) },
              label = { Text(option.label, style = MaterialTheme.typography.labelMedium) }
            )
          }
        }
      }
    }

    when (productsState) {
      is UiState.Loading -> {
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .fillMaxSize()
            .testTag("products_loading_indicator")
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "Fetching products from Firestore...",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      is UiState.Error -> {
        EmptyStateCard(
          title = "Unable to load catalog",
          subtitle = productsState.message,
          buttonLabel = "Retry Sync",
          onAction = onRetrySync,
          modifier = Modifier.fillMaxSize()
        )
      }

      is UiState.Success -> {
        val products = productsState.data
        if (products.isEmpty()) {
          EmptyStateCard(
            title = "No matching products",
            subtitle = "Try resetting filters or syncing the QuickKart catalog from Firestore.",
            buttonLabel = if (searchQuery.isNotBlank() || selectedCategory != ProductCategory.ALL) {
              "Reset Filters"
            } else {
              "Load Starter Catalog"
            },
            onAction = {
              if (searchQuery.isNotBlank() || selectedCategory != ProductCategory.ALL) {
                onSearchQueryChange("")
                onSelectCategory(ProductCategory.ALL)
                onSelectSort(SortOption.POPULARITY)
              } else {
                onRetrySync()
              }
            },
            modifier = Modifier.fillMaxSize()
          )
        } else {
          LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
              .fillMaxSize()
              .testTag("firestore_products_grid")
          ) {
            item(span = { GridItemSpan(columns) }) {
              Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Filled.CloudDone,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "Firestore Live Storefront",
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.SemiBold,
                      color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                  }
                  Text(
                    text = "${products.size} items",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            items(products, key = { it.id }) { product ->
              ProductGridCard(
                product = product,
                isWishlisted = wishlistIds.contains(product.id),
                onProductClick = { selectedProduct = product },
                onAddToCart = { onAddToCart(product) },
                onToggleWishlist = { onToggleWishlist(product) }
              )
            }
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
