# VBuddy

Chat-Applikation, mit der man mit einem virtuellen Freund (Virtual Buddy = VBuddy) reden kann.

## Konzept

### Was ist ein VBuddy?

Ein VBuddy ist ein virtueller Freund mit eigener Persönlichkeit. Er hat:

- **Vorlieben und Bedürfnisse** — z.B. Hunger, Langeweile, Wissensdurst, Bewegungsdrang, soziale Interaktion. Diese Bedürfnisse verändern sich über die Zeit und werden durch Tätigkeiten gestillt.
- **Einen eigenen Tagesplan** — Der VBuddy plant seinen Tag selbstständig mithilfe eines LLMs. Der Tagesplan berücksichtigt seine aktuellen Bedürfnisse und Vorlieben und ist in sich konsistent (z.B. kein Mittagessen direkt nach dem Frühstück, logische Übergänge zwischen Aktivitäten).
- **Blogartikel** — Zu seinen geplanten Tätigkeiten erstellt der VBuddy Blogartikel (LLM-generiert) und veröffentlicht diese über **Browser Use** auf dem externen Blog `https://elitegames.v6.rocks/vbuddy-blog/`.
- **Aufenthaltsort** — Der VBuddy befindet sich immer an einem bestimmten Ort auf der Welt. Der Ort ergibt sich aus seinen Aktivitäten und seinem Tagesplan (z.B. zu Hause, im Park, im Restaurant, in der Bibliothek). Ortswechsel geschehen logisch im Rahmen des Tagesablaufs.

### Kernfunktionen

1. **Chat** — Der Nutzer kann mit dem VBuddy in Echtzeit chatten. Der VBuddy antwortet kontextbezogen, basierend auf seiner Persönlichkeit, seinen aktuellen Aktivitäten, seinen Tagesplan und seinen Bedürfnissen. Die Nutzereingaben können seine Bedürfnisse und Pläne beeinflussen.
2. **Tagesplanung** — Der VBuddy generiert täglich einen konsistenten Tagesablauf. Tätigkeiten beeinflussen seine Bedürfnisse (z.B. Essen stillt Hunger, Sport stillt Bewegungsdrang).
3. **Blog** — Der VBuddy verfasst Blogartikel zu seinen Aktivitäten und veröffentlicht diese auf einem echten Blog unter `https://elitegames.v6.rocks/vbuddy-blog/`. Dafür nutzt der VBuddy das AI-Tool **Browser Use**, um den Blog-Editor im Browser zu bedienen und Beiträge zu erstellen. Der Blog ist **nicht** Teil der VBuddy-GUI — er existiert ausschließlich als externe Website.
4. **Bedürfnissystem** — Bedürfnisse steigen über Zeit an und werden durch passende Aktivitäten reduziert. Das beeinflusst Stimmung und Tagesplanung.
5. **Aufenthaltsort** — Der VBuddy hat immer einen aktuellen Aufenthaltsort. Aktivitäten sind an Orte gebunden, und der VBuddy wechselt seinen Standort entsprechend seinem Tagesplan. Der aktuelle Ort fließt in Chat-Antworten und Tagesplanung ein.
6. **AI-Decision-Log** — Alle Überlegungen und Entscheidungen des VBuddy werden protokolliert.

### Konsistenz des Tagesablaufs

- Aktivitäten folgen einer logischen zeitlichen Reihenfolge
- Keine Überlappungen oder unrealistischen Übergänge
- Bedürfnisse beeinflussen die Wahl der nächsten Aktivität
- Bisherige Aktivitäten des Tages fließen als Kontext in die Planung ein (RAG)

## Tech Stack

- **Frontend**: Vite + React + TypeScript
- **Backend**: Java 21 + Spring Boot 3.4 + LangChain4j
- **Database**: PostgreSQL with pgvector extension (für Daten + RAG/Embedding-Speicher)
- **Containerisierung**: Docker Compose (alle Services)

## Projektstruktur

```
VBuddy/
├── frontend/          # Vite React TypeScript App
├── backend/           # Spring Boot + LangChain4j (Maven)
├── docker-compose.yml # Startet alle Services
└── CLAUDE.md
```

## AI-Integration

Details zur LLM- und AI-Integration sind in [AI_INTEGRATION.md](AI_INTEGRATION.md) dokumentiert.

## Backend

- **Build-Tool**: Maven
- **Framework**: Spring Boot 3.4.x
- **LLM-Integration**: LangChain4j (langchain4j-spring-boot-starter, langchain4j-open-ai, langchain4j-pgvector)
- **LLM-Provider**: durch selfhosted Systeme (Ollama)
- **DB-Migrationen**: Flyway
- **Java-Version**: 21
- **Package**: `com.github.martinfrank.vbuddy`
- **Port**: 8080

### Abhängigkeiten

- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-validation
- postgresql (runtime)
- langchain4j-spring-boot-starter
- langchain4j-open-ai-spring-boot-starter
- langchain4j-pgvector (RAG mit pgvector)
- flyway-core + flyway-database-postgresql

## Frontend

- **Build-Tool**: Vite
- **Framework**: React 19 + TypeScript
- **Port**: 5173 (dev), 80 (Docker/nginx)

## Docker Compose Services

| Service    | Image / Build       | Port          |
|------------|---------------------|---------------|
| `postgres` | pgvector/pgvector:pg17 | 5432:5432  |
| `backend`  | ./backend (Dockerfile) | 8080:8080  |
| `frontend` | ./frontend (Dockerfile)| 3000:80    |

### Datenbank-Konfiguration

- DB-Name: `vbuddy`
- User: `vbuddy`
- Password: `vbuddy` (nur lokal/dev)
- pgvector Extension wird automatisch aktiviert

### Umgebungsvariablen

- `OPENAI_API_KEY` — wird für LLM und Embedding-Modelle benötigt (via `.env` Datei oder docker-compose environment)

## Base Path

Die gesamte Applikation läuft unter dem Pfad `/vbuddy` (nicht unter `/`).

- **Backend**: Context-Path ist `/vbuddy` (`server.servlet.context-path=/vbuddy`). Alle API-Endpunkte sind unter `/vbuddy/api/...` erreichbar.
- **Frontend**: Vite `base`-Option ist `/vbuddy/`. Das Frontend wird unter `/vbuddy/` ausgeliefert.
- **Nginx (Docker)**: Leitet `/vbuddy/` auf das Frontend und `/vbuddy/api/` auf das Backend weiter.

## Konventionen

- Backend-Code in `backend/src/main/java/de/vbuddy/`
- DB-Migrationen in `backend/src/main/resources/db/migration/`
- Frontend-Source in `frontend/src/`
- Alle Services starten mit: `docker compose up --build`
- Alle Zeiten, Datumsangaben und Timestamps sind ausschließlich in **UTC**
