package de.virtuellekueche.app

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.persistentCacheSettings

class VirtuelleKuecheApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Offline-Modus: Firestore speichert alle Daten lokal zwischen und
        // synchronisiert automatisch, sobald wieder Netz da ist.
        FirebaseFirestore.getInstance().firestoreSettings = firestoreSettings {
            setLocalCacheSettings(persistentCacheSettings { })
        }
    }
}
