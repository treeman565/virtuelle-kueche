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

/** Liest einen Kassenbon von einem Foto. */
interface BonErkennung {
    suspend fun erkennen(foto: Bitmap): BonErgebnis
}

/**
 * Gemini über Firebase AI Logic. Der Zugang läuft über das Firebase-Projekt,
 * es liegt also kein API-Schlüssel in der App.
 */
class GeminiBonErkennung : BonErkennung {

    private val modell by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = "gemini-2.5-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                temperature = 0f
            },
        )
    }

    override suspend fun erkennen(foto: Bitmap): BonErgebnis {
        val antwort = modell.generateContent(
            content {
                image(foto)
                text(BonPrompt.text)
            },
        )
        return BonParser.parse(antwort.text ?: throw IllegalArgumentException("Leere Antwort"))
    }
}
