package de.virtuellekueche.app.ui.kitchen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.virtuellekueche.app.data.model.Einheit
import de.virtuellekueche.app.data.model.Lagerort
import de.virtuellekueche.app.data.model.Lebensmittel
import de.virtuellekueche.app.domain.Datum
import de.virtuellekueche.app.domain.Haltbarkeit
import de.virtuellekueche.app.domain.LagerortArt
import de.virtuellekueche.app.domain.Menge
import de.virtuellekueche.app.domain.Preis

/** Neues Lebensmittel anlegen oder ein vorhandenes bearbeiten. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LebensmittelDialog(
    ausgangswert: Lebensmittel,
    lagerorte: List<Lagerort>,
    onSpeichern: (Lebensmittel) -> Unit,
    onVerbraucht: () -> Unit,
    onWeggeworfen: () -> Unit,
    onLoeschen: () -> Unit,
    onLagerortAnlegen: (String) -> Unit,
    onSchliessen: () -> Unit,
) {
    val neu = ausgangswert.id.isEmpty()
    var name by remember { mutableStateOf(ausgangswert.name) }
    var menge by remember { mutableStateOf(Menge.format(ausgangswert.menge)) }
    var einheit by remember { mutableStateOf(ausgangswert.einheit) }
    var lagerortId by remember { mutableStateOf(ausgangswert.lagerortId) }
    var kaufdatum by remember { mutableStateOf(ausgangswert.kaufdatum.takeIf { it > 0 } ?: Datum.heute()) }
    var preis by remember { mutableStateOf(ausgangswert.preisCent?.let(Preis::formatEingabe).orEmpty()) }
    var verfallManuell by remember { mutableStateOf(!neu && !ausgangswert.verfallsdatumGeschaetzt) }
    var verfallManuellWert by remember { mutableStateOf(ausgangswert.verfallsdatum) }
    var neuerLagerortDialog by remember { mutableStateOf(false) }

    val art = lagerorte.firstOrNull { it.id == lagerortId }?.art ?: LagerortArt.SONSTIGE
    // Solange das Datum nicht von Hand gesetzt wurde, folgt die Schätzung Name und Lagerort.
    val verfallsdatum: Long? = if (verfallManuell) verfallManuellWert
    else if (name.isBlank()) null
    else Haltbarkeit.schaetzeVerfallsdatum(name, art, kaufdatum)

    val mengeWert = Menge.parse(menge)
    val preisWert = Preis.parseCent(preis)
    val preisFehler = preis.isNotBlank() && preisWert == null
    val gueltig = name.isNotBlank() && mengeWert != null && mengeWert > 0 && lagerortId.isNotEmpty() && !preisFehler

    Dialog(onDismissRequest = onSchliessen, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (neu) "Lebensmittel hinzufügen" else "Lebensmittel bearbeiten") },
                    navigationIcon = {
                        IconButton(onClick = onSchliessen) { Icon(Icons.Outlined.Close, contentDescription = "Schließen") }
                    },
                    actions = {
                        TextButton(
                            enabled = gueltig,
                            onClick = {
                                onSpeichern(
                                    ausgangswert.copy(
                                        name = name.trim(),
                                        menge = mengeWert ?: 1.0,
                                        einheit = einheit,
                                        lagerortId = lagerortId,
                                        kaufdatum = kaufdatum,
                                        preisCent = preisWert,
                                        verfallsdatum = verfallsdatum,
                                        verfallsdatumGeschaetzt = !verfallManuell,
                                    ),
                                )
                            },
                        ) { Text("Speichern") }
                    },
                )
            },
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = menge,
                        onValueChange = { menge = it },
                        label = { Text("Menge") },
                        singleLine = true,
                        isError = mengeWert == null || mengeWert <= 0,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                    Auswahl(
                        label = "Einheit",
                        wert = einheit.lang,
                        optionen = Einheit.entries.map { it.lang to { einheit = it } },
                        modifier = Modifier.weight(1f),
                    )
                }
                Auswahl(
                    label = "Lagerort",
                    wert = lagerorte.firstOrNull { it.id == lagerortId }?.name.orEmpty(),
                    optionen = lagerorte.map { ort -> ort.name to { lagerortId = ort.id } } +
                        ("+ Neuen Lagerort anlegen" to { neuerLagerortDialog = true }),
                    modifier = Modifier.fillMaxWidth(),
                )
                DatumFeld(label = "Kaufdatum", wert = kaufdatum, onWahl = { kaufdatum = it })
                OutlinedTextField(
                    value = preis,
                    onValueChange = { preis = it },
                    label = { Text("Preis (optional)") },
                    suffix = { Text("€") },
                    singleLine = true,
                    isError = preisFehler,
                    supportingText = { if (preisFehler) Text("Bitte so eingeben: 1,99") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
                DatumFeld(
                    label = "Verfallsdatum",
                    wert = verfallsdatum,
                    hinweis = when {
                        verfallsdatum == null && art == LagerortArt.HAUSHALT -> "Für Haushaltsartikel nicht nötig. Tippe, um trotzdem eins festzulegen."
                        verfallsdatum == null -> "Wird geschätzt, sobald ein Name eingetragen ist."
                        verfallManuell -> "Von dir festgelegt."
                        else -> "Geschätzt. Tippe, um es zu ändern."
                    },
                    onWahl = { verfallManuell = true; verfallManuellWert = it },
                )
                if (verfallManuell) {
                    TextButton(onClick = { verfallManuell = false }) { Text("Wieder automatisch schätzen") }
                }

                if (!neu) {
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    Text("Aus der Küche nehmen", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onVerbraucht, modifier = Modifier.weight(1f)) { Text("Verbraucht") }
                        OutlinedButton(onClick = onWeggeworfen, modifier = Modifier.weight(1f)) { Text("Weggeworfen") }
                    }
                    TextButton(onClick = onLoeschen) {
                        Text("Falsch eingetragen, löschen", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    if (neuerLagerortDialog) {
        NeuerLagerortDialog(
            onAnlegen = { onLagerortAnlegen(it); neuerLagerortDialog = false },
            onAbbrechen = { neuerLagerortDialog = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Auswahl(
    label: String,
    wert: String,
    optionen: List<Pair<String, () -> Unit>>,
    modifier: Modifier = Modifier,
) {
    var offen by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = offen, onExpandedChange = { offen = it }, modifier = modifier) {
        OutlinedTextField(
            value = wert,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = offen) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = offen, onDismissRequest = { offen = false }) {
            optionen.forEach { (text, aktion) ->
                DropdownMenuItem(text = { Text(text) }, onClick = { aktion(); offen = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DatumFeld(label: String, wert: Long?, onWahl: (Long) -> Unit, hinweis: String? = null) {
    var offen by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = wert?.let(Datum::format).orEmpty(),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        supportingText = { hinweis?.let { Text(it) } },
        trailingIcon = {
            IconButton(onClick = { offen = true }) {
                Icon(Icons.Outlined.CalendarMonth, contentDescription = "$label wählen")
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
    if (offen) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = wert ?: Datum.heute())
        DatePickerDialog(
            onDismissRequest = { offen = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let(onWahl)
                    offen = false
                }) { Text("Übernehmen") }
            },
            dismissButton = { TextButton(onClick = { offen = false }) { Text("Abbrechen") } },
        ) { DatePicker(state = pickerState) }
    }
}

@Composable
internal fun NeuerLagerortDialog(onAnlegen: (String) -> Unit, onAbbrechen: () -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onAbbrechen,
        title = { Text("Neuer Lagerort") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name, z. B. Keller") },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onAnlegen(name) }, enabled = name.isNotBlank()) { Text("Anlegen") }
        },
        dismissButton = { TextButton(onClick = onAbbrechen) { Text("Abbrechen") } },
    )
}
