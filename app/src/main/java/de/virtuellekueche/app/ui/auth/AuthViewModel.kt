package de.virtuellekueche.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import de.virtuellekueche.app.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SessionState {
    data object Loading : SessionState
    data object SignedOut : SessionState
    data class SignedIn(val user: FirebaseUser) : SessionState
}

data class LoginUiState(
    val busy: Boolean = false,
    val error: String? = null,
    val info: String? = null,
)

class AuthViewModel(
    private val repo: AuthRepository = AuthRepository(),
) : ViewModel() {

    val session: StateFlow<SessionState> = repo.currentUser
        .map { user -> if (user == null) SessionState.SignedOut else SessionState.SignedIn(user) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)

    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui.asStateFlow()

    fun signIn(email: String, password: String) = run { repo.signIn(email, password) }

    fun register(email: String, password: String) = run { repo.register(email, password) }

    fun resetPassword(email: String) = run {
        repo.resetPassword(email)
        _ui.value = _ui.value.copy(info = "Wir haben dir eine E-Mail zum Zurücksetzen geschickt.")
    }

    fun signOut() = repo.signOut()

    private fun run(block: suspend () -> Unit) {
        if (_ui.value.busy) return
        _ui.value = LoginUiState(busy = true)
        viewModelScope.launch {
            try {
                block()
                _ui.value = _ui.value.copy(busy = false)
            } catch (e: Exception) {
                _ui.value = LoginUiState(error = fehlertext(e))
            }
        }
    }

    private fun fehlertext(e: Exception): String = when (e) {
        is FirebaseAuthWeakPasswordException -> "Das Passwort muss mindestens 6 Zeichen haben."
        is FirebaseAuthUserCollisionException -> "Für diese E-Mail gibt es schon ein Konto."
        is FirebaseAuthInvalidCredentialsException -> "E-Mail oder Passwort stimmt nicht."
        is FirebaseNetworkException -> "Keine Internetverbindung. Die Anmeldung braucht einmalig Netz."
        is IllegalArgumentException -> "Bitte E-Mail und Passwort eingeben."
        else -> "Das hat nicht geklappt. Bitte versuch es noch einmal."
    }
}
