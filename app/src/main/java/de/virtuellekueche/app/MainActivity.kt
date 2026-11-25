package de.virtuellekueche.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import de.virtuellekueche.app.ui.navigation.AppRoot
import de.virtuellekueche.app.ui.theme.VirtuelleKuecheTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VirtuelleKuecheTheme {
                AppRoot()
            }
        }
    }
}
