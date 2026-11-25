package de.virtuellekueche.app.ui.kitchen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.virtuellekueche.app.data.model.Lagerort
import de.virtuellekueche.app.data.model.Lebensmittel
import de.virtuellekueche.app.domain.Datum
import de.virtuellekueche.app.domain.LagerortArt
import de.virtuellekueche.app.domain.Menge
import de.virtuellekueche.app.ui.theme.KuechenFarben

/** Ab so vielen Tagen vor Ablauf wird ein Produkt rot markiert. */
private const val BALD_ABLAUFEND_TAGE = 2L

@Composable
fun KitchenScreen(vm: KitchenViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    // null = geschlossen, Lebensmittel mit leerer id = neu
    var bearbeiten by remember { mutableStateOf<Lebensmittel?>(null) }
    var neuerLagerort by remember { mutableStateOf(false) }
    var sucheOffen by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        if (state.laedt) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        } else if (state.fehler != null) {
            Text(
                state.fehler!!,
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                color = MaterialTheme.colorScheme.error,
            )
        } else {
            val gewaehlt = state.lagerorte.firstOrNull { it.id == state.filterLagerortId }
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.lagerorte, key = { "ort-" + it.id }) { ort ->
                    LagerortKachel(
                        ort = ort,
                        anzahl = state.anzahl[ort.id] ?: 0,
                        gewaehlt = ort.id == state.filterLagerortId,
                        onClick = { vm.filterSetzen(if (ort.id == state.filterLagerortId) null else ort.id) },
                    )
                }
                item(key = "ort-neu") { NeuKachel(onClick = { neuerLagerort = true }) }

                item(key = "kopf", span = { GridItemSpan(maxLineSpan) }) {
                    ListenKopf(
                        titel = when {
                            state.suche.isNotBlank() -> "Suche: „${state.suche}“"
                            gewaehlt != null -> gewaehlt.name
                            else -> "Bald aufbrauchen"
                        },
                        sucheOffen = sucheOffen || state.suche.isNotBlank(),
                        onSuche = { sucheOffen = true },
                    )
                }
                if (sucheOffen || state.suche.isNotBlank()) {
                    item(key = "suche", span = { GridItemSpan(maxLineSpan) }) {
                        SuchFeld(
                            text = state.suche,
                            onText = vm::sucheAendern,
                            onSchliessen = { vm.sucheAendern(""); sucheOffen = false },
                        )
                    }
                }
                if (state.liste.isEmpty()) {
                    item(key = "leer", span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            when {
                                state.gesamt == 0 -> "Deine Küche ist noch leer.\nScanne einen Bon oder tippe auf +, um etwas einzutragen."
                                state.suche.isNotBlank() -> "Nichts gefunden."
                                else -> "Hier ist gerade nichts drin."
                            },
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        )
                    }
                }
                items(state.liste, key = { it.id }, span = { GridItemSpan(maxLineSpan) }) { lm ->
                    LebensmittelKarte(
                        lm = lm,
                        ort = state.lagerorte.firstOrNull { it.id == lm.lagerortId },
                        zeigeOrt = gewaehlt == null,
                        onClick = { bearbeiten = lm },
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = {
                bearbeiten = Lebensmittel(
                    lagerortId = state.filterLagerortId ?: state.lagerorte.firstOrNull()?.id.orEmpty(),
                    kaufdatum = Datum.heute(),
                )
            },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        ) { Icon(Icons.Outlined.Add, contentDescription = "Lebensmittel hinzufügen") }
    }

    bearbeiten?.let { lm ->
        LebensmittelDialog(
            ausgangswert = lm,
            lagerorte = state.lagerorte,
            onSpeichern = { vm.speichern(it); bearbeiten = null },
            onVerbraucht = { vm.verbraucht(lm); bearbeiten = null },
            onWeggeworfen = { vm.weggeworfen(lm); bearbeiten = null },
            onLoeschen = { vm.loeschen(lm); bearbeiten = null },
            onLagerortAnlegen = vm::lagerortAnlegen,
            onSchliessen = { bearbeiten = null },
        )
    }
    if (neuerLagerort) {
        NeuerLagerortDialog(
            onAnlegen = { vm.lagerortAnlegen(it); neuerLagerort = false },
            onAbbrechen = { neuerLagerort = false },
        )
    }
}

@Composable
private fun LagerortKachel(ort: Lagerort, anzahl: Int, gewaehlt: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (gewaehlt) KuechenFarben.Mint else KuechenFarben.Flaeche,
        contentColor = if (gewaehlt) KuechenFarben.Grund else KuechenFarben.Mint,
        border = if (gewaehlt) null else BorderStroke(1.dp, KuechenFarben.Rand),
        modifier = Modifier
            .aspectRatio(1f)
            .semantics { contentDescription = "${ort.name}, $anzahl Produkte" },
    ) {
        Box(Modifier.fillMaxSize()) {
            Icon(ort.art.symbol(), contentDescription = null, modifier = Modifier.align(Alignment.Center).size(44.dp))
            if (anzahl > 0) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (gewaehlt) KuechenFarben.Grund else KuechenFarben.Mint,
                    contentColor = if (gewaehlt) KuechenFarben.Mint else KuechenFarben.Grund,
                    modifier = Modifier.align(Alignment.TopEnd).padding(7.dp),
                ) {
                    Text(
                        "$anzahl",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(min = 22.dp).padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun NeuKachel(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        contentColor = KuechenFarben.SchriftLeise,
        border = BorderStroke(1.dp, KuechenFarben.Rand),
        modifier = Modifier.aspectRatio(1f),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Add, contentDescription = "Lagerort hinzufügen", modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun ListenKopf(titel: String, sucheOffen: Boolean, onSuche: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
    ) {
        Text(
            titel,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (!sucheOffen) {
            IconButton(onClick = onSuche) {
                Icon(Icons.Outlined.Search, contentDescription = "Suchen", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SuchFeld(text: String, onText: (String) -> Unit, onSchliessen: () -> Unit) {
    OutlinedTextField(
        value = text,
        onValueChange = onText,
        placeholder = { Text("Lebensmittel suchen") },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = onSchliessen) { Icon(Icons.Outlined.Close, contentDescription = "Suche schließen") }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = KuechenFarben.Flaeche,
            focusedContainerColor = KuechenFarben.Flaeche,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun LebensmittelKarte(lm: Lebensmittel, ort: Lagerort?, zeigeOrt: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = KuechenFarben.Flaeche,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(10.dp)) {
            Surface(shape = RoundedCornerShape(11.dp), color = KuechenFarben.FlaecheHoch, contentColor = KuechenFarben.Mint) {
                Icon(
                    (ort?.art ?: LagerortArt.SONSTIGE).symbol(),
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp).size(20.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    lm.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    listOfNotNull("${Menge.format(lm.menge)} ${lm.einheit.kurz}", ort?.name?.takeIf { zeigeOrt }).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            lm.verfallsdatum?.let { AblaufKaestchen(it) }
        }
    }
}

/** Kleines Quadrat mit Zahl und Einheit, rot wenn es bald abläuft. */
@Composable
private fun AblaufKaestchen(verfallsdatum: Long) {
    val bald = Datum.tageBis(verfallsdatum) <= BALD_ABLAUFEND_TAGE
    val (zahl, einheit) = Datum.ablaufKurz(verfallsdatum)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (bald) KuechenFarben.Bald else KuechenFarben.Ok,
        contentColor = if (bald) KuechenFarben.AufBald else KuechenFarben.AufOk,
        modifier = Modifier
            .padding(start = 8.dp)
            .size(46.dp)
            .semantics { contentDescription = Datum.ablaufText(verfallsdatum) },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Text(zahl, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 18.sp)
            Text(einheit, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, lineHeight = 11.sp)
        }
    }
}
