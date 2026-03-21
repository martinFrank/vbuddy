# Benutzerverwaltung

## Rollen

- **ADMIN** — Kann Benutzer anlegen, bearbeiten und löschen. Hat Zugriff auf die Benutzerverwaltung.
- **USER** — Kann Buddys ansehen und mit ihnen interagieren.

## Initialer Admin

Beim ersten Start wird automatisch ein Admin-Benutzer aus den Umgebungsvariablen erstellt:

- `ADMIN_USERNAME` (Standard: `admin`)
- `ADMIN_PASSWORD` (Standard: `admin`)

Diese Variablen werden in der `.env`-Datei konfiguriert.

## Neue Benutzer anlegen

Es gibt keine Selbstregistrierung. Neue Benutzer können nur von einem Admin angelegt werden:

1. Als Admin unter `/vbuddy/login` anmelden
2. Über den Link **Benutzerverwaltung** in der Navigation (oder direkt `/vbuddy/admin`) die Admin-Seite öffnen
3. Dort neue Benutzer mit Benutzername, Passwort und Rolle (USER oder ADMIN) anlegen

## Abmelden

Der **Abmelden**-Button befindet sich unten in der Seitenleiste (Navigation). Nach dem Abmelden wird man automatisch zur Login-Seite weitergeleitet.
