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
| Embedding  | `nomic-embed-text` | —         | RAG/Embedding (noch nicht aktiv integriert) |

Konfiguration in `application.yml` unter `vbuddy.ai.planning`, `vbuddy.ai.execution`, `vbuddy.ai.chat` und `vbuddy.ai.embedding`.

## Agenten-Architektur

Die Tätigkeiten des VBuddy werden durch zwei spezialisierte Agenten gesteuert. Beide werden als LangChain4j `AiServices`-Interfaces implementiert und in `AiConfig.java` konfiguriert.

### Planungs-Agent (`PlanningAiService`)

- **Aufgabe**: Plant die nächsten 3-5 Aktivitäten des VBuddy
- **Input**: Persönlichkeit, aktueller Ort, aktuelle Uhrzeit, Bedürfnisse (0-100), zuletzt erledigte Aktivitäten (letzte 10)
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
```

**Output-Struktur** (`PlannedTask`):
- `title` — Titel der Aktivität
- `description` — Beschreibung
- `location` — Ort
- `startTime` — Startzeit im Format `yyyy-MM-dd HH:mm`
- `durationMinutes` — Dauer in Minuten

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
2. Der **Ausführungs-Agent** arbeitet die Aktivitäten der Reihe nach ab
3. Nach Abschluss einer Aktivität aktualisiert der Ausführungs-Agent den Zustand (Bedürfnisse, Ort, Blog-Post)
4. Alle Entscheidungen beider Agenten werden im AI-Decision-Log protokolliert

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

RAG ist im Chat aktuell **nicht aktiv integriert**. Die pgvector-Infrastruktur ist vorhanden, wird aber noch nicht genutzt.

## Tagesplan-Generierung

- **Service**: `PlanningAgentService.java`
- **Endpunkt**: `POST /api/buddies/{buddyId}/tasks/plan`

### Ablauf

1. Buddy, Bedürfnisse und kürzlich erledigte Tasks (letzte 10) werden geladen
2. Bedürfnisse werden als String formatiert (`needType: currentValue/maxValue`)
3. `PlanningAiService.planTasks()` wird mit allen Inputs aufgerufen
4. Rückgabe: strukturiertes `PlannedTasks`-Objekt
5. `PlannedTask`-Records werden in `VBuddyTask`-Entities konvertiert (Status: PLANNED)
6. AI-Decision-Log-Eintrag wird erstellt (Kontext, Entscheidung, Reasoning)

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
- **Embedding-Modell**: `nomic-embed-text` (Ollama)
- **Abhängigkeit**: `langchain4j-pgvector` (v0.36.2)
- **Status**: Infrastruktur vorhanden, aber **noch nicht aktiv in die LLM-Aufrufe integriert**

## AI-Decision-Log

- Alle LLM-gestützten Entscheidungen werden in `ai_decision_log` protokolliert
- **Felder**: Kontext, Entscheidung, Begründung (Reasoning), Zeitstempel, Buddy-ID
- **Endpunkt**: `GET /api/buddies/{buddyId}/ai-decision-log`
- **Geloggte Entscheidungen**:
  - Tagesplanung (Planungs-Agent): geplante Aktivitäten + Reasoning
  - Task-Ausführung (Ausführungs-Agent): Bedürfnis-Anpassungen + Reasoning
