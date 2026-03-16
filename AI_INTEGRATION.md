# AI-Integration — VBuddy

Dieses Dokument beschreibt die Details der LLM- und AI-Integration des VBuddy-Projekts.

## LLM-Provider

- **Provider**: Ollama (selfhosted)
- **Base-URL**: `http://localhost:11434/v1` (OpenAI-kompatible API)
- **Integration**: LangChain4j (OpenAI-kompatibler Client, v0.36.2)

### Modelle

Drei separate LLMs werden für unterschiedliche Aufgaben eingesetzt:

| Aufgabe    | Modell            | Temperatur | Zweck                                      |
|------------|-------------------|------------|---------------------------------------------|
| Planung    | `deepseek-r1:7b`  | 0.7        | Logisches Reasoning für Aktivitätsplanung   |
| Ausführung | `qwen3:8b`        | 0.7        | Kreativität für Blog-Erstellung + Reasoning |
| Chat       | `llama3.1:8b`     | 0.7        | Natürliche Konversation                     |
| Embedding  | `nomic-embed-text` | —         | RAG/Embedding (768 Dimensionen, pgvector)   |

Konfiguration in `application.yml` unter `vbuddy.ai.planning`, `vbuddy.ai.execution`, `vbuddy.ai.chat` und `vbuddy.ai.embedding`.

## Agenten-Architektur

Die Tätigkeiten des VBuddy werden durch drei spezialisierte Agenten gesteuert. Alle werden als LangChain4j `AiServices`-Interfaces implementiert und in `AiConfig.java` konfiguriert.

### Planungs-Agent (`PlanningAiService`)

- **Aufgabe**: Plant die nächsten 3-5 Aktivitäten des VBuddy
- **Input**: Persönlichkeit, aktueller Ort, aktuelle Uhrzeit, Bedürfnisse (0-100), zuletzt erledigte Aktivitäten (letzte 10), Websuche-Ergebnisse, historischer Kontext (RAG)
- **Output**: Strukturiertes `PlannedTasks`-Objekt (Liste von `PlannedTask` + Reasoning)
- **Auslösung**: Manuell via `POST /api/buddies/{buddyId}/tasks/plan` oder automatisch durch den Lifecycle-Service, wenn keine aktiven/geplanten Tasks vorhanden sind
- **System-Prompt**: Regeln für realistische, konsistente Planung — keine Zeitüberlappungen, Berücksichtigung von Bedürfnissen und Persönlichkeit, Vermeidung von Wiederholungen

**User-Prompt-Template**:
```
Plane die nächsten Aktivitäten für den VBuddy.

**Persönlichkeit:** {{personality}}
**Aktueller Ort:** {{currentLocation}}
**Aktuelle Uhrzeit:** {{currentTime}}
**Aktuelle Bedürfnisse (0=kein Bedarf, 100=maximaler Bedarf):**
{{needs}}
**Zuletzt erledigte Aktivitäten:**
{{recentTasks}}
**Lokale Veranstaltungen und Aktivitäten (Websuche):**
{{localActivities}}
**Historischer Kontext (Hintergrundgeschichte und vergangene Erlebnisse):**
{{historicalContext}}
```

**Output-Struktur** (`PlannedTask`):
- `title` — Titel der Aktivität
- `description` — Beschreibung
- `location` — Ort
- `startTime` — Startzeit im Format `yyyy-MM-dd HH:mm`
- `durationMinutes` — Dauer in Minuten

### Enrichment-Agent (`EnrichmentAiService`)

- **Aufgabe**: Reichert die vom Planungs-Agent erzeugten Task-Beschreibungen an — macht sie ausführlicher und lebendiger
- **Modell**: `qwen3:8b` (Execution-Modell) — besser für kreatives Schreiben als das Reasoning-Modell `deepseek-r1:7b`
- **Input**: Persönlichkeit, Task-Titel, Task-Ort, Original-Beschreibung, Dauer, Planungskontext (Reasoning des Planungs-Agents), Websuche-Ergebnisse
- **Output**: Strukturiertes `EnrichedTask`-Objekt (`enrichedDescription`)
- **Auslösung**: Automatisch nach der Planung, vor dem Speichern — jeder Task wird einzeln angereichert
- **Fehlerbehandlung**: Graceful Degradation — bei Fehler wird die Original-Beschreibung beibehalten

**System-Prompt**: Kreativer Autor, der Aktivitätsbeschreibungen ausführlich und lebendig gestaltet (3-5 Sätze, passend zur Persönlichkeit, auf Deutsch, aus Erzähler-Perspektive).

### Ausführungs-Agent (`ExecutionAiService`)

- **Aufgabe**: Führt eine geplante Aktivität aus — erstellt einen Blogartikel und bestimmt Bedürfnis-Anpassungen
- **Input**: Persönlichkeit, Aktivitäts-Details (Titel, Beschreibung, Ort, Dauer), aktuelle Bedürfnisse
- **Output**: Strukturiertes `TaskExecutionResult`-Objekt (Blog-Titel, Blog-Inhalt, Bedürfnis-Anpassungen, Reasoning)
- **Auslösung**: Via `POST /api/buddies/{buddyId}/tasks/{taskId}/execute`, `POST /api/buddies/{buddyId}/tasks/execute-all` oder automatisch durch den Lifecycle-Service

**Regeln für Bedürfnis-Anpassungen**:
- Werte zwischen -30 und +10 pro Bedürfnis
- Negativer Wert = Bedürfnis wird gestillt (z.B. Essen stillt HUNGER um -25)
- Positiver Wert = Bedürfnis steigt (z.B. alleine lesen erhöht SOCIAL um +5)
- Nicht jede Aktivität beeinflusst jedes Bedürfnis — nur relevante werden angegeben
- Verfügbare Bedürfnisse: HUNGER, BOREDOM, KNOWLEDGE, EXERCISE, SOCIAL

### Zusammenspiel & Lifecycle

1. Der **Planungs-Agent** erstellt den Tagesplan (3-5 Aktivitäten)
2. Der **Enrichment-Agent** reichert jede Task-Beschreibung einzeln an (vor dem Speichern)
3. Der **Ausführungs-Agent** arbeitet die Aktivitäten der Reihe nach ab — mit den bereits angereicherten Beschreibungen
4. Nach Abschluss einer Aktivität aktualisiert der Ausführungs-Agent den Zustand (Bedürfnisse, Ort, Blog-Post)
5. Alle Entscheidungen der Agenten werden im AI-Decision-Log protokolliert

**Automatischer Lifecycle** (`BuddyLifecycleService`):
- Tick-Intervall: Alle 60 Sekunden (konfigurierbar via `vbuddy.lifecycle.interval-ms`)
- Wenn kein aktiver Task und keine geplanten Tasks vorhanden: automatische Neuplanung
- Erster neu geplanter Task wird automatisch gestartet

## Chat

- **Service**: `ChatService.java`
- **Modell**: `llama3.1:8b`
- **Endpunkte**: `GET/POST /api/buddies/{buddyId}/chat`

### Kontext im Chat-Prompt

Der System-Prompt wird dynamisch in `ChatService.buildSystemPrompt()` zusammengebaut und enthält:
- Name und Persönlichkeit des VBuddy
- Relevanter Hintergrund und Erfahrungen (RAG — semantische Suche mit der User-Nachricht, max. 5 Ergebnisse)
- Aktueller Aufenthaltsort
- Aktuelle Bedürfnisse (gerundete Werte mit Max-Wert)
- Aktive Aktivität (falls vorhanden) mit Details
- Letzte 5 abgeschlossene Aktivitäten

### Chat-History

- **Max. History**: 20 Nachrichten (`MAX_HISTORY_MESSAGES`)
- **Speicherung**: PostgreSQL-Tabelle `chat_message` (USER/ASSISTANT-Rollen)
- **Ablauf**:
  1. User-Nachricht wird in DB gespeichert (Rolle: USER)
  2. System-Prompt wird dynamisch mit aktuellem Buddy-Zustand erstellt
  3. Letzte 20 Nachrichten werden aus DB geladen und als LangChain4j-Messages konvertiert
  4. ChatRequest wird mit allen Nachrichten an das Chat-Modell gesendet
  5. Antwort wird in DB gespeichert (Rolle: ASSISTANT)

### RAG im Chat

RAG ist im Chat aktiv integriert. Die User-Nachricht wird als Suchquery an den `EmbeddingService` übergeben (max. 5 Ergebnisse, MinScore 0.5). Gefundene Segmente (Background-Narrativ und abgeschlossene Tasks) werden als "Relevanter Hintergrund und Erfahrungen" in den System-Prompt eingefügt — zwischen Persönlichkeit und Aufenthaltsort.

## Tagesplan-Generierung

- **Service**: `PlanningAgentService.java`
- **Endpunkt**: `POST /api/buddies/{buddyId}/tasks/plan`

### Ablauf

1. Buddy, Bedürfnisse und kürzlich erledigte Tasks (letzte 10) werden geladen
2. Websuche via SearXNG nach lokalen Aktivitäten/Veranstaltungen
3. RAG-Abfrage: Persönlichkeit + Bedürfnisse als Query an `EmbeddingService` (max. 10 Ergebnisse) → historischer Kontext
4. Bedürfnisse werden als String formatiert (`needType: currentValue/maxValue`)
5. `PlanningAiService.planTasks()` wird mit allen Inputs inkl. `historicalContext` aufgerufen
6. Rückgabe: strukturiertes `PlannedTasks`-Objekt
7. **Enrichment-Schritt**: Jeder `PlannedTask` wird einzeln durch `EnrichmentAiService.enrichTask()` geschickt — mit Persönlichkeit, Planungs-Reasoning und Websuche-Ergebnissen als Kontext. Bei Fehler wird die Original-Beschreibung beibehalten.
8. Angereicherte `PlannedTask`-Records werden in `VBuddyTask`-Entities konvertiert (Status: PLANNED)
9. AI-Decision-Log-Eintrag wird erstellt (Kontext, Entscheidung, Reasoning)

### Zeitraster

- Freie Zeiträume (kein festes Stunden-Raster)
- Startzeiten im Format `yyyy-MM-dd HH:mm`
- Dauer in Minuten
- Flexibler DateTime-Parser für verschiedene LLM-Ausgabeformate

## Blog-Generierung

- **Erzeugung**: Automatisch durch den Ausführungs-Agenten bei Task-Completion
- **Inhalt**: Aus der Ich-Perspektive des VBuddy, lebendig, persönlich, passend zur Persönlichkeit
- **Speicherung**: `BlogPost`-Entity in PostgreSQL (Titel max. 300 Zeichen, Inhalt als TEXT)
- **Endpunkte**: `GET /api/buddies/{buddyId}/blog-posts`, `GET /api/buddies/{buddyId}/blog-posts/{postId}`

### Veröffentlichung

- Geplant: Über **Browser Use** auf `https://elitegames.v6.rocks/vbuddy-blog/`
- **Status**: Noch nicht implementiert — Blog-Posts werden generiert und in der DB gespeichert, aber noch nicht extern veröffentlicht

## Bedürfnissystem

### Bedürfnisse

| Bedürfnis   | Beschreibung       |
|-------------|-------------------|
| HUNGER      | Bedarf nach Essen  |
| BOREDOM     | Langeweile         |
| KNOWLEDGE   | Wissensdurst       |
| EXERCISE    | Bewegungsdrang     |
| SOCIAL      | Soziale Interaktion|

### Werte und Decay

- **Wertebereich**: 0 (kein Bedarf) bis 100 (maximaler Bedarf)
- **Max-Wert**: 100 (Standard)
- **Decay-Rate**: 5.0 pro Stunde (Standard) — Bedürfnisse steigen natürlich über die Zeit

### AI-Integration

1. **Planungs-Agent** erhält aktuelle Bedürfnisse als Input und berücksichtigt sie bei der Aktivitätsplanung
2. **Ausführungs-Agent** gibt `NeedAdjustment`-Objekte zurück (needType + change-Wert)
3. `ExecutionAgentService.adjustNeeds()` mappt die Adjustments auf Need-Entities, clampt auf 0-maxValue und speichert

## Persönlichkeit

- Freitext-Feld am `Buddy`-Entity
- Fließt in alle drei AI-Kontexte ein: Planung, Ausführung und Chat
- Beeinflusst Stil der Blog-Artikel, Wahl der Aktivitäten und Tonalität im Chat

## Aufenthaltsort

- Feld `location` am `Buddy`-Entity
- Wird bei Task-Completion durch den Ort der ausgeführten Aktivität aktualisiert
- Fließt in Planungs- und Chat-Prompt ein
- Orte sind frei wählbar (keine vordefinierte Liste) — das LLM bestimmt passende Orte

## RAG (Retrieval Augmented Generation)

- **Embedding-Store**: pgvector (PostgreSQL Extension, automatisch aktiviert)
- **Embedding-Modell**: `nomic-embed-text` (Ollama, 768 Dimensionen)
- **Abhängigkeit**: `langchain4j-pgvector` (v0.36.2)
- **Tabelle**: `vbuddy_embeddings` (automatisch erstellt via `createTable(true)`)
- **Service**: `EmbeddingService.java`
- **MinScore**: 0.5

### Embedding-Erzeugung

- **Background**: Nach erfolgreicher Generierung des narrativen Texts in `BackgroundAgentService` wird dieser als Segment embedded (Metadata: `buddy_id`, `type=background`)
- **Abgeschlossene Tasks**: Nach Task-Completion in `ExecutionAgentService` wird der Task als Segment embedded (Metadata: `buddy_id`, `type=task`, `task_id`)
- Beide Aufrufe sind in try/catch gewrapped — Embedding-Fehler unterbrechen nicht den Hauptprozess

### Embedding-Retrieval

- **Chat**: User-Nachricht als Query, max. 5 Ergebnisse → im System-Prompt als "Relevanter Hintergrund und Erfahrungen"
- **Planung**: Persönlichkeit + Bedürfnisse als Query, max. 10 Ergebnisse → als `historicalContext` im User-Prompt

### Backfill

- **Service**: `EmbeddingBackfillService.java`
- **Endpunkt**: `POST /api/admin/backfill-embeddings`
- Embeddet alle bestehenden COMPLETED Backgrounds und COMPLETED Tasks nachträglich

## AI-Decision-Log

- Alle LLM-gestützten Entscheidungen werden in `ai_decision_log` protokolliert
- **Felder**: Kontext, Entscheidung, Begründung (Reasoning), Zeitstempel, Buddy-ID
- **Endpunkt**: `GET /api/buddies/{buddyId}/ai-decision-log`
- **Geloggte Entscheidungen**:
  - Tagesplanung (Planungs-Agent): geplante Aktivitäten + Reasoning
  - Task-Ausführung (Ausführungs-Agent): Bedürfnis-Anpassungen + Reasoning
