package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
  primary = ElectricAzure,
  onPrimary = PureWhite,
  primaryContainer = AzureContainer,
  onPrimaryContainer = OnAzureContainer,
  secondary = SunburstGold,
  onSecondary = InkDark,
  secondaryContainer = GoldContainer,
  onSecondaryContainer = OnGoldContainer,
  tertiary = DealGreen,
  onTertiary = PureWhite,
  tertiaryContainer = DealGreenContainer,
  onTertiaryContainer = Color(0xFF0A3D17),
  background = MistBackground,
  onBackground = InkDark,
  surface = PureWhite,
  onSurface = InkDark,
  surfaceVariant = Color(0xFFE9EEF7),
  onSurfaceVariant = InkMuted,
  outline = SoftOutline,
  error = CoralDiscount,
  onError = PureWhite,
  errorContainer = CoralContainer,
  onErrorContainer = Color(0xFF690005)
)

private val DarkColorScheme = darkColorScheme(
  primary = LightAzure,
  onPrimary = Color(0xFF002B6C),
  primaryContainer = DeepRoyalBlue,
  onPrimaryContainer = AzureContainer,
  secondary = SunburstGold,
  onSecondary = InkDark,
  secondaryContainer = Color(0xFF5A3700),
  onSecondaryContainer = GoldContainer,
  tertiary = Color(0xFF56D364),
  onTertiary = Color(0xFF003913),
  tertiaryContainer = Color(0xFF0F5123),
  onTertiaryContainer = DealGreenContainer,
  background = DarkBg,
  onBackground = PureWhite,
  surface = DarkSurface,
  onSurface = PureWhite,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = Color(0xFFB3C0D4),
  outline = DarkOutline,
  error = Color(0xFFFF8A80),
  onError = Color(0xFF690005),
  errorContainer = Color(0xFF93000A),
  onErrorContainer = CoralContainer
)

private val QuickKartShapes = Shapes(
  small = RoundedCornerShape(8.dp),
  medium = RoundedCornerShape(14.dp),
  large = RoundedCornerShape(20.dp),
  extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun QuickKartTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = QuickKartTypography,
    shapes = QuickKartShapes,
    content = content
  )
}
