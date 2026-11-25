package de.virtuellekueche.app.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    state: LoginUiState,
    onSignIn: (String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    onResetPassword: (String) -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var password2 by rememberSaveable { mutableStateOf("") }
    var registerMode by rememberSaveable { mutableStateOf(false) }
    var passwortSichtbar by rememberSaveable { mutableStateOf(false) }
    val focus = LocalFocusManager.current

    val passwoerterUngleich = registerMode && password2.isNotEmpty() && password != password2
    val kannAbsenden = email.isNotBlank() && password.isNotBlank() &&
        (!registerMode || (password2.isNotEmpty() && password == password2))
    val absenden = {
        if (kannAbsenden) {
            focus.clearFocus()
            if (registerMode) onRegister(email, password) else onSignIn(email, password)
        }
    }
    val passwortTransformation =
        if (passwortSichtbar) VisualTransformation.None else PasswordVisualTransformation()
    val sichtbarKnopf: @Composable () -> Unit = {
        IconButton(onClick = { passwortSichtbar = !passwortSichtbar }) {
            Icon(
                if (passwortSichtbar) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                contentDescription = if (passwortSichtbar) "Passwort verbergen" else "Passwort anzeigen",
            )
        }
    }

    // imePadding + verticalScroll: Wenn die Tastatur aufgeht, schrumpft der Bereich
    // und das aktive Eingabefeld wird automatisch sichtbar gescrollt.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))
        Text("Virtuelle Küche", style = MaterialTheme.typography.headlineMedium)
        Text(
            if (registerMode) "Neues Konto anlegen" else "Anmelden",
            style = MaterialTheme.typography.titleMedium,
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-Mail") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Passwort") },
            singleLine = true,
            visualTransformation = passwortTransformation,
            trailingIcon = sichtbarKnopf,
            supportingText = { if (registerMode) Text("Mindestens 6 Zeichen") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = if (registerMode) ImeAction.Next else ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { absenden() }),
            modifier = Modifier.fillMaxWidth(),
        )
        if (registerMode) {
            OutlinedTextField(
                value = password2,
                onValueChange = { password2 = it },
                label = { Text("Passwort wiederholen") },
                singleLine = true,
                visualTransformation = passwortTransformation,
                isError = passwoerterUngleich,
                supportingText = { if (passwoerterUngleich) Text("Die Passwörter stimmen nicht überein.") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { absenden() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        state.info?.let { Text(it, color = MaterialTheme.colorScheme.primary) }

        if (state.busy) {
            CircularProgressIndicator()
        } else {
            Button(onClick = absenden, enabled = kannAbsenden, modifier = Modifier.fillMaxWidth()) {
                Text(if (registerMode) "Konto anlegen" else "Anmelden")
            }
        }
        TextButton(onClick = { registerMode = !registerMode; password2 = "" }) {
            Text(if (registerMode) "Ich habe schon ein Konto" else "Noch kein Konto? Registrieren")
        }
        if (!registerMode) {
            TextButton(onClick = { onResetPassword(email) }, enabled = email.isNotBlank()) {
                Text("Passwort vergessen")
            }
        }
    }
}
