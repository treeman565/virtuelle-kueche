package de.virtuellekueche.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import de.virtuellekueche.app.ui.navigation.AppRoot
import de.virtuellekueche.app.ui.theme.VirtuelleKuecheTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VirtuelleKuecheTheme {
                // Die Surface sorgt dafür, dass Hintergrund und Schriftfarbe überall
                // aus demselben Farbschema kommen, auch auf dem Anmeldebildschirm.
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppRoot()
                }
            }
        }
    }
}
