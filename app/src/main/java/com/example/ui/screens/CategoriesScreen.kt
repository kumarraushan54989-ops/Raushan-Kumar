package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.model.DeliveryAddress
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.model.SortOption
import com.example.ui.viewmodel.UiState

@Composable
fun CategoriesScreen(
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
  ProductGridScreen(
    productsState = productsState,
    searchQuery = searchQuery,
    selectedCategory = selectedCategory,
    sortOption = sortOption,
    wishlistIds = wishlistIds,
    activeAddress = activeAddress,
    onSearchQueryChange = onSearchQueryChange,
    onSelectCategory = onSelectCategory,
    onSelectSort = onSelectSort,
    onAddToCart = onAddToCart,
    onBuyNow = onBuyNow,
    onToggleWishlist = onToggleWishlist,
    onRetrySync = onRetrySync,
    isWideScreen = isWideScreen,
    modifier = modifier
  )
}

