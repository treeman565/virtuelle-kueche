package de.virtuellekueche.app.data

import android.graphics.Bitmap
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import de.virtuellekueche.app.domain.BonErgebnis
import de.virtuellekueche.app.domain.BonParser
import de.virtuellekueche.app.domain.BonPrompt
import de.virtuellekueche.app.domain.ProduktErgebnis
import de.virtuellekueche.app.domain.ProduktParser
import de.virtuellekueche.app.domain.ProduktPrompt

/** Liest einen Kassenbon von einem Foto. */
interface BonErkennung {
    suspend fun erkennen(foto: Bitmap): BonErgebnis
}

/**
 * Gemini über Firebase AI Logic. Der Zugang läuft über das Firebase-Projekt,
 * es liegt also kein API-Schlüssel in der App.
 */
class GeminiBonErkennung : BonErkennung {
    override suspend fun erkennen(foto: Bitmap): BonErgebnis =
        BonParser.parse(Gemini.fotoFragen(foto, BonPrompt.text))
}

/** Erkennt ein einzelnes Produkt auf einem Foto (Name, Menge, MHD). */
interface ProduktErkennung {
    suspend fun erkennen(foto: Bitmap): ProduktErgebnis
}

class GeminiProduktErkennung : ProduktErkennung {
    override suspend fun erkennen(foto: Bitmap): ProduktErgebnis =
        ProduktParser.parse(Gemini.fotoFragen(foto, ProduktPrompt.text))
}

private object Gemini {
    private val modell by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = "gemini-2.5-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                temperature = 0f
            },
        )
    }

    suspend fun fotoFragen(foto: Bitmap, anweisung: String): String {
        val antwort = modell.generateContent(
            content {
                image(foto)
                text(anweisung)
            },
        )
        return antwort.text ?: throw IllegalArgumentException("Leere Antwort")
    }
}
