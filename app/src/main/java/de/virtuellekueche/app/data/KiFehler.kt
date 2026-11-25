package de.virtuellekueche.app.data

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import java.io.IOException

/**
 * Übersetzt Fehler der KI-Erkennung in verständliche Hinweise.
 * Der technische Grund wird immer mit angezeigt und ins Logcat geschrieben
 * (Filter „VirtuelleKueche“), damit sich Probleme schnell eingrenzen lassen.
 */
object KiFehler {
    private const val TAG = "VirtuelleKueche"

    fun text(e: Throwable, was: String): String {
        Log.w(TAG, "KI-Erkennung fehlgeschlagen ($was)", e)
        val kette = generateSequence(e) { it.cause }.take(5).toList()
        val alles = kette.joinToString(" | ") { "${it::class.simpleName}: ${it.message}" }
        val klassen = kette.joinToString(" ") { it::class.simpleName.orEmpty() }
        val grund = "\n\nTechnischer Grund: ${alles.take(400)}"

        return when {
            kette.any { it is FirebaseNetworkException || it is IOException } || "ServiceConnection" in klassen ->
                "Keine Verbindung zur KI. Prüf dein Internet und versuch es noch einmal."
            "ServiceDisabled" in klassen || listOf("has not been used", "is disabled", "SERVICE_DISABLED", "not enabled").any { it in alles } ->
                "Die KI ist in Firebase noch nicht freigeschaltet. Öffne in der Firebase-Konsole „AI Logic“, wähl „Gemini Developer API“ und aktiviere sie. Danach ein paar Minuten warten.$grund"
            "InvalidAPIKey" in klassen || "API key" in alles || "API_KEY" in alles ->
                "Firebase lehnt den Zugang ab. Wahrscheinlich passt die google-services.json nicht zum Projekt oder der Schlüssel ist eingeschränkt.$grund"
            "Quota" in klassen || "quota" in alles.lowercase() || "429" in alles ->
                "Das kostenlose Kontingent der KI ist gerade aufgebraucht. Versuch es in einer Minute noch einmal.$grund"
            "PERMISSION_DENIED" in alles || "403" in alles ->
                "Firebase verweigert den Zugriff auf die KI. Prüf, ob AI Logic mit der Gemini Developer API aktiviert ist.$grund"
            e is IllegalArgumentException || "Serialization" in klassen ->
                "Die KI hat keine brauchbare Antwort geliefert. Versuch es mit einem schärferen, gut beleuchteten Foto.$grund"
            else -> "Das hat nicht geklappt.$grund"
        }
    }
}
