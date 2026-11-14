package com.example.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.ui.auth.signOutUser
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.AddressMapPickerScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OrderDetailScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.SellerDeskScreen
import com.example.ui.screens.WishlistScreen
import com.example.ui.viewmodel.QuickKartViewModel
import kotlinx.serialization.Serializable

@Serializable data object HomeRoute
@Serializable data object CategoriesRoute
@Serializable data object CartRoute
@Serializable data object OrdersRoute
@Serializable data object WishlistRoute
@Serializable data object AccountRoute
@Serializable data object CheckoutRoute
@Serializable data object AddressPickerRoute
@Serializable data object SellerDeskRoute
@Serializable data class OrderDetailRoute(val orderId: String)

private data class TopTab(
  val label: String,
  val route: Any,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String
)

private val topTabs = listOf(
  TopTab("Home", HomeRoute, Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
  TopTab("Categories", CategoriesRoute, Icons.Filled.Category, Icons.Outlined.Category, "nav_categories"),
  TopTab("Cart", CartRoute, Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart, "nav_cart"),
  TopTab("Orders", OrdersRoute, Icons.Filled.LocalShipping, Icons.Outlined.LocalShipping, "nav_orders"),
  TopTab("Account", AccountRoute, Icons.Filled.Person, Icons.Outlined.Person, "nav_account")
)

@Composable
fun QuickKartMainScaffold(
  viewModel: QuickKartViewModel,
  userEmail: String,
  userDisplayName: String
) {
  val navController = rememberNavController()
  val snackbarHostState = remember { SnackbarHostState() }
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
  val filteredProductsState by viewModel.filteredProductsState.collectAsStateWithLifecycle()
  val sellerListedProducts by viewModel.sellerListedProducts.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
  val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
  val wishlistIds by viewModel.wishlistProductIds.collectAsStateWithLifecycle()
  val cartCount by viewModel.cartCount.collectAsStateWithLifecycle()
  val cartState by viewModel.cartItems.collectAsStateWithLifecycle()
  val wishlistState by viewModel.wishlistItems.collectAsStateWithLifecycle()
  val addressesState by viewModel.addresses.collectAsStateWithLifecycle()
  val activeAddress by viewModel.activeAddress.collectAsStateWithLifecycle()
  val priceBreakdown by viewModel.priceBreakdown.collectAsStateWithLifecycle()
  val appliedCoupon by viewModel.appliedCoupon.collectAsStateWithLifecycle()
  val deliverySpeed by viewModel.deliverySpeed.collectAsStateWithLifecycle()
  val paymentMethod by viewModel.paymentMethod.collectAsStateWithLifecycle()
  val isPlacingOrder by viewModel.isPlacingOrder.collectAsStateWithLifecycle()
  val ordersState by viewModel.orders.collectAsStateWithLifecycle()
  val profileState by viewModel.userProfile.collectAsStateWithLifecycle()
  val productPhotos = remember(allProducts) {
    allProducts.filter { it.photoBase64.isNotBlank() }.associate { it.id to it.photoBase64 }
  }

  LaunchedEffect(Unit) {
    viewModel.events.collect { msg ->
      snackbarHostState.showSnackbar(msg)
    }
  }

  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentDest = navBackStackEntry?.destination
  val showChrome = currentDest?.let { dest ->
    dest.hasRoute<HomeRoute>() ||
      dest.hasRoute<CategoriesRoute>() ||
      dest.hasRoute<CartRoute>() ||
      dest.hasRoute<OrdersRoute>() ||
      dest.hasRoute<WishlistRoute>() ||
      dest.hasRoute<AccountRoute>()
  } ?: true

  fun navigateTopTab(route: Any) {
    navController.navigate(route) {
      popUpTo(navController.graph.findStartDestination().id) {
        saveState = true
      }
      launchSingleTop = true
      restoreState = true
    }
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val isWideScreen = maxWidth >= 600.dp

    Scaffold(
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      contentWindowInsets = WindowInsets(0, 0, 0, 0),
      bottomBar = {
        if (showChrome && !isWideScreen) {
          NavigationBar {
            topTabs.forEach { tab ->
              val selected = currentDest?.hasRoute(tab.route::class) == true
              NavigationBarItem(
                selected = selected,
                onClick = { navigateTopTab(tab.route) },
                icon = {
                  val icon = if (selected) tab.selectedIcon else tab.unselectedIcon
                  if (tab.route == CartRoute && cartCount > 0) {
                    BadgedBox(badge = { Badge { Text("$cartCount") } }) {
                      Icon(imageVector = icon, contentDescription = tab.label)
                    }
                  } else {
                    Icon(imageVector = icon, contentDescription = tab.label)
                  }
                },
                label = { Text(tab.label) },
                modifier = Modifier.testTag(tab.testTag)
              )
            }
          }
        }
      }
    ) { innerPadding ->
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        if (showChrome && isWideScreen) {
          NavigationRail {
            topTabs.forEach { tab ->
              val selected = currentDest?.hasRoute(tab.route::class) == true
              NavigationRailItem(
                selected = selected,
                onClick = { navigateTopTab(tab.route) },
                icon = {
                  val icon = if (selected) tab.selectedIcon else tab.unselectedIcon
                  if (tab.route == CartRoute && cartCount > 0) {
                    BadgedBox(badge = { Badge { Text("$cartCount") } }) {
                      Icon(imageVector = icon, contentDescription = tab.label)
                    }
                  } else {
                    Icon(imageVector = icon, contentDescription = tab.label)
                  }
                },
                label = { Text(tab.label) },
                modifier = Modifier.testTag(tab.testTag)
              )
            }
            NavigationRailItem(
              selected = currentDest?.hasRoute<WishlistRoute>() == true,
              onClick = { navigateTopTab(WishlistRoute) },
              icon = {
                val selected = currentDest?.hasRoute<WishlistRoute>() == true
                Icon(
                  imageVector = if (selected) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                  contentDescription = "Wishlist"
                )
              },
              label = { Text("Wishlist") }
            )
          }
        }

        Box(modifier = Modifier.weight(1f)) {
          NavHost(
            navController = navController,
            startDestination = HomeRoute
          ) {
            composable<HomeRoute> {
              HomeScreen(
                products = allProducts,
                selectedCategory = selectedCategory,
                wishlistIds = wishlistIds,
                activeAddress = activeAddress,
                cartCount = cartCount,
                onSelectCategory = viewModel::setCategory,
                onSearchBarClick = { navigateTopTab(CategoriesRoute) },
                onLocationBarClick = { navController.navigate(AddressPickerRoute) },
                onCartClick = { navigateTopTab(CartRoute) },
                onSellerDeskClick = { navController.navigate(SellerDeskRoute) },
                onAddToCart = viewModel::addToCart,
                onBuyNow = { product ->
                  viewModel.addToCart(product)
                  navigateTopTab(CartRoute)
                },
                onToggleWishlist = viewModel::toggleWishlist,
                isWideScreen = isWideScreen
              )
            }

            composable<CategoriesRoute> {
              CategoriesScreen(
                productsState = filteredProductsState,
                searchQuery = searchQuery,
                selectedCategory = selectedCategory,
                sortOption = sortOption,
                wishlistIds = wishlistIds,
                activeAddress = activeAddress,
                onSearchQueryChange = viewModel::setSearchQuery,
                onSelectCategory = viewModel::setCategory,
                onSelectSort = viewModel::setSortOption,
                onAddToCart = viewModel::addToCart,
                onBuyNow = { product ->
                  viewModel.addToCart(product)
                  navigateTopTab(CartRoute)
                },
                onToggleWishlist = viewModel::toggleWishlist,
                onRetrySync = viewModel::syncCatalogFromFirestore,
                isWideScreen = isWideScreen
              )
            }

            composable<CartRoute> {
              CartScreen(
                cartState = cartState,
                priceBreakdown = priceBreakdown,
                appliedCoupon = appliedCoupon,
                activeAddress = activeAddress,
                onIncrement = { item -> viewModel.updateCartQuantity(item.id, item.quantity + 1) },
                onDecrement = { item -> viewModel.updateCartQuantity(item.id, item.quantity - 1) },
                onRemove = { item -> viewModel.removeFromCart(item.id) },
                onApplyCoupon = viewModel::applyCoupon,
                onClearCoupon = viewModel::clearCoupon,
                onChangeAddressClick = { navController.navigate(AddressPickerRoute) },
                onCheckoutClick = { navController.navigate(CheckoutRoute) },
                onExploreClick = { navigateTopTab(HomeRoute) },
                productPhotos = productPhotos
              )
            }

            composable<OrdersRoute> {
              OrdersScreen(
                ordersState = ordersState,
                onOrderClick = { orderId -> navController.navigate(OrderDetailRoute(orderId)) },
                onShopNowClick = { navigateTopTab(HomeRoute) }
              )
            }

            composable<WishlistRoute> {
              WishlistScreen(
                wishlistState = wishlistState,
                onMoveToCart = viewModel::moveWishlistItemToCart,
                onRemoveFromWishlist = { item ->
                  allProducts.firstOrNull { it.id == item.productId }?.let(viewModel::toggleWishlist)
                },
                onExploreClick = { navigateTopTab(HomeRoute) },
                productPhotos = productPhotos
              )
            }

            composable<AccountRoute> {
              AccountScreen(
                profileState = profileState,
                addressesState = addressesState,
                ordersState = ordersState,
                userEmail = userEmail,
                onOpenAddressPicker = { navController.navigate(AddressPickerRoute) },
                onDeleteAddress = viewModel::deleteAddress,
                onOpenSellerDesk = { navController.navigate(SellerDeskRoute) },
                onSignOut = {
                  signOutUser(
                    credentialManager = credentialManager,
                    scope = scope
                  )
                }
              )
            }

            composable<CheckoutRoute> {
              CheckoutScreen(
                addressesState = addressesState,
                activeAddress = activeAddress,
                priceBreakdown = priceBreakdown,
                deliverySpeed = deliverySpeed,
                paymentMethod = paymentMethod,
                isPlacingOrder = isPlacingOrder,
                onSelectAddress = viewModel::selectAddress,
                onOpenAddressPicker = { navController.navigate(AddressPickerRoute) },
                onSelectDeliverySpeed = viewModel::setDeliverySpeed,
                onSelectPaymentMethod = viewModel::setPaymentMethod,
                onPlaceOrder = {
                  viewModel.placeOrder { newOrderId ->
                    navController.navigate(OrderDetailRoute(newOrderId)) {
                      popUpTo(HomeRoute)
                    }
                  }
                },
                onBack = { navController.popBackStack() }
              )
            }

            composable<AddressPickerRoute> {
              AddressMapPickerScreen(
                defaultRecipientName = userDisplayName,
                onSaveAddress = { addr ->
                  viewModel.saveAddress(addr) {
                    navController.popBackStack()
                  }
                },
                onBack = { navController.popBackStack() }
              )
            }

            composable<SellerDeskRoute> {
              SellerDeskScreen(
                sellerProducts = sellerListedProducts,
                allProducts = allProducts,
                ordersState = ordersState,
                onAddProduct = viewModel::addSellerProduct,
                onUpdateProductPricing = viewModel::updateProductPricingAndStock,
                onDeleteProduct = viewModel::deleteSellerProduct,
                onAdvanceOrderStatus = viewModel::advanceOrderStatus,
                onOpenOrderDetail = { orderId -> navController.navigate(OrderDetailRoute(orderId)) },
                onBack = { navController.popBackStack() }
              )
            }

            composable<OrderDetailRoute> { entry ->
              val route = entry.toRoute<OrderDetailRoute>()
              val order by viewModel.observeOrder(route.orderId).collectAsStateWithLifecycle(initialValue = null)
              val lineItems by viewModel.observeOrderItems(route.orderId).collectAsStateWithLifecycle(initialValue = emptyList())
              OrderDetailScreen(
                order = order,
                lineItems = lineItems,
                onAdvanceStatus = viewModel::advanceOrderStatus,
                onCancelOrder = viewModel::cancelOrder,
                onBack = { navController.popBackStack() }
              )
            }
          }
        }
      }
    }
  }
}
