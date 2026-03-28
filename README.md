# VBuddy

Chat-Applikation, mit der man mit einem virtuellen Freund (Virtual Buddy = VBuddy) reden kann.

## Tech Stack

- **Frontend**: Vite + React 19 + TypeScript
- **Backend**: Java 21 + Spring Boot 3.4 + LangChain4j
- **Datenbank**: PostgreSQL 17 mit pgvector Extension
- **Containerisierung**: Docker Compose

## Voraussetzungen

- Docker & Docker Compose
- (Optional) Java 21, Node.js 20+ für lokale Entwicklung

## Starten

```bash
docker compose up --build
```

Die Applikation ist unter `http://localhost:3000/vbuddy/` erreichbar.

## Services

| Service    | Port       | Beschreibung                  |
|------------|------------|-------------------------------|
| Frontend   | 3000 (80)  | Vite React App via nginx      |
| Backend    | 8080       | Spring Boot REST API          |
| PostgreSQL | 5432       | Datenbank mit pgvector        |

## Projektstruktur

```
VBuddy/
├── frontend/          # Vite React TypeScript App
├── backend/           # Spring Boot + LangChain4j (Maven)
├── docker-compose.yml
├── CLAUDE.md
└── README.md
```

## Lizenz

Privates Projekt.
