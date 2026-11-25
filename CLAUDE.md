# Projekt: Virtuelle Küche (Android-App)

## Ziel
App zur Dokumentation des Lebensmitteleinkaufs per Kassenbon-Scan, mit virtueller Küche, Verfallsdatum-Überwachung, KI-Rezeptvorschlägen und Ausgabenübersicht.

## Tech-Stack (bestätigt)
- Kotlin, Jetpack Compose, Material 3
- Firebase Auth (E-Mail/Passwort) und Firestore mit Offline-Cache
- KI (Bon-Scan, Rezepte): Claude über eine Firebase Cloud Function, der API-Schlüssel liegt nur dort

## Regeln
- Sprache der Oberfläche und der Kommunikation: Deutsch.
- Zielplattform: Android, store-tauglich bauen.
- Design: schlicht, clean, Material Design. Keine überladene Optik.
- Erst planen, dann Code schreiben. Wichtige Entscheidungen (Tech-Stack, Dienste, Kosten) vorher mit mir abstimmen.
- In kleinen, einzeln testbaren Schritten arbeiten.
- API-Schlüssel und Passwörter niemals im App-Code oder im Repository speichern, sondern nur auf dem Server oder in Umgebungsvariablen.
- Nutzerdaten sind geschützt: Anmeldung erforderlich, jeder sieht nur seine eigenen Daten.
- Offline-Fähigkeit mit späterer Synchronisation einplanen.
- Verfallsdaten werden automatisch geschätzt und sind immer manuell änderbar.
- Korrekturen bei der Bon-Erkennung werden gespeichert und beim nächsten Scan wiederverwendet.
- Kein Cookidoo.
- Gekochte Gerichte lassen sich mit 1 bis 5 Sternen bewerten und als Favoriten markieren.
- Die Einkaufsliste ist ein eigener Reiter, nach Gericht gruppiert, mit freier Produkteingabe.

## Kernbegriffe
- Lebensmittel: Name, Menge, Einheit, Lagerort, Kaufdatum, Preis, Verfallsdatum.
- Lagerort: Kühlschrank, Vorratsschrank, Tiefkühler, weitere frei anlegbar.
- Virtuelle Küche: gezeichnete Küche als Hauptansicht, Lebensmittel werden automatisch oder von Hand eingeräumt.

## Umsetzungsreihenfolge
1. Plan, Tech-Stack, Datenmodell (erledigt)
2. Grundgerüst mit Anmeldung und Cloud-Anbindung
3. Lebensmittel manuell erfassen
4. Virtuelle Küche als Oberfläche
5. Kassenbon-Scan
6. Lernende Korrektur
7. Benachrichtigungen vor dem Ablauf
8. Rezeptvorschläge mit automatischem Verbrauch
9. Sternebewertung und Favoriten
10. Einkaufsliste
11. Ausgabenübersicht
12. Offline-Modus, Feinschliff und Tests
