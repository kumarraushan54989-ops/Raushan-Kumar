package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Product(
  @DocumentId val id: String = "",
  val userId: String = "",
  val title: String = "",
  val brand: String = "",
  val categoryName: String = ProductCategory.ELECTRONICS.label,
  val price: Long = 0L,
  val mrp: Long = 0L,
  val rating: Double = 4.5,
  val reviewCount: Long = 0L,
  val highlights: List<String> = emptyList(),
  val description: String = "",
  val badge: String = "",
  val imageKey: String = "electronics",
  val photoBase64: String = "",
  val deliveryDays: Long = 1L,
  val inStock: Boolean = true,
  val sellerListed: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
) {
  @get:Exclude
  val category: ProductCategory
    get() = ProductCategory.entries.firstOrNull {
      it.label.equals(categoryName, ignoreCase = true)
    } ?: ProductCategory.ELECTRONICS

  @get:Exclude
  val discountPercent: Int
    get() = if (mrp > price && mrp > 0) (((mrp - price) * 100) / mrp).toInt() else 0
}
