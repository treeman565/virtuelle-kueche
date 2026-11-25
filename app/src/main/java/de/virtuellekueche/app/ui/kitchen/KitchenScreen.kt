package de.virtuellekueche.app.ui.kitchen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.virtuellekueche.app.data.model.Lebensmittel
import de.virtuellekueche.app.domain.Datum
import de.virtuellekueche.app.domain.Menge

/** Ab so vielen Tagen vor Ablauf wird ein Produkt hervorgehoben. */
private const val BALD_ABLAUFEND_TAGE = 2L

@Composable
fun KitchenScreen(vm: KitchenViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    // null = geschlossen, Lebensmittel mit leerer id = neu
    var bearbeiten by remember { mutableStateOf<Lebensmittel?>(null) }

    Box(Modifier.fillMaxSize()) {
        when {
            state.laedt -> CircularProgressIndicator(Modifier.align(Alignment.Center))
            state.fehler != null -> Text(
                state.fehler!!,
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                color = MaterialTheme.colorScheme.error,
            )
            else -> VorratListe(
                state = state,
                onSuche = vm::sucheAendern,
                onFilter = vm::filterSetzen,
                onAntippen = { bearbeiten = it },
            )
        }
        ExtendedFloatingActionButton(
            onClick = {
                bearbeiten = Lebensmittel(
                    lagerortId = state.filterLagerortId ?: state.lagerorte.firstOrNull()?.id.orEmpty(),
                    kaufdatum = Datum.heute(),
                )
            },
            icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
            text = { Text("Hinzufügen") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VorratListe(
    state: KitchenUiState,
    onSuche: (String) -> Unit,
    onFilter: (String?) -> Unit,
    onAntippen: (Lebensmittel) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = state.suche,
            onValueChange = onSuche,
            placeholder = { Text("Lebensmittel suchen") },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        )
        Row(
            Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = state.filterLagerortId == null,
                onClick = { onFilter(null) },
                label = { Text("Alle") },
            )
            state.lagerorte.forEach { ort ->
                FilterChip(
                    selected = state.filterLagerortId == ort.id,
                    onClick = { onFilter(if (state.filterLagerortId == ort.id) null else ort.id) },
                    label = { Text(ort.name) },
                )
            }
        }

        val leer = state.gruppen.all { it.lebensmittel.isEmpty() }
        if (leer) {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(
                    if (state.suche.isBlank()) "Deine Küche ist noch leer.\nTippe auf „Hinzufügen“, um ein Lebensmittel einzutragen."
                    else "Nichts gefunden für „${state.suche}“.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            return
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
            state.gruppen.forEach { gruppe ->
                stickyHeader(key = "kopf-${gruppe.lagerort.id}") {
                    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "${gruppe.lagerort.name} (${gruppe.lebensmittel.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }
                }
                if (gruppe.lebensmittel.isEmpty()) {
                    item(key = "leer-${gruppe.lagerort.id}") {
                        Text(
                            "Noch nichts hier.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }
                items(gruppe.lebensmittel, key = { it.id }) { lm ->
                    LebensmittelZeile(lm, onClick = { onAntippen(lm) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun LebensmittelZeile(lm: Lebensmittel, onClick: () -> Unit) {
    val tage = lm.verfallsdatum?.let { Datum.tageBis(it) }
    val bald = tage != null && tage <= BALD_ABLAUFEND_TAGE
    ListItem(
        headlineContent = { Text(lm.name) },
        supportingContent = { Text("${Menge.format(lm.menge)} ${lm.einheit.kurz}") },
        trailingContent = {
            lm.verfallsdatum?.let {
                Text(
                    Datum.ablaufText(it),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (bald) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        colors = if (bald) {
            ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f))
        } else {
            ListItemDefaults.colors()
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}
