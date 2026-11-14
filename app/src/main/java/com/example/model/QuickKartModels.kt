package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

enum class ProductCategory(val label: String) {
  ALL("All"),
  MOBILES("Mobiles"),
  ELECTRONICS("Electronics"),
  FASHION("Fashion"),
  HOME("Home"),
  APPLIANCES("Appliances"),
  BEAUTY("Beauty"),
  GROCERY("Grocery")
}

enum class SortOption(val label: String) {
  POPULARITY("Popularity"),
  PRICE_LOW_HIGH("Price: Low to High"),
  PRICE_HIGH_LOW("Price: High to Low"),
  RATING("Top Rated"),
  DISCOUNT("Best Discount")
}

enum class OrderStatus(val key: String, val label: String) {
  PLACED("PLACED", "Order Placed"),
  PACKED("PACKED", "Packed at Hub"),
  SHIPPED("SHIPPED", "Shipped"),
  OUT_FOR_DELIVERY("OUT_FOR_DELIVERY", "Out for Delivery"),
  DELIVERED("DELIVERED", "Delivered"),
  CANCELLED("CANCELLED", "Cancelled");

  companion object {
    fun fromKey(key: String): OrderStatus =
      entries.firstOrNull { it.key == key } ?: PLACED
  }
}

data class DeliveryHub(
  val name: String,
  val city: String,
  val latitude: Double,
  val longitude: Double
)

@IgnoreExtraProperties
data class UserProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val displayName: String = "",
  val email: String = "",
  val phone: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class DeliveryAddress(
  @DocumentId val id: String = "",
  val userId: String = "",
  val label: String = "Home",
  val recipientName: String = "",
  val phone: String = "",
  val streetAddress: String = "",
  val city: String = "",
  val pincode: String = "",
  val latitude: Double = 12.9716,
  val longitude: Double = 77.5946,
  @get:PropertyName("isDefault") @set:PropertyName("isDefault") var isDefault: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CartItem(
  @DocumentId val id: String = "",
  val userId: String = "",
  val productId: String = "",
  val title: String = "",
  val brand: String = "",
  val category: String = "",
  val price: Long = 0L,
  val mrp: Long = 0L,
  val quantity: Long = 1L,
  val imageKey: String = "electronics",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class WishlistItem(
  @DocumentId val id: String = "",
  val userId: String = "",
  val productId: String = "",
  val title: String = "",
  val brand: String = "",
  val category: String = "",
  val price: Long = 0L,
  val mrp: Long = 0L,
  val rating: Double = 4.5,
  val imageKey: String = "electronics",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class OrderRecord(
  @DocumentId val id: String = "",
  val userId: String = "",
  val orderNumber: String = "",
  val customerName: String = "",
  val customerPhone: String = "",
  val deliveryAddress: String = "",
  val deliveryCity: String = "",
  val deliveryPincode: String = "",
  val deliveryLat: Double = 12.9716,
  val deliveryLng: Double = 77.5946,
  val hubLat: Double = 12.9352,
  val hubLng: Double = 77.6245,
  val paymentMethod: String = "UPI",
  val deliverySpeed: String = "STANDARD",
  val couponCode: String = "",
  val subtotal: Long = 0L,
  val discount: Long = 0L,
  val deliveryFee: Long = 0L,
  val totalAmount: Long = 0L,
  val status: String = "PLACED",
  val itemsSummary: String = "",
  val itemTitles: List<String> = emptyList(),
  val itemCount: Long = 1L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class OrderLineItem(
  @DocumentId val id: String = "",
  val userId: String = "",
  val orderId: String = "",
  val productId: String = "",
  val title: String = "",
  val brand: String = "",
  val price: Long = 0L,
  val mrp: Long = 0L,
  val quantity: Long = 1L,
  val imageKey: String = "electronics",
  @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CustomProduct(
  @DocumentId val id: String = "",
  val userId: String = "",
  val title: String = "",
  val brand: String = "",
  val category: String = "Electronics",
  val price: Long = 0L,
  val mrp: Long = 0L,
  val description: String = "",
  val badge: String = "Seller Pick",
  val inStock: Boolean = true,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)
