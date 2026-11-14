package com.example.data

import android.content.Context
import com.example.R
import com.example.model.CartItem
import com.example.model.CustomProduct
import com.example.model.DeliveryAddress
import com.example.model.OrderLineItem
import com.example.model.OrderRecord
import com.example.model.OrderStatus
import com.example.model.Product
import com.example.model.UserProfile
import com.example.model.WishlistItem
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class QuickKartRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth
) {

  constructor(context: Context) : this(
    db = FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
  }

  private fun userDoc(uid: String) = db.collection("users").document(uid)

  fun observeUserProfile(uid: String): Flow<UserProfile?> = flow {
    val docRef = userDoc(uid)
    emitAll(
      docRef.snapshots()
        .map { snapshot ->
          if (snapshot.exists()) {
            snapshot.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          } else {
            null
          }
        }
        .catch { error ->
          handleFlowError(error, OperationType.GET, docRef.path, uid)
          throw error
        }
    )
  }

  suspend fun ensureUserProfile(displayName: String, email: String, phone: String = "") {
    val uid = requireUserId()
    val docRef = userDoc(uid)
    try {
      val existing = docRef.get().await()
      if (!existing.exists()) {
        val payload = mapOf(
          "userId" to uid,
          "displayName" to displayName.ifBlank { "QuickKart Shopper" }.take(80),
          "email" to email.take(120),
          "phone" to phone.take(20),
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  suspend fun updateUserProfile(displayName: String, phone: String) {
    val uid = requireUserId()
    val docRef = userDoc(uid)
    try {
      docRef.update(
        mapOf(
          "displayName" to displayName.trim().ifBlank { "QuickKart Shopper" }.take(80),
          "phone" to phone.trim().take(20),
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  fun observeAddresses(uid: String): Flow<List<DeliveryAddress>> = flow {
    val colRef = userDoc(uid).collection("addresses")
    emitAll(
      colRef.snapshots()
        .map { snapshot ->
          snapshot.toObjects(DeliveryAddress::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedWith(compareByDescending<DeliveryAddress> { it.isDefault }.thenByDescending { it.createdAt ?: Timestamp(0, 0) })
        }
        .catch { error ->
          handleFlowError(error, OperationType.LIST, colRef.path, uid)
          throw error
        }
    )
  }

  suspend fun saveAddress(address: DeliveryAddress): String {
    val uid = requireUserId()
    val colRef = userDoc(uid).collection("addresses")
    val docId = address.id.ifBlank { "addr_${UUID.randomUUID().toString().replace("-", "").take(12)}" }
    val docRef = colRef.document(docId)
    try {
      if (address.isDefault) {
        val existing = colRef.get().await()
        val batch = db.batch()
        existing.documents.forEach { doc ->
          if (doc.id != docId && doc.getBoolean("isDefault") == true) {
            batch.update(doc.reference, mapOf("isDefault" to false, "updatedAt" to FieldValue.serverTimestamp()))
          }
        }
        val payload = mapOf(
          "userId" to uid,
          "label" to address.label.trim().ifBlank { "Home" }.take(30),
          "recipientName" to address.recipientName.trim().ifBlank { "Customer" }.take(80),
          "phone" to address.phone.trim().ifBlank { "9876543210" }.take(20),
          "streetAddress" to address.streetAddress.trim().take(200),
          "city" to address.city.trim().ifBlank { "Bengaluru" }.take(60),
          "pincode" to address.pincode.trim().ifBlank { "560001" }.take(12),
          "latitude" to address.latitude.coerceIn(-90.0, 90.0),
          "longitude" to address.longitude.coerceIn(-180.0, 180.0),
          "isDefault" to true,
          "createdAt" to (address.createdAt ?: FieldValue.serverTimestamp()),
          "updatedAt" to FieldValue.serverTimestamp()
        )
        if (address.id.isBlank()) {
          batch.set(docRef, payload)
        } else {
          batch.update(
            docRef,
            mapOf(
              "label" to payload["label"]!!,
              "recipientName" to payload["recipientName"]!!,
              "phone" to payload["phone"]!!,
              "streetAddress" to payload["streetAddress"]!!,
              "city" to payload["city"]!!,
              "pincode" to payload["pincode"]!!,
              "latitude" to payload["latitude"]!!,
              "longitude" to payload["longitude"]!!,
              "isDefault" to true,
              "updatedAt" to FieldValue.serverTimestamp()
            )
          )
        }
        batch.commit().await()
      } else {
        if (address.id.isBlank()) {
          val payload = mapOf(
            "userId" to uid,
            "label" to address.label.trim().ifBlank { "Home" }.take(30),
            "recipientName" to address.recipientName.trim().ifBlank { "Customer" }.take(80),
            "phone" to address.phone.trim().ifBlank { "9876543210" }.take(20),
            "streetAddress" to address.streetAddress.trim().take(200),
            "city" to address.city.trim().ifBlank { "Bengaluru" }.take(60),
            "pincode" to address.pincode.trim().ifBlank { "560001" }.take(12),
            "latitude" to address.latitude.coerceIn(-90.0, 90.0),
            "longitude" to address.longitude.coerceIn(-180.0, 180.0),
            "isDefault" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
          )
          docRef.set(payload).await()
        } else {
          docRef.update(
            mapOf(
              "label" to address.label.trim().ifBlank { "Home" }.take(30),
              "recipientName" to address.recipientName.trim().ifBlank { "Customer" }.take(80),
              "phone" to address.phone.trim().ifBlank { "9876543210" }.take(20),
              "streetAddress" to address.streetAddress.trim().take(200),
              "city" to address.city.trim().ifBlank { "Bengaluru" }.take(60),
              "pincode" to address.pincode.trim().ifBlank { "560001" }.take(12),
              "latitude" to address.latitude.coerceIn(-90.0, 90.0),
              "longitude" to address.longitude.coerceIn(-180.0, 180.0),
              "isDefault" to false,
              "updatedAt" to FieldValue.serverTimestamp()
            )
          ).await()
        }
      }
      return docId
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  suspend fun deleteAddress(addressId: String) {
    val uid = requireUserId()
    val docRef = userDoc(uid).collection("addresses").document(addressId)
    try {
      docRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  fun observeCart(uid: String): Flow<List<CartItem>> = flow {
    val colRef = userDoc(uid).collection("cart")
    emitAll(
      colRef.snapshots()
        .map { snapshot ->
          snapshot.toObjects(CartItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
        }
        .catch { error ->
          handleFlowError(error, OperationType.LIST, colRef.path, uid)
          throw error
        }
    )
  }

  suspend fun addToCart(product: Product, quantityDelta: Long = 1L) {
    val uid = requireUserId()
    val safeDocId = product.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64)
    val docRef = userDoc(uid).collection("cart").document(safeDocId)
    try {
      val snapshot = docRef.get().await()
      if (snapshot.exists()) {
        val currentQty = snapshot.getLong("quantity") ?: 1L
        val nextQty = (currentQty + quantityDelta).coerceIn(1L, 20L)
        docRef.update(
          mapOf(
            "quantity" to nextQty,
            "price" to product.price,
            "mrp" to product.mrp.coerceAtLeast(product.price),
            "updatedAt" to FieldValue.serverTimestamp()
          )
        ).await()
      } else {
        val payload = mapOf(
          "userId" to uid,
          "productId" to safeDocId,
          "title" to product.title.take(120),
          "brand" to product.brand.take(60),
          "category" to product.category.label.take(40),
          "price" to product.price.coerceAtLeast(1L),
          "mrp" to product.mrp.coerceAtLeast(product.price),
          "quantity" to quantityDelta.coerceIn(1L, 20L),
          "imageKey" to product.imageKey.ifBlank { "electronics" }.take(64),
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  suspend fun updateCartQuantity(cartItemId: String, newQuantity: Long) {
    val uid = requireUserId()
    val docRef = userDoc(uid).collection("cart").document(cartItemId)
    try {
      if (newQuantity <= 0L) {
        docRef.delete().await()
      } else {
        docRef.update(
          mapOf(
            "quantity" to newQuantity.coerceIn(1L, 20L),
            "updatedAt" to FieldValue.serverTimestamp()
          )
        ).await()
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  suspend fun removeFromCart(cartItemId: String) {
    val uid = requireUserId()
    val docRef = userDoc(uid).collection("cart").document(cartItemId)
    try {
      docRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  fun observeWishlist(uid: String): Flow<List<WishlistItem>> = flow {
    val colRef = userDoc(uid).collection("wishlist")
    emitAll(
      colRef.snapshots()
        .map { snapshot ->
          snapshot.toObjects(WishlistItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
        }
        .catch { error ->
          handleFlowError(error, OperationType.LIST, colRef.path, uid)
          throw error
        }
    )
  }

  suspend fun toggleWishlist(product: Product) {
    val uid = requireUserId()
    val safeDocId = product.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64)
    val docRef = userDoc(uid).collection("wishlist").document(safeDocId)
    try {
      val snapshot = docRef.get().await()
      if (snapshot.exists()) {
        docRef.delete().await()
      } else {
        val payload = mapOf(
          "userId" to uid,
          "productId" to safeDocId,
          "title" to product.title.take(120),
          "brand" to product.brand.take(60),
          "category" to product.category.label.take(40),
          "price" to product.price.coerceAtLeast(1L),
          "mrp" to product.mrp.coerceAtLeast(product.price),
          "rating" to product.rating.coerceIn(0.0, 5.0),
          "imageKey" to product.imageKey.ifBlank { "electronics" }.take(64),
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      throw e
    }
  }

  fun observeOrders(uid: String): Flow<List<OrderRecord>> = flow {
    val colRef = userDoc(uid).collection("orders")
    emitAll(
      colRef.orderBy("createdAt", Query.Direction.DESCENDING)
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(OrderRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        }
        .catch { error ->
          handleFlowError(error, OperationType.LIST, colRef.path, uid)
          throw error
        }
    )
  }

  fun observeOrderById(uid: String, orderId: String): Flow<OrderRecord?> = flow {
    val docRef = userDoc(uid).collection("orders").document(orderId)
    emitAll(
      docRef.snapshots()
        .map { snapshot ->
          if (snapshot.exists()) {
            snapshot.toObject(OrderRecord::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          } else {
            null
          }
        }
        .catch { error ->
          handleFlowError(error, OperationType.GET, docRef.path, uid)
          throw error
        }
    )
  }

  fun observeOrderItems(uid: String, orderId: String): Flow<List<OrderLineItem>> = flow {
    val colRef = userDoc(uid).collection("orders").document(orderId).collection("items")
    emitAll(
      colRef.snapshots()
        .map { snapshot ->
          snapshot.toObjects(OrderLineItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        }
        .catch { error ->
          handleFlowError(error, OperationType.LIST, colRef.path, uid)
          throw error
        }
    )
  }

  suspend fun placeOrder(
    cartItems: List<CartItem>,
    address: DeliveryAddress,
    paymentMethod: String,
    deliverySpeed: String,
    couponCode: String,
    subtotal: Long,
    discount: Long,
    deliveryFee: Long,
    totalAmount: Long,
    clearCartAfterOrder: Boolean = true
  ): String {
    val uid = requireUserId()
    require(cartItems.isNotEmpty()) { "Cannot place an order with an empty cart" }

    val orderId = "ord_${UUID.randomUUID().toString().replace("-", "").take(14)}"
    val orderRef = userDoc(uid).collection("orders").document(orderId)
    val nearestHub = CatalogData.nearestHubFor(address.latitude, address.longitude)
    val orderNumber = "QK-${(10000..99999).random()}"
    val itemTitles = cartItems.take(25).map { it.title.take(120) }
    val itemCount = cartItems.sumOf { it.quantity }.coerceIn(1L, 100L)
    val summary = cartItems.joinToString(", ") { "${it.title} x${it.quantity}" }.take(300)

    val orderPayload = mapOf(
      "userId" to uid,
      "orderNumber" to orderNumber,
      "customerName" to address.recipientName.trim().ifBlank { "QuickKart Customer" }.take(80),
      "customerPhone" to address.phone.trim().ifBlank { "9876543210" }.take(20),
      "deliveryAddress" to address.streetAddress.trim().ifBlank { "MG Road" }.take(250),
      "deliveryCity" to address.city.trim().ifBlank { "Bengaluru" }.take(60),
      "deliveryPincode" to address.pincode.trim().ifBlank { "560001" }.take(12),
      "deliveryLat" to address.latitude.coerceIn(-90.0, 90.0),
      "deliveryLng" to address.longitude.coerceIn(-180.0, 180.0),
      "hubLat" to nearestHub.latitude,
      "hubLng" to nearestHub.longitude,
      "paymentMethod" to paymentMethod.take(16),
      "deliverySpeed" to deliverySpeed.take(16),
      "couponCode" to couponCode.trim().take(24),
      "subtotal" to subtotal.coerceAtLeast(0L),
      "discount" to discount.coerceIn(0L, subtotal.coerceAtLeast(0L)),
      "deliveryFee" to deliveryFee.coerceIn(0L, 5000L),
      "totalAmount" to totalAmount.coerceAtLeast(1L),
      "status" to OrderStatus.PLACED.key,
      "itemsSummary" to summary.ifBlank { "QuickKart Order" },
      "itemTitles" to itemTitles,
      "itemCount" to itemCount,
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp()
    )

    try {
      val batch = db.batch()
      batch.set(orderRef, orderPayload)

      cartItems.forEachIndexed { index, item ->
        val lineId = "line_${index}_${item.productId.take(20)}"
        val lineRef = orderRef.collection("items").document(lineId)
        val linePayload = mapOf(
          "userId" to uid,
          "orderId" to orderId,
          "productId" to item.productId.take(64),
          "title" to item.title.take(120),
          "brand" to item.brand.take(60),
          "price" to item.price.coerceAtLeast(1L),
          "mrp" to item.mrp.coerceAtLeast(item.price),
          "quantity" to item.quantity.coerceIn(1L, 20L),
          "imageKey" to item.imageKey.ifBlank { "electronics" }.take(64),
          "createdAt" to FieldValue.serverTimestamp()
        )
        batch.set(lineRef, linePayload)

        if (clearCartAfterOrder && item.id.isNotBlank()) {
          batch.delete(userDoc(uid).collection("cart").document(item.id))
        }
      }

      batch.commit().await()
      return orderId
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, orderRef.path)
      throw e
    }
  }

  suspend fun updateOrderStatus(orderId: String, nextStatus: OrderStatus) {
    val uid = requireUserId()
    val docRef = userDoc(uid).collection("orders").document(orderId)
    try {
      docRef.update(
        mapOf(
          "status" to nextStatus.key,
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  fun observeProducts(uid: String): Flow<List<Product>> = flow {
    val colRef = userDoc(uid).collection("products")
    emitAll(
      colRef.orderBy("createdAt", Query.Direction.DESCENDING)
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(Product::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        }
        .catch { error ->
          handleFlowError(error, OperationType.LIST, colRef.path, uid)
          throw error
        }
    )
  }

  suspend fun seedStarterProductsIfEmpty() {
    val uid = requireUserId()
    val colRef = userDoc(uid).collection("products")
    try {
      val existing = colRef.limit(1).get().await()
      if (!existing.isEmpty) return

      val batch = db.batch()
      CatalogData.curatedProducts.forEach { product ->
        val safeDocId = product.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64)
        val docRef = colRef.document(safeDocId)
        val payload = buildProductPayload(uid, product, sellerListed = false)
        batch.set(docRef, payload)
      }
      batch.commit().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, colRef.path)
      throw e
    }
  }

  suspend fun addProduct(product: Product): String {
    val uid = requireUserId()
    val colRef = userDoc(uid).collection("products")
    // When product.id is blank, colRef.document() auto-generates a unique 20-char Firestore document ID.
    val docRef = if (product.id.isBlank()) {
      colRef.document()
    } else {
      colRef.document(product.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64))
    }
    val payload = buildProductPayload(uid, product, sellerListed = product.sellerListed)
    try {
      docRef.set(payload).await()
      return docRef.id
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
  }

  suspend fun deleteProduct(productId: String) {
    val uid = requireUserId()
    val docRef = userDoc(uid).collection("products").document(productId)
    try {
      docRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  suspend fun updateProductPricingAndStock(
    productId: String,
    price: Long,
    mrp: Long,
    inStock: Boolean,
    photoBase64: String? = null
  ) {
    val uid = requireUserId()
    val safePrice = price.coerceIn(1L, 10000000L)
    val safeMrp = mrp.coerceIn(safePrice, 10000000L)
    val collectionName = if (productId.startsWith("cust_")) "custom_products" else "products"
    val docRef = userDoc(uid).collection(collectionName).document(productId)
    val updates = mutableMapOf<String, Any>(
      "price" to safePrice,
      "mrp" to safeMrp,
      "inStock" to inStock,
      "updatedAt" to FieldValue.serverTimestamp()
    )
    if (photoBase64 != null && collectionName == "products") {
      updates["photoBase64"] = photoBase64.take(250000)
    }
    try {
      docRef.update(updates).await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      throw e
    }
  }

  private fun buildProductPayload(
    uid: String,
    product: Product,
    sellerListed: Boolean
  ): Map<String, Any> {
    val safePrice = product.price.coerceIn(1L, 10000000L)
    val safeMrp = product.mrp.coerceIn(safePrice, 10000000L)
    return mapOf(
      "userId" to uid,
      "title" to product.title.trim().ifBlank { "QuickKart Product" }.take(120),
      "brand" to product.brand.trim().ifBlank { "QuickKart" }.take(60),
      "categoryName" to product.categoryName.trim().ifBlank { "Electronics" }.take(40),
      "price" to safePrice,
      "mrp" to safeMrp,
      "rating" to product.rating.coerceIn(0.0, 5.0),
      "reviewCount" to product.reviewCount.coerceIn(0L, 10000000L),
      "highlights" to product.highlights.take(10).map { it.take(160) },
      "description" to product.description.trim().take(500),
      "badge" to product.badge.trim().take(32),
      "imageKey" to product.imageKey.ifBlank { "electronics" }.take(64),
      "photoBase64" to product.photoBase64.take(250000),
      "deliveryDays" to product.deliveryDays.coerceIn(0L, 60L),
      "inStock" to product.inStock,
      "sellerListed" to sellerListed,
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp()
    )
  }

  fun observeCustomProducts(uid: String): Flow<List<CustomProduct>> = flow {
    val colRef = userDoc(uid).collection("custom_products")
    emitAll(
      colRef.snapshots()
        .map { snapshot ->
          snapshot.toObjects(CustomProduct::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
        }
        .catch { error ->
          handleFlowError(error, OperationType.LIST, colRef.path, uid)
          throw error
        }
    )
  }

  suspend fun addCustomProduct(
    title: String,
    brand: String,
    category: String,
    price: Long,
    mrp: Long,
    description: String,
    badge: String
  ): String {
    val uid = requireUserId()
    val prodId = "cust_${UUID.randomUUID().toString().replace("-", "").take(12)}"
    val docRef = userDoc(uid).collection("custom_products").document(prodId)
    val payload = mapOf(
      "userId" to uid,
      "title" to title.trim().take(120),
      "brand" to brand.trim().ifBlank { "MyStore" }.take(60),
      "category" to category.trim().ifBlank { "Electronics" }.take(40),
      "price" to price.coerceAtLeast(1L),
      "mrp" to mrp.coerceAtLeast(price.coerceAtLeast(1L)),
      "description" to description.trim().ifBlank { "Seller direct item" }.take(500),
      "badge" to badge.trim().ifBlank { "Seller Direct" }.take(32),
      "inStock" to true,
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp()
    )
    try {
      docRef.set(payload).await()
      return prodId
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      throw e
    }
  }

  suspend fun deleteCustomProduct(productId: String) {
    val uid = requireUserId()
    val docRef = userDoc(uid).collection("custom_products").document(productId)
    try {
      docRef.delete().await()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      throw e
    }
  }

  private fun handleFlowError(
    error: Throwable,
    operationType: OperationType,
    path: String,
    uid: String
  ) {
    val firestoreError = generateSequence(error) { it.cause }
      .filterIsInstance<FirebaseFirestoreException>()
      .firstOrNull()
    if (firestoreError == null && error is CancellationException) return
    val exceptionToLog = firestoreError ?: (error as? Exception)
    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
      handleFirestoreError(exceptionToLog, operationType, path)
    }
  }
}
