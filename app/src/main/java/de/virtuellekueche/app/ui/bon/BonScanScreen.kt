package de.virtuellekueche.app.ui.bon

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.virtuellekueche.app.data.Fotos
import de.virtuellekueche.app.data.model.Lagerort
import de.virtuellekueche.app.domain.Menge
import de.virtuellekueche.app.domain.Preis
import de.virtuellekueche.app.ui.kitchen.DatumFeld
import de.virtuellekueche.app.ui.kitchen.LebensmittelDialog
import kotlinx.coroutines.launch

@Composable
fun BonScanScreen(onFertig: () -> Unit, vm: BonViewModel = viewModel()) {
    val zustand by vm.zustand.collectAsStateWithLifecycle()
    val lagerorte by vm.lagerorte.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var fotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    fun laden(uri: Uri) = scope.launch {
        runCatching { Fotos.laden(context, uri) }
            .onSuccess(vm::fotoErkennen)
            .onFailure { vm.fotoFehler() }
    }

    val kamera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val uri = fotoUri
        if (ok && uri != null) laden(uri)
    }
    val galerie = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) laden(uri)
    }
    val fotoAufnehmen = {
        val uri = Fotos.neueFotoUri(context)
        fotoUri = uri
        kamera.launch(uri)
    }
    val ausGalerie = {
        galerie.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    when (val z = zustand) {
        BonZustand.Start -> StartAnsicht(onKamera = fotoAufnehmen, onGalerie = ausGalerie)
        is BonZustand.Erkennen -> ErkennenAnsicht(z)
        is BonZustand.Pruefen -> PruefAnsicht(z, lagerorte, vm)
        is BonZustand.Fehler -> Mitteilung(
            text = z.text,
            knopf = "Neues Foto aufnehmen",
            onKnopf = fotoAufnehmen,
            zweiterKnopf = "Abbrechen",
            onZweiterKnopf = onFertig,
        )
        is BonZustand.Fertig -> Mitteilung(
            text = if (z.anzahl == 1) "1 Produkt ist jetzt in deiner Küche." else "${z.anzahl} Produkte sind jetzt in deiner Küche.",
            knopf = "Zur Küche",
            onKnopf = onFertig,
            zweiterKnopf = "Noch einen Bon scannen",
            onZweiterKnopf = vm::neuStarten,
        )
    }
}

@Composable
private fun StartAnsicht(onKamera: () -> Unit, onGalerie: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text("Kassenbon scannen", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Leg den Bon flach hin und fotografiere ihn vollständig und gut beleuchtet. Bei langen Bons reicht es, wenn alle Produktzeilen zu sehen sind.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onKamera, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.PhotoCamera, contentDescription = null)
            Spacer(Modifier.padding(4.dp))
            Text("Foto aufnehmen")
        }
        OutlinedButton(onClick = onGalerie, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.PhotoLibrary, contentDescription = null)
            Spacer(Modifier.padding(4.dp))
            Text("Foto aus der Galerie")
        }
    }
}

@Composable
private fun ErkennenAnsicht(z: BonZustand.Erkennen) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            bitmap = z.foto.asImageBitmap(),
            contentDescription = "Foto des Kassenbons",
            contentScale = ContentScale.Fit,
            modifier = Modifier.heightIn(max = 320.dp),
        )
        CircularProgressIndicator()
        Text("Der Bon wird gelesen …", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun PruefAnsicht(z: BonZustand.Pruefen, lagerorte: List<Lagerort>, vm: BonViewModel) {
    var bearbeiten by remember { mutableStateOf<Int?>(null) }
    val anzahl = z.entwuerfe.count { it.uebernehmen }

    Column(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 16.dp)) {
            item {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Bitte prüfen", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Tippe ein Produkt an, um es zu korrigieren. Mit dem Häkchen legst du fest, was in die Küche kommt.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = z.supermarkt,
                        onValueChange = vm::supermarktAendern,
                        label = { Text("Geschäft") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    DatumFeld(label = "Einkaufsdatum", wert = z.datum, onWahl = vm::datumAendern)
                    z.summeCent?.let { Text("Summe laut Bon: ${Preis.format(it)}", style = MaterialTheme.typography.bodyMedium) }
                }
                HorizontalDivider()
            }
            itemsIndexed(z.entwuerfe) { i, e ->
                val lm = e.lebensmittel
                val ort = lagerorte.firstOrNull { it.id == lm.lagerortId }?.name
                ListItem(
                    leadingContent = { Checkbox(checked = e.uebernehmen, onCheckedChange = { vm.umschalten(i) }) },
                    headlineContent = {
                        Text(
                            lm.name,
                            textDecoration = if (e.uebernehmen) null else TextDecoration.LineThrough,
                            color = if (e.uebernehmen) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    supportingContent = {
                        Text(listOfNotNull("${Menge.format(lm.menge)} ${lm.einheit.kurz}", ort).joinToString(" · "))
                    },
                    trailingContent = { lm.preisCent?.let { Text(Preis.format(it)) } },
                    modifier = Modifier.clickable { bearbeiten = i },
                )
                HorizontalDivider()
            }
        }
        Surface(tonalElevation = 3.dp) {
            Button(
                onClick = vm::uebernehmen,
                enabled = anzahl > 0,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) {
                Text(if (anzahl == 1) "1 Produkt in die Küche übernehmen" else "$anzahl Produkte in die Küche übernehmen")
            }
        }
    }

    bearbeiten?.let { i ->
        LebensmittelDialog(
            ausgangswert = z.entwuerfe[i].lebensmittel,
            lagerorte = lagerorte,
            onSpeichern = { vm.entwurfAendern(i, it); bearbeiten = null },
            onVerbraucht = {},
            onWeggeworfen = {},
            onLoeschen = {},
            onLagerortAnlegen = vm::lagerortAnlegen,
            onSchliessen = { bearbeiten = null },
        )
    }
}

@Composable
private fun Mitteilung(
    text: String,
    knopf: String,
    onKnopf: () -> Unit,
    zweiterKnopf: String,
    onZweiterKnopf: () -> Unit,
) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onKnopf, modifier = Modifier.fillMaxWidth()) { Text(knopf) }
            OutlinedButton(onClick = onZweiterKnopf, modifier = Modifier.fillMaxWidth()) { Text(zweiterKnopf) }
        }
    }
}
