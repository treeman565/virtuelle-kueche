# Virtuelle Küche

Android-App, die deinen Lebensmittelvorrat per Kassenbon-Scan erfasst, als virtuelle Küche zeigt, vor dem Ablauf erinnert, Rezepte aus dem Vorrat vorschlägt und deine Ausgaben zusammenfasst.

**Stand:** Schritt 2, das Grundgerüst. Anmeldung per E-Mail und Passwort, Cloud-Anbindung an Firestore mit Offline-Cache und die untere Navigation mit Küche, Rezepte, Einkaufsliste und Ausgaben (noch als Platzhalter).

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
