package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CatalogData
import com.example.data.QuickKartRepository
import com.example.model.CartItem
import com.example.model.CustomProduct
import com.example.model.DeliveryAddress
import com.example.model.OrderLineItem
import com.example.model.OrderRecord
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.model.SortOption
import com.example.model.UserProfile
import com.example.model.WishlistItem
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>
  data class Success<T>(val data: T) : UiState<T>
  data class Error(val message: String) : UiState<Nothing>
}

data class PriceBreakdown(
  val mrpTotal: Long,
  val subtotal: Long,
  val productSavings: Long,
  val couponDiscount: Long,
  val deliveryFee: Long,
  val totalPayable: Long,
  val totalSavings: Long
)

class QuickKartViewModel(
  private val repository: QuickKartRepository,
  private val currentUserId: String,
  userDisplayName: String,
  userEmail: String
) : ViewModel() {

  private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
  val events = _events.asSharedFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery = _searchQuery.asStateFlow()

  private val _selectedCategory = MutableStateFlow(ProductCategory.ALL)
  val selectedCategory = _selectedCategory.asStateFlow()

  private val _sortOption = MutableStateFlow(SortOption.POPULARITY)
  val sortOption = _sortOption.asStateFlow()

  private val _appliedCoupon = MutableStateFlow("")
  val appliedCoupon = _appliedCoupon.asStateFlow()

  private val _deliverySpeed = MutableStateFlow("STANDARD")
  val deliverySpeed = _deliverySpeed.asStateFlow()

  private val _paymentMethod = MutableStateFlow("UPI")
  val paymentMethod = _paymentMethod.asStateFlow()

  private val _selectedAddressId = MutableStateFlow<String?>(null)
  val selectedAddressId = _selectedAddressId.asStateFlow()

  private val _isPlacingOrder = MutableStateFlow(false)
  val isPlacingOrder = _isPlacingOrder.asStateFlow()

  init {
    viewModelScope.launch {
      try {
        repository.ensureUserProfile(userDisplayName, userEmail)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Log.w(TAG, "Profile init warning: ${e.message}")
      }
      try {
        repository.seedStarterProductsIfEmpty()
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        Log.w(TAG, "Catalog seed warning: ${e.message}")
      }
    }
  }

  val userProfile: StateFlow<UiState<UserProfile?>> =
    repository.observeUserProfile(currentUserId)
      .toUiState("Failed to load profile")

  val addresses: StateFlow<UiState<List<DeliveryAddress>>> =
    repository.observeAddresses(currentUserId)
      .toUiState("Failed to load addresses")

  val cartItems: StateFlow<UiState<List<CartItem>>> =
    repository.observeCart(currentUserId)
      .toUiState("Failed to load cart")

  val wishlistItems: StateFlow<UiState<List<WishlistItem>>> =
    repository.observeWishlist(currentUserId)
      .toUiState("Failed to load wishlist")

  val orders: StateFlow<UiState<List<OrderRecord>>> =
    repository.observeOrders(currentUserId)
      .toUiState("Failed to load orders")

  val customProducts: StateFlow<UiState<List<CustomProduct>>> =
    repository.observeCustomProducts(currentUserId)
      .toUiState("Failed to load store products")

  val firestoreProductsState: StateFlow<UiState<List<Product>>> =
    repository.observeProducts(currentUserId)
      .toUiState("Failed to load products from Firestore")

  val allProducts: StateFlow<List<Product>> = combine(
    firestoreProductsState,
    customProducts
  ) { prodState, customState ->
    val legacyCustom = (customState as? UiState.Success)?.data.orEmpty().map { cp ->
      val cat = ProductCategory.entries.firstOrNull { it.label.equals(cp.category, ignoreCase = true) }
        ?: ProductCategory.ELECTRONICS
      Product(
        id = cp.id,
        userId = cp.userId,
        title = cp.title,
        brand = cp.brand,
        categoryName = cat.label,
        price = cp.price,
        mrp = cp.mrp.coerceAtLeast(cp.price),
        rating = 4.6,
        reviewCount = 48L,
        highlights = listOf("Direct from Seller Desk", "QuickKart Quality Checked", "Fast Dispatch"),
        description = cp.description,
        badge = cp.badge.ifBlank { "Seller Direct" },
        imageKey = cat.label.lowercase(),
        deliveryDays = 1L,
        inStock = cp.inStock,
        sellerListed = true
      )
    }
    val firestoreList = (prodState as? UiState.Success)?.data
    val base = if (firestoreList != null && firestoreList.isNotEmpty()) {
      firestoreList
    } else if (prodState is UiState.Loading) {
      CatalogData.curatedProducts
    } else {
      firestoreList.orEmpty()
    }
    (legacyCustom + base).distinctBy { it.id }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), CatalogData.curatedProducts)

  val sellerListedProducts: StateFlow<List<Product>> = allProducts
    .map { list -> list.filter { it.sellerListed } }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

  val filteredProductsState: StateFlow<UiState<List<Product>>> = combine(
    firestoreProductsState,
    customProducts,
    _searchQuery,
    _selectedCategory,
    _sortOption
  ) { prodState, customState, query, category, sort ->
    when (prodState) {
      is UiState.Loading -> UiState.Loading
      is UiState.Error -> UiState.Error(prodState.message)
      is UiState.Success -> {
        val legacyCustom = (customState as? UiState.Success)?.data.orEmpty().map { cp ->
          val cat = ProductCategory.entries.firstOrNull { it.label.equals(cp.category, ignoreCase = true) }
            ?: ProductCategory.ELECTRONICS
          Product(
            id = cp.id,
            userId = cp.userId,
            title = cp.title,
            brand = cp.brand,
            categoryName = cat.label,
            price = cp.price,
            mrp = cp.mrp.coerceAtLeast(cp.price),
            rating = 4.6,
            reviewCount = 48L,
            highlights = listOf("Direct from Seller Desk", "QuickKart Quality Checked", "Fast Dispatch"),
            description = cp.description,
            badge = cp.badge.ifBlank { "Seller Direct" },
            imageKey = cat.label.lowercase(),
            deliveryDays = 1L,
            inStock = cp.inStock,
            sellerListed = true
          )
        }
        val combined = (legacyCustom + prodState.data).distinctBy { it.id }
        UiState.Success(filterAndSortProducts(combined, query, category, sort))
      }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

  val filteredProducts: StateFlow<List<Product>> = combine(
    allProducts,
    _searchQuery,
    _selectedCategory,
    _sortOption
  ) { products, query, category, sort ->
    filterAndSortProducts(products, query, category, sort)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), CatalogData.curatedProducts)

  private fun filterAndSortProducts(
    products: List<Product>,
    query: String,
    category: ProductCategory,
    sort: SortOption
  ): List<Product> {
    val q = query.trim().lowercase()
    val filtered = products.filter { p ->
      val matchesCat = category == ProductCategory.ALL || p.category == category
      val matchesQuery = q.isEmpty() ||
        p.title.lowercase().contains(q) ||
        p.brand.lowercase().contains(q) ||
        p.category.label.lowercase().contains(q)
      matchesCat && matchesQuery
    }
    return when (sort) {
      SortOption.POPULARITY -> filtered
      SortOption.PRICE_LOW_HIGH -> filtered.sortedBy { it.price }
      SortOption.PRICE_HIGH_LOW -> filtered.sortedByDescending { it.price }
      SortOption.RATING -> filtered.sortedByDescending { it.rating }
      SortOption.DISCOUNT -> filtered.sortedByDescending { it.discountPercent }
    }
  }

  val cartCount: StateFlow<Int> = cartItems
    .map { state -> (state as? UiState.Success)?.data?.sumOf { it.quantity.toInt() } ?: 0 }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), 0)

  val wishlistProductIds: StateFlow<Set<String>> = wishlistItems
    .map { state -> (state as? UiState.Success)?.data?.map { it.productId }?.toSet() ?: emptySet() }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptySet())

  val activeAddress: StateFlow<DeliveryAddress?> = combine(
    addresses,
    _selectedAddressId
  ) { state, selectedId ->
    val list = (state as? UiState.Success)?.data.orEmpty()
    list.firstOrNull { it.id == selectedId } ?: list.firstOrNull { it.isDefault } ?: list.firstOrNull()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

  val priceBreakdown: StateFlow<PriceBreakdown> = combine(
    cartItems,
    _appliedCoupon,
    _deliverySpeed
  ) { state, coupon, speed ->
    val items = (state as? UiState.Success)?.data.orEmpty()
    calculatePrices(items, coupon, speed)
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
    PriceBreakdown(0L, 0L, 0L, 0L, 0L, 0L, 0L)
  )

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setCategory(category: ProductCategory) {
    _selectedCategory.value = category
  }

  fun setSortOption(option: SortOption) {
    _sortOption.value = option
  }

  fun setDeliverySpeed(speed: String) {
    _deliverySpeed.value = speed
  }

  fun setPaymentMethod(method: String) {
    _paymentMethod.value = method
  }

  fun selectAddress(addressId: String) {
    _selectedAddressId.value = addressId
  }

  fun applyCoupon(code: String) {
    val clean = code.trim().uppercase()
    if (clean.isEmpty()) {
      _appliedCoupon.value = ""
      return
    }
    if (CatalogData.promoCoupons.containsKey(clean)) {
      _appliedCoupon.value = clean
      _events.tryEmit("Coupon $clean applied!")
    } else {
      _events.tryEmit("Invalid coupon. Try QUICK10, SUPER500, or FREEDEL")
    }
  }

  fun clearCoupon() {
    _appliedCoupon.value = ""
  }

  fun addToCart(product: Product) {
    viewModelScope.launch {
      try {
        repository.addToCart(product, 1L)
        _events.tryEmit("${product.title.take(28)} added to cart")
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _events.tryEmit(e.localizedMessage ?: "Could not add to cart")
      }
    }
  }

  fun updateCartQuantity(cartItemId: String, newQty: Long) {
    viewModelScope.launch {
      try {
        repository.updateCartQuantity(cartItemId, newQty)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _events.tryEmit(e.localizedMessage ?: "Failed to update quantity")
      }
    }
  }

  fun removeFromCart(cartItemId: String) {
    viewModelScope.launch {
      try {
        repository.removeFromCart(cartItemId)
        _events.tryEmit("Item removed from cart")
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _events.tryEmit(e.localizedMessage ?: "Failed to remove item")
      }
    }
  }

  fun toggleWishlist(product: Product) {
    viewModelScope.launch {
      try {
        val wasSaved = wishlistProductIds.value.contains(product.id)
        repository.toggleWishlist(product)
        _events.tryEmit(if (wasSaved) "Removed from Wishlist" else "Saved to Wishlist")
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _events.tryEmit(e.localizedMessage ?: "Failed to update wishlist")
      }
    }
  }

  fun moveWishlistItemToCart(item: WishlistItem) {
    val cat = ProductCategory.entries.firstOrNull { it.label == item.category } ?: ProductCategory.ELECTRONICS
    val product = Product(
      id = item.productId,
      title = item.title,
      brand = item.brand,
      categoryName = cat.label,
      price = item.price,
      mrp = item.mrp,
      rating = item.rating,
      reviewCount = 100L,
      highlights = emptyList(),
      description = "",
      imageKey = item.imageKey
    )
    addToCart(product)
  }

  fun saveAddress(address: DeliveryAddress, onSaved: (String) -> Unit = {}) {
    viewModelScope.launch {
      try {
        val savedId = repository.saveAddress(address)
        _selectedAddressId.value = savedId
        _events.tryEmit("Delivery address pinned & saved")
        onSaved(savedId)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _events.tryEmit(e.localizedMessage ?: "Failed to save address")
      }
    }
  }

  fun deleteAddress(addressId: String) {
    viewModelScope.launch {
      try {
        repository.deleteAddress(addressId)
        _events.tryEmit("Address deleted")
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _events.tryEmit(e.localizedMessage ?: "Failed to delete address")
      }
    }
  }

  fun placeOrder(onOrderPlaced: (String) -> Unit) {
    val items = (cartItems.value as? UiState.Success)?.data.orEmpty()
    val address = activeAddress.value
    if (items.isEmpty()) {
      _events.tryEmit("Your cart is empty")
      return
    }
    if (address == null) {
      _events.tryEmit("Please select or pin a delivery address first")
      return
    }
    val breakdown = priceBreakdown.value
    _isPlacingOrder.value = true
    viewModelScope.launch {
      try {
        val orderId = repository.placeOrder(
          cartItems = items,
          address = address,
          paymentMethod = _paymentMethod.value,
          deliverySpeed = _deliverySpeed.value,
          couponCode = _appliedCoupon.value,
          subtotal = breakdown.subtotal,
          discount = breakdown.couponDiscount,
          deliveryFee = breakdown.deliveryFee,
          totalAmount = breakdown.totalPayable,
          clearCartAfterOrder = true
        )
        _appliedCoupon.value = ""
        _events.tryEmit("Order placed successfully!")
        onOrderPlaced(orderId)
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _events.tryEmit(e.localizedMessage ?: "Failed to place order")
      } finally {
        _isPlacingOrder.value = false
      }
    }
  }

  fun observeOrder(orderId: String): Flow<OrderRecord?> =
    repository.observeOrderById(currentUserId, orderId)

  fun observeOrderItems(orderId: String): Flow<List<OrderLineItem>> =
    repository.observeOrderItems(currentUserId, orderId)

  fun advanceOrderStatus(order: OrderRecord) {
    val current = OrderStatus.fromKey(order.status)
    val next = when (current) {
      OrderStatus.PLACED -> OrderStatus.PACKED
      OrderStatus.PACKED -> OrderStatus.SHIPPED
      OrderStatus.SHIPPED -> OrderStatus.OUT_FOR_DELIVERY
      OrderStatus.OUT_FOR_DELIVERY -> OrderStatus.DELIVERED
      OrderStatus.DELIVERED, OrderStatus.CANCELLED -> return
    }
    viewModelScope.launch {
      try {
        repository.updateOrderStatus(order.id, next)
        _events.tryEmit("Order marked ${next.label}")
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _events.tryEmit(e.localizedMessage ?: "Could not update order status")
      }
    }
  }

  fun cancelOrder(order: OrderRecord) {
    val current = OrderStatus.fromKey(order.status)
    if (current == OrderStatus.DELIVERED || current == OrderStatus.CANCELLED) return
    viewModelScope.launch {
      try {
        repository.updateOrderStatus(order.id, OrderStatus.CANCELLED)
        _events.tryEmit("Order cancelled")
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _events.tryEmit(e.localizedMessage ?: "Could not cancel order")
      }
    }
  }

  fun syncCatalogFromFirestore() {
    viewModelScope.launch {
      try {
        repository.seedStarterProductsIfEmpty()
        _events.tryEmit("Catalog synced with Firestore")
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _events.tryEmit(e.localizedMessage ?: "Failed to sync catalog")
      }
    }
  }

  fun addSellerProduct(
    title: String,
    brand: String,
    category: String,
    price: Long,
    mrp: Long,
    description: String,
    badge: String,
    photoBase64: String = ""
  ) {
    viewModelScope.launch {
      try {
        val cat = ProductCategory.entries.firstOrNull {
          it.label.equals(category, ignoreCase = true)
        } ?: ProductCategory.ELECTRONICS
        val newProduct = Product(
          id = "", // Blank so repository uses colRef.document() auto-generated Firestore ID
          title = title,
          brand = brand.ifBlank { "MyStore" },
          categoryName = cat.label,
          price = price.coerceAtLeast(1L),
          mrp = mrp.coerceAtLeast(price.coerceAtLeast(1L)),
          rating = 4.6,
          reviewCount = 24L,
          highlights = listOf("Direct from Seller Desk", "QuickKart Quality Checked", "Fast Dispatch"),
          description = description.ifBlank { "Seller direct item" },
          badge = badge.ifBlank { "Seller Pick" },
          imageKey = cat.label.lowercase(),
          photoBase64 = photoBase64.take(250000),
          deliveryDays = 1L,
          inStock = true,
          sellerListed = true
        )
        repository.addProduct(newProduct)
        _events.tryEmit("Product published to Firestore catalog")
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _events.tryEmit(e.localizedMessage ?: "Failed to publish product")
      }
    }
  }

  fun deleteSellerProduct(productId: String) {
    viewModelScope.launch {
      try {
        if (productId.startsWith("cust_")) {
          repository.deleteCustomProduct(productId)
        } else {
          repository.deleteProduct(productId)
        }
        _events.tryEmit("Store product removed")
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _events.tryEmit(e.localizedMessage ?: "Failed to delete product")
      }
    }
  }

  fun updateProductPricingAndStock(
    productId: String,
    price: Long,
    mrp: Long,
    inStock: Boolean,
    photoBase64: String? = null
  ) {
    viewModelScope.launch {
      try {
        repository.updateProductPricingAndStock(productId, price, mrp, inStock, photoBase64)
        _events.tryEmit("Product details updated")
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _events.tryEmit(e.localizedMessage ?: "Failed to update product")
      }
    }
  }

  fun updateProfile(displayName: String, phone: String) {
    viewModelScope.launch {
      try {
        repository.updateUserProfile(displayName, phone)
        _events.tryEmit("Profile updated")
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _events.tryEmit(e.localizedMessage ?: "Failed to update profile")
      }
    }
  }

  private fun calculatePrices(
    items: List<CartItem>,
    coupon: String,
    speed: String
  ): PriceBreakdown {
    if (items.isEmpty()) return PriceBreakdown(0, 0, 0, 0, 0, 0, 0)
    val mrpTotal = items.sumOf { it.mrp * it.quantity }
    val subtotal = items.sumOf { it.price * it.quantity }
    val productSavings = (mrpTotal - subtotal).coerceAtLeast(0L)
    val couponDiscount = when (coupon) {
      "QUICK10" -> ((subtotal * 10) / 100).coerceAtMost(2000L)
      "SUPER500" -> if (subtotal >= 2000L) 500L else 0L
      else -> 0L
    }.coerceAtMost(subtotal)
    val baseDeliveryFee = if (speed == "EXPRESS") {
      if (coupon == "FREEDEL" || subtotal >= 2500L) 0L else 49L
    } else {
      if (subtotal >= 499L || coupon == "FREEDEL") 0L else 40L
    }
    val totalPayable = (subtotal - couponDiscount + baseDeliveryFee).coerceAtLeast(1L)
    val totalSavings = productSavings + couponDiscount
    return PriceBreakdown(
      mrpTotal = mrpTotal,
      subtotal = subtotal,
      productSavings = productSavings,
      couponDiscount = couponDiscount,
      deliveryFee = baseDeliveryFee,
      totalPayable = totalPayable,
      totalSavings = totalSavings
    )
  }

  private fun <T> Flow<T>.toUiState(fallbackError: String): StateFlow<UiState<T>> =
    this.map<T, UiState<T>> { UiState.Success(it) }
      .catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
          .filterIsInstance<FirebaseFirestoreException>()
          .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w(TAG, fallbackError, rootError)
        emit(UiState.Error(rootError.message ?: fallbackError))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = UiState.Loading
      )

  private companion object {
    const val TAG = "QuickKartVM"
    const val STOP_TIMEOUT_MS = 5000L
  }
}
