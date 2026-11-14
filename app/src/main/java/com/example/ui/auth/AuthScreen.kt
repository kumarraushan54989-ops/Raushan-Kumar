package com.example.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) {
    attemptAutoSignIn(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = onAuthSuccess,
      onUnauthenticated = {},
      scope = scope
    )
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer
          )
        )
      )
      .systemBarsPadding(),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 520.dp)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.secondary,
          modifier = Modifier.size(64.dp),
          shadowElevation = 6.dp
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Filled.ShoppingBag,
              contentDescription = "QuickKart logo",
              tint = MaterialTheme.colorScheme.onSecondary,
              modifier = Modifier.size(34.dp)
            )
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = stringResource(id = com.example.R.string.app_name),
          style = MaterialTheme.typography.displaySmall,
          color = MaterialTheme.colorScheme.onPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Mega Deals, 1-Tap Ordering & Live Maps Delivery",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
          textAlign = TextAlign.Center
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Image(
            painter = painterResource(id = com.example.R.drawable.img_hero_big_billion),
            contentDescription = "QuickKart featured electronics and deals banner",
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .fillMaxWidth()
              .height(150.dp)
              .clip(RoundedCornerShape(16.dp))
          )
          Spacer(modifier = Modifier.height(18.dp))
          Text(
            text = "Everything You Need, Delivered Fast",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(12.dp))

          AuthFeatureRow(
            icon = Icons.Filled.Bolt,
            title = "Flash Deals & Super Coupons",
            subtitle = "Instant cart sync, promo codes & 1-tap Buy Now"
          )
          Spacer(modifier = Modifier.height(10.dp))
          AuthFeatureRow(
            icon = Icons.Filled.Map,
            title = "Google Maps Pin & Live Tracking",
            subtitle = "Pin your doorstep & watch orders arrive in real time"
          )
          Spacer(modifier = Modifier.height(10.dp))
          AuthFeatureRow(
            icon = Icons.Filled.Storefront,
            title = "Seller Desk Included",
            subtitle = "List store items & manage customer order stages"
          )

          if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
              color = MaterialTheme.colorScheme.errorContainer,
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(12.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          Button(
            onClick = {
              errorMessage = null
              isLoading = true
              onGoogleSignInClicked(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {
                  isLoading = false
                  onAuthSuccess()
                },
                onAuthError = { msg ->
                  isLoading = false
                  errorMessage = msg
                },
                scope = scope,
                onAuthCancelled = {
                  isLoading = false
                }
              )
            },
            enabled = !isLoading,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
              .fillMaxWidth()
              .height(54.dp)
              .testTag("google_sign_in_button")
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.5.dp
              )
            } else {
              Icon(
                imageVector = Icons.Filled.LocalShipping,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.size(10.dp))
              Text(
                text = "Sign in with Google",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          Surface(
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Your QuickKart buyer & seller ID is created automatically with Google Sign-In. On an emulator, tap \"SIGN IN ANOTHER WAY\" if Google cannot check a phone number.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.padding(12.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
      Text(
        text = "Secured by Google Sign-In & Cloud Firestore",
        style = MaterialTheme.typography.labelMedium,
        color = Color.White.copy(alpha = 0.85f)
      )
    }
  }
}

@Composable
private fun AuthFeatureRow(
  icon: ImageVector,
  title: String,
  subtitle: String
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth()
  ) {
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = MaterialTheme.colorScheme.primaryContainer,
      modifier = Modifier.size(40.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(22.dp)
        )
      }
    }
    Spacer(modifier = Modifier.size(12.dp))
    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
