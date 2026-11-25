package de.virtuellekueche.app.ui.kitchen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AcUnit
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.Cookie
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.ui.graphics.vector.ImageVector
import de.virtuellekueche.app.domain.LagerortArt

/** Großes, klares Symbol je Lagerort für die Kacheln. */
fun LagerortArt.symbol(): ImageVector = when (this) {
    LagerortArt.KUEHLSCHRANK -> Icons.Outlined.Kitchen
    LagerortArt.TIEFKUEHLER -> Icons.Outlined.AcUnit
    LagerortArt.VORRATSSCHRANK -> Icons.Outlined.Inventory2
    LagerortArt.SUESSIGKEITEN -> Icons.Outlined.Cookie
    LagerortArt.HAUSHALT -> Icons.Outlined.CleaningServices
    LagerortArt.SONSTIGE -> Icons.Outlined.Category
}
