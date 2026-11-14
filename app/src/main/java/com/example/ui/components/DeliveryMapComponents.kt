package com.example.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.BuildConfig
import com.example.data.CatalogData
import com.example.model.OrderRecord
import com.example.model.OrderStatus
import com.example.ui.theme.DealGreen
import com.example.ui.theme.ElectricAzure
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Locale

fun hasValidMapsApiKey(): Boolean {
  val key = BuildConfig.MAPS_API_KEY
  return key.isNotBlank() && key != "YOUR_GOOGLE_MAPS_API_KEY"
}

@Composable
fun DeliveryPinMapPicker(
  selectedLat: Double,
  selectedLng: Double,
  onLocationSelected: (lat: Double, lng: Double, cityHint: String?) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val pinLatLng = remember(selectedLat, selectedLng) { LatLng(selectedLat, selectedLng) }
  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(pinLatLng, 13f)
  }

  var locationNotice by remember { mutableStateOf<String?>(null) }
  var hasLocationPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    )
  }

  val fetchCurrentLocation: () -> Unit = {
    @SuppressLint("MissingPermission")
    fun requestFix() {
      val fusedClient = LocationServices.getFusedLocationProviderClient(context)
      fusedClient.lastLocation
        .addOnSuccessListener { loc ->
          if (loc != null) {
            onLocationSelected(loc.latitude, loc.longitude, null)
            locationNotice = "Pinned your current GPS coordinates"
          } else {
            locationNotice = "GPS fix unavailable — tap anywhere on the map or pick a city hub"
          }
        }
        .addOnFailureListener {
          locationNotice = "Could not read GPS — tap the map to pin manually"
        }
    }
    if (hasLocationPermission) requestFix()
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
      permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    hasLocationPermission = granted
    if (granted) {
      fetchCurrentLocation()
    } else {
      locationNotice = "Location permission denied — tap on map or choose a city hub below"
    }
  }

  LaunchedEffect(selectedLat, selectedLng) {
    cameraPositionState.animate(
      update = CameraUpdateFactory.newLatLngZoom(LatLng(selectedLat, selectedLng), 13.5f),
      durationMs = 450
    )
  }

  Column(modifier = modifier.fillMaxWidth()) {
    if (!hasValidMapsApiKey()) {
      Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 8.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(10.dp)
        ) {
          Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Add MAPS_API_KEY in AI Studio Secrets for Google Map tiles. Coordinates & pin routing remain active.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
          )
        }
      }
    }

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(220.dp)
        .clip(RoundedCornerShape(16.dp))
    ) {
      GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
        uiSettings = MapUiSettings(
          zoomControlsEnabled = true,
          myLocationButtonEnabled = false,
          mapToolbarEnabled = false
        ),
        onMapClick = { tapped ->
          onLocationSelected(tapped.latitude, tapped.longitude, null)
          locationNotice = "Delivery pin moved to ${String.format(Locale.US, "%.4f, %.4f", tapped.latitude, tapped.longitude)}"
        }
      ) {
        Marker(
          state = MarkerState(position = pinLatLng),
          title = "Delivery Pin",
          snippet = "Tap anywhere on map to move"
        )
      }

      Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        shape = RoundedCornerShape(10.dp),
        shadowElevation = 4.dp,
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(10.dp)
      ) {
        Text(
          text = "Tap map to pin exact doorstep",
          style = MaterialTheme.typography.labelSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      OutlinedButton(
        onClick = {
          if (hasLocationPermission) {
            fetchCurrentLocation()
          } else {
            permissionLauncher.launch(
              arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
              )
            )
          }
        },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .height(48.dp)
          .testTag("use_gps_button")
      ) {
        Icon(
          imageVector = Icons.Filled.MyLocation,
          contentDescription = "Use current GPS location",
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text("Use GPS", style = MaterialTheme.typography.labelLarge)
      }

      Text(
        text = String.format(Locale.US, "%.4f, %.4f", selectedLat, selectedLng),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    if (locationNotice != null) {
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = locationNotice!!,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary
      )
    }

    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = "Quick Hub Jump:",
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(vertical = 4.dp)
    ) {
      items(CatalogData.fulfillmentHubs) { hub ->
        AssistChip(
          onClick = {
            onLocationSelected(hub.latitude + 0.014, hub.longitude + 0.011, hub.city)
            locationNotice = "Pinned near ${hub.city} Hub"
          },
          label = { Text(hub.city, style = MaterialTheme.typography.labelMedium) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Filled.LocationSearching,
              contentDescription = null,
              modifier = Modifier.size(15.dp)
            )
          }
        )
      }
    }
  }
}

@Composable
fun OrderTrackingMapCard(
  order: OrderRecord,
  modifier: Modifier = Modifier
) {
  val hubLatLng = remember(order.hubLat, order.hubLng) { LatLng(order.hubLat, order.hubLng) }
  val destLatLng = remember(order.deliveryLat, order.deliveryLng) { LatLng(order.deliveryLat, order.deliveryLng) }
  val status = OrderStatus.fromKey(order.status)

  val progress = when (status) {
    OrderStatus.PLACED -> 0.05
    OrderStatus.PACKED -> 0.25
    OrderStatus.SHIPPED -> 0.55
    OrderStatus.OUT_FOR_DELIVERY -> 0.85
    OrderStatus.DELIVERED -> 1.0
    OrderStatus.CANCELLED -> 0.0
  }

  val courierLatLng = remember(hubLatLng, destLatLng, progress) {
    LatLng(
      hubLatLng.latitude + (destLatLng.latitude - hubLatLng.latitude) * progress,
      hubLatLng.longitude + (destLatLng.longitude - hubLatLng.longitude) * progress
    )
  }

  val distanceKm = remember(order) {
    CatalogData.haversineKm(order.hubLat, order.hubLng, order.deliveryLat, order.deliveryLng)
  }

  val etaText = when (status) {
    OrderStatus.DELIVERED -> "Delivered safely"
    OrderStatus.CANCELLED -> "Order cancelled"
    OrderStatus.OUT_FOR_DELIVERY -> "Arriving in ${(distanceKm * 2.5).toInt().coerceIn(8, 25)} mins"
    OrderStatus.SHIPPED -> "In transit • ${String.format(Locale.US, "%.1f", distanceKm)} km route"
    else -> "Preparing at fulfillment hub (${String.format(Locale.US, "%.1f", distanceKm)} km)"
  }

  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(destLatLng, 12f)
  }

  LaunchedEffect(hubLatLng, destLatLng) {
    val bounds = LatLngBounds.Builder()
      .include(hubLatLng)
      .include(destLatLng)
      .build()
    try {
      cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 90), 500)
    } catch (_: Exception) {
      cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(destLatLng, 12f))
    }
  }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Live Google Maps Delivery Tracker",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = etaText,
            style = MaterialTheme.typography.bodySmall,
            color = if (status == OrderStatus.CANCELLED) MaterialTheme.colorScheme.error else DealGreen,
            fontWeight = FontWeight.SemiBold
          )
        }
        Surface(
          color = MaterialTheme.colorScheme.primaryContainer,
          shape = RoundedCornerShape(8.dp)
        ) {
          Text(
            text = "${String.format(Locale.US, "%.1f", distanceKm)} km",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(210.dp)
          .clip(RoundedCornerShape(14.dp))
      ) {
        GoogleMap(
          modifier = Modifier.fillMaxSize(),
          cameraPositionState = cameraPositionState,
          uiSettings = MapUiSettings(zoomControlsEnabled = true, mapToolbarEnabled = false)
        ) {
          Marker(
            state = MarkerState(position = hubLatLng),
            title = "QuickKart Hub",
            snippet = "Order dispatch hub",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
          )
          Marker(
            state = MarkerState(position = destLatLng),
            title = "Delivery Pin",
            snippet = "${order.deliveryAddress}, ${order.deliveryCity}",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
          )
          if (status != OrderStatus.CANCELLED && status != OrderStatus.DELIVERED) {
            Marker(
              state = MarkerState(position = courierLatLng),
              title = "Delivery Partner",
              snippet = status.label,
              icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
            )
          }
          Polyline(
            points = listOf(hubLatLng, destLatLng),
            color = ElectricAzure,
            width = 10f
          )
        }
      }
    }
  }
}
