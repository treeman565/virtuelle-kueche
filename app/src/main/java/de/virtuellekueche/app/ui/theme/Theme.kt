package de.virtuellekueche.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import de.virtuellekueche.app.R

/*
 * Design D "Kacheln am Abend": tiefes Waldgrün als Grund, Mint als Akzent,
 * DM Sans als Schrift. Bewusst immer dunkel, unabhängig vom Systemmodus.
 */
object KuechenFarben {
    val Grund = Color(0xFF0F2A22)
    val Flaeche = Color(0xFF163A2F)
    val FlaecheHoch = Color(0xFF1E4A3C)
    val Rand = Color(0xFF21503F)
    val Leiste = Color(0xFF0B211A)
    val Mint = Color(0xFF7FD1AE)
    val Schrift = Color(0xFFEEF6F1)
    val SchriftLeise = Color(0xFF9FC0B1)
    val Bald = Color(0xFFF2B8A6)
    val AufBald = Color(0xFF5A1D0F)
    val Ok = Color(0xFF24503F)
    val AufOk = Color(0xFFBFE9D6)
}

private val Farben = darkColorScheme(
    primary = KuechenFarben.Mint,
    onPrimary = KuechenFarben.Grund,
    primaryContainer = KuechenFarben.FlaecheHoch,
    onPrimaryContainer = KuechenFarben.Mint,
    secondary = KuechenFarben.Mint,
    onSecondary = KuechenFarben.Grund,
    secondaryContainer = KuechenFarben.FlaecheHoch,
    onSecondaryContainer = KuechenFarben.Schrift,
    tertiary = KuechenFarben.Bald,
    background = KuechenFarben.Grund,
    onBackground = KuechenFarben.Schrift,
    surface = KuechenFarben.Grund,
    onSurface = KuechenFarben.Schrift,
    surfaceVariant = KuechenFarben.Flaeche,
    onSurfaceVariant = KuechenFarben.SchriftLeise,
    surfaceContainerLowest = KuechenFarben.Leiste,
    surfaceContainerLow = KuechenFarben.Flaeche,
    surfaceContainer = KuechenFarben.Flaeche,
    surfaceContainerHigh = KuechenFarben.FlaecheHoch,
    surfaceContainerHighest = KuechenFarben.FlaecheHoch,
    outline = KuechenFarben.Rand,
    outlineVariant = KuechenFarben.Rand,
    error = KuechenFarben.Bald,
    onError = KuechenFarben.AufBald,
    errorContainer = Color(0xFF5A2A22),
    onErrorContainer = KuechenFarben.Bald,
)

@OptIn(ExperimentalTextApi::class)
private fun dmSans(gewicht: Int) = Font(
    R.font.dm_sans,
    weight = FontWeight(gewicht),
    variationSettings = FontVariation.Settings(FontVariation.weight(gewicht)),
)

val DmSans = FontFamily(dmSans(400), dmSans(500), dmSans(600), dmSans(700), dmSans(800))

private val Basis = Typography()

private fun TextStyle.dm() = copy(fontFamily = DmSans)

private val Schrift = Typography(
    displayLarge = Basis.displayLarge.dm(),
    displayMedium = Basis.displayMedium.dm(),
    displaySmall = Basis.displaySmall.dm(),
    headlineLarge = Basis.headlineLarge.dm().copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em),
    headlineMedium = Basis.headlineMedium.dm().copy(fontWeight = FontWeight.Bold, fontSize = 28.sp, letterSpacing = (-0.02).em),
    headlineSmall = Basis.headlineSmall.dm().copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.01).em),
    titleLarge = Basis.titleLarge.dm().copy(fontWeight = FontWeight.Bold),
    titleMedium = Basis.titleMedium.dm().copy(fontWeight = FontWeight.SemiBold),
    titleSmall = Basis.titleSmall.dm().copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = Basis.bodyLarge.dm(),
    bodyMedium = Basis.bodyMedium.dm(),
    bodySmall = Basis.bodySmall.dm(),
    labelLarge = Basis.labelLarge.dm().copy(fontWeight = FontWeight.SemiBold),
    labelMedium = Basis.labelMedium.dm(),
    labelSmall = Basis.labelSmall.dm(),
)

@Composable
fun VirtuelleKuecheTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Farben, typography = Schrift, content = content)
}
