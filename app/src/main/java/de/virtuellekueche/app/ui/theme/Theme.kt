package de.virtuellekueche.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/*
 * Eigenes, festes Farbschema statt der dynamischen Android-Farben:
 * Die dynamischen Farben färben die Schrift je nach Hintergrundbild leicht ein,
 * dadurch wirkte sie nicht richtig schwarz.
 */

private val Gruen = Color(0xFF2E6B30)
private val Schwarz = Color(0xFF111111)
private val Grau = Color(0xFF444444)

private val LightColors = lightColorScheme(
    primary = Gruen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8EEC2),
    onPrimaryContainer = Color(0xFF00210A),
    secondaryContainer = Color(0xFFDDE6D8),
    onSecondaryContainer = Schwarz,
    background = Color.White,
    onBackground = Schwarz,
    surface = Color.White,
    onSurface = Schwarz,
    surfaceVariant = Color(0xFFEEF1EA),
    onSurfaceVariant = Grau,
    surfaceContainer = Color(0xFFF3F5F0),
    surfaceContainerHigh = Color(0xFFEDEFEA),
    surfaceContainerHighest = Color(0xFFE7E9E4),
    outline = Color(0xFF757575),
    error = Color(0xFFB3261E),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF9FD69A),
    onPrimary = Color(0xFF003910),
    primaryContainer = Color(0xFF16521B),
    onPrimaryContainer = Color(0xFFC8EEC2),
    background = Color(0xFF121212),
    onBackground = Color(0xFFF2F2F2),
    surface = Color(0xFF121212),
    onSurface = Color(0xFFF2F2F2),
    onSurfaceVariant = Color(0xFFC7C7C7),
)

@Composable
fun VirtuelleKuecheTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
}
