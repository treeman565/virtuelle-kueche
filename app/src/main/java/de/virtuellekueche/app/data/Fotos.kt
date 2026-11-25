package de.virtuellekueche.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Fotos {
    /** Längste Kante in Pixeln. Reicht für gut lesbare Bons und hält die Übertragung klein. */
    private const val MAX_KANTE = 1600

    /** Neue leere Datei für die Kamera-App, freigegeben über den FileProvider. */
    fun neueFotoUri(context: Context): Uri {
        val ordner = File(context.cacheDir, "bons").apply { mkdirs() }
        ordner.listFiles()?.forEach { it.delete() } // alte Fotos aufräumen
        val datei = File(ordner, "bon_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", datei)
    }

    /** Lädt das Foto verkleinert. Ab Android 9 wird die Drehung aus der Kamera berücksichtigt. */
    suspend fun laden(context: Context, uri: Uri): Bitmap = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val quelle = ImageDecoder.createSource(context.contentResolver, uri)
            ImageDecoder.decodeBitmap(quelle) { decoder, info, _ ->
                val (b, h) = info.size.width to info.size.height
                val faktor = MAX_KANTE.toFloat() / maxOf(b, h)
                if (faktor < 1f) decoder.setTargetSize((b * faktor).toInt(), (h * faktor).toInt())
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            val groesse = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, groesse) }
            var sample = 1
            while (maxOf(groesse.outWidth, groesse.outHeight) / (sample * 2) >= MAX_KANTE) sample *= 2
            val optionen = BitmapFactory.Options().apply { inSampleSize = sample }
            context.contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, optionen) }
                ?: error("Foto konnte nicht geladen werden")
        }
    }
}
