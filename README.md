# Virtuelle Küche

Android-App, die deinen Lebensmittelvorrat per Kassenbon-Scan erfasst, als virtuelle Küche zeigt, vor dem Ablauf erinnert, Rezepte aus dem Vorrat vorschlägt und deine Ausgaben zusammenfasst.

**Stand:** Schritt 3. Anmeldung, Cloud-Anbindung mit Offline-Cache und der Küche-Reiter: Lebensmittel nach Lagerort, Hinzufügen und Bearbeiten, geschätztes Verfallsdatum, Suche und Filter. Rezepte, Einkaufsliste und Ausgaben sind noch Platzhalter.

## Einrichten (einmalig)

1. **Android Studio** installieren: https://developer.android.com/studio
2. **Firebase-Projekt anlegen:** https://console.firebase.google.com, dann „Projekt hinzufügen“.
3. Im Projekt eine **Android-App** hinzufügen, Paketname `de.virtuellekueche.app`.
4. Die Datei **`google-services.json`** herunterladen und in den Ordner `app/` legen. Sie wird nicht eingecheckt.
5. In der Firebase-Konsole:
   - **Authentication** → Anmeldemethode **E-Mail/Passwort** aktivieren.
   - **Firestore Database** → Datenbank erstellen (Region `europe-west3`, Frankfurt).
   - Unter **Regeln** den Inhalt von [`firestore.rules`](firestore.rules) einfügen und veröffentlichen.
6. Projekt in Android Studio öffnen und auf **Run** klicken (Emulator oder Handy per USB).

## Was du jetzt testen kannst

- Ein Konto registrieren, abmelden und wieder anmelden.
- „Passwort vergessen“ schickt eine E-Mail.
- Zwischen den vier Reitern wechseln.
- Im Reiter **Küche** Lebensmittel hinzufügen. Das Verfallsdatum wird aus Name und Lagerort geschätzt und lässt sich ändern.
- Ein Lebensmittel antippen, um Menge, Lagerort oder Datum zu ändern oder es als verbraucht oder weggeworfen zu markieren.
- Einen eigenen Lagerort anlegen (im Feld „Lagerort“ ganz unten).
- Im Flugmodus etwas eintragen. Sobald wieder Netz da ist, wird es synchronisiert.
