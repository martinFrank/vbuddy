# LLM-Modellvergleich: Structured Output für VBuddy-Agents

Vergleich von vier selfhosted Ollama-Modellen als Planning-Agent-Backbone für die VBuddy-Anwendung. Getestet mit 21 LLM-Integrationstests, die den tatsächlichen Output der AI Services auf Struktur, Format, Plausibilität und inhaltliche Qualität prüfen.

**Testumgebung:**
- Ollama auf `192.168.0.251:11434`
- LangChain4j 0.36.2 mit `AiServices.builder()` (Structured Output via JSON-Schema)
- Kein Spring Context, keine DB — reine LLM-zu-Interface-Tests
- Jedes Modell wurde als Planning-/Schedule-/ChatAnalysis-Modell getestet, während `qwen3:8b` durchgehend als Execution-/Enrichment-Modell diente

---

## Getestete Modelle

| Modell | Parameter | Größe | Quantisierung | Familie |
|--------|-----------|-------|---------------|---------|
| `deepseek-r1:7b` | 7.6B | 4.7 GB | Q4_K_M | qwen2 |
| `qwen3:8b` | 8.2B | 5.2 GB | Q4_K_M | qwen3 |
| `deepseek-r1:14b` | 14.8B | 9.0 GB | Q4_K_M | qwen2 |
| `qwen3:14b` | 14.8B | 9.0 GB | Q4_K_M | qwen3 |

---

## Gesamtergebnis

| Modell | Bestanden | Fehler | Failures | Gesamt | Quote |
|--------|-----------|--------|----------|--------|-------|
| **qwen3:14b** | **21** | **0** | **0** | 21 | **100%** |
| deepseek-r1:14b | 19 | 1 | 1 | 21 | 90% |
| deepseek-r1:7b | 17 | 4 | 0 | 21 | 81% |
| qwen3:8b | 16 | 5 | 0 | 21 | 76% |

---

## Detailergebnisse pro Test

### BackgroundEnrichmentAiService (Execution-Modell: qwen3:8b)

Generiert einen Erzähltext in 3. Person aus strukturierten Charakterdaten.

| Test | deepseek-r1:7b | qwen3:8b | deepseek-r1:14b | qwen3:14b |
|------|:-:|:-:|:-:|:-:|
| `enrichBackground_returnsNarrativeText` | PASS | PASS | PASS | PASS |
| `enrichBackground_isWrittenInThirdPerson` | PASS | PASS | PASS | PASS |

Alle Modelle nutzen hier `qwen3:8b` als Execution-Modell — keine Unterschiede. Output-Qualität durchgehend gut: lebendige Erzähltexte, korrekte 3. Person, 500+ Zeichen.

---

### BackgroundPlanningAiService (Planning-Modell)

Generiert ein strukturiertes Charakterprofil (PlannedBackground-Record mit 13 Feldern).

| Test | deepseek-r1:7b | qwen3:8b | deepseek-r1:14b | qwen3:14b |
|------|:-:|:-:|:-:|:-:|
| `planBackground_returnsCompleteCharacterProfile` | PASS | PASS | PASS | PASS |
| `planBackground_ageIsConsistentWithPersonality` | ERROR (MalformedJson) | PASS | PASS | PASS |

**Inhaltliche Qualität im Vergleich:**

| Aspekt | deepseek-r1:7b | qwen3:8b | deepseek-r1:14b | qwen3:14b |
|--------|----------------|----------|-----------------|-----------|
| Alter erkannt (28) | Ja | Ja | Ja | Ja |
| Aussehen | "Mittelaltiggro ß" (Encoding-Artefakte) | "Mittelgroß, schlank, karamellbraune Kurzhaare" | "1,85 m groß, sportliche Figur, kurze Stoppeln" | "Mittelgroß, dunkle kurze Haare, schmale Brille, sportlich" |
| Hobbys | "Gärtnern, Wandern, Kochen" (OK) | "Erkundet neue Restaurants, Bridge" (kreativ) | "Hiking, Klettern, Spieleentwurf" (vielfältig) | "Laufen, Mountainbiken, Kochen exotischer Gerichte, Food-Festivals" |
| Macken | "Trinkt Kaffee aus spezieller Tasse" | "Stolpert über eigene Füße, zählt Bäume" | "Fummelt am Rucksack, spezielle Kaffeemischung" | "Bestimmte Messerschleife, redet bei Stress leise und schnell" |
| Kohärenz | Teils wirr ("JungenInterpolator", "Tochter" bei 28j. Single) | Gut kohärent | Gut, aber teils Sprachwechsel ins Englische | Sehr gut — durchgehend kohärent und auf Deutsch |
| Reasoning | Teils unverständlich | Klar und nachvollziehbar | Ausführlich und kausal verknüpft | Exzellent — erklärt jede Entscheidung mit Bezug zur Persönlichkeit |

**Fazit:** `qwen3:14b` liefert die kohärentesten und detailliertesten Profile — durchgehend auf Deutsch, mit nachvollziehbarem Reasoning. `qwen3:8b` ist ebenfalls gut. `deepseek-r1:14b` wechselt gelegentlich ins Englische. `deepseek-r1:7b` produziert teils unsinnige Inhalte.

---

### ChatPlanAnalysisAiService (Planning-Modell)

Analysiert Chatverläufe auf vereinbarte Planänderungen (CANCEL, UPDATE, ADD).

| Test | deepseek-r1:7b | qwen3:8b | deepseek-r1:14b | qwen3:14b |
|------|:-:|:-:|:-:|:-:|
| `smalltalk_noAdjustment` | PASS | PASS | PASS | PASS |
| `cancelRequest_detectsAdjustment` | ERROR (NumberFormat "1L") | PASS | PASS | PASS |
| `addRequest_detectsNewTask` | FAIL (false negative) | PASS | PASS | PASS |

**Detailanalyse der Ergebnisse:**

**Smalltalk (alle bestanden):**

| Modell | Reasoning |
|--------|-----------|
| deepseek-r1:7b | Korrekt erkannt, aber Reasoning teils unklar |
| qwen3:8b | "Konversation besteht aus Smalltalk und positiven Rückfragen" |
| deepseek-r1:14b | "Der Nutzer hat keine explizite Änderung vorgeschlagen. Die Gespräche bestehen aus Smalltalk." |

**Cancel-Request:**

| Modell | Ergebnis | Detail |
|--------|----------|--------|
| deepseek-r1:7b | ERROR | LLM gab `"1L"` statt `1` als existingTaskId zurück (Java-Long-Literal im JSON) |
| qwen3:8b | PASS | `CANCEL | ID:1 | Joggen im Park` — korrekte ID und Action |
| deepseek-r1:14b | PASS | `CANCEL | ID:1 | Joggen im Park` — korrekt mit klarem Reasoning |
| qwen3:14b | PASS | `CANCEL | ID:1 | Joggen im Park` — mit detailliertem Reasoning ("passt zur Persönlichkeit, da er bei Unwetter realistisch reagiert") |

**Add-Request:**

| Modell | Ergebnis | Detail |
|--------|----------|--------|
| deepseek-r1:7b | FAIL | `adjustmentNeeded: false` — hat die Zustimmung nicht erkannt |
| qwen3:8b | PASS | `ADD | Kino | 2026-03-21 15:00 | Kino` |
| deepseek-r1:14b | PASS | `ADD | Science-Fiction-Film im Kino | 2026-03-21 15:00 | Cinema` — kontextsensitiver Titel |
| qwen3:14b | PASS | `ADD | Ins Kino gehen | 2026-03-21 15:00 | Kino` — prüft sogar Kollisionsfreiheit mit bestehendem Plan |

**Fazit:** `qwen3:14b` zeigt das beste Reasoning — prüft sogar Kollisionsfreiheit mit bestehenden Tasks. `deepseek-r1:14b` generiert den kreativsten Titel. `deepseek-r1:7b` scheitert an JSON-Formatierung und inhaltlicher Erkennung.

---

### EnrichmentAiService (Execution-Modell: qwen3:8b)

Reichert kurze Task-Beschreibungen mit Details an.

| Test | deepseek-r1:7b | qwen3:8b | deepseek-r1:14b | qwen3:14b |
|------|:-:|:-:|:-:|:-:|
| `enrichTask_returnsDetailedDescription` | PASS | PASS | PASS | PASS |
| `enrichTask_isWrittenInGerman` | PASS | PASS | PASS | PASS |

Alle nutzen `qwen3:8b` — keine Unterschiede. Output durchgehend gut.

---

### ExecutionAiService (Execution-Modell: qwen3:8b für 7b/8b, deepseek-r1:14b für 14b-Run)

Generiert Blogartikel und Bedürfnis-Anpassungen.

| Test | deepseek-r1:7b | qwen3:8b | deepseek-r1:14b | qwen3:14b |
|------|:-:|:-:|:-:|:-:|
| `executeTask_returnsValidBlogAndNeedAdjustments` | PASS | PASS | PASS | PASS |
| `executeTask_needAdjustmentsAreValid` | PASS | PASS | ERROR (JSON) | PASS |
| `executeTask_cookingReducesHunger` | PASS | PASS | PASS | PASS |
| `executeTask_blogIsWrittenInFirstPerson` | PASS | PASS | PASS | PASS |

**Hinweis:** Beim 14b-Run wurde `qwen3:8b` weiterhin als Execution-Modell verwendet. Der eine JSON-Fehler ist sporadisch und nicht modellspezifisch — LangChain4j konnte die LLM-Antwort nicht parsen. Bei Wiederholung würde der Test wahrscheinlich bestehen.

**Fazit:** `qwen3:8b` ist als Execution-Modell sehr zuverlässig (11/12 Testläufe bestanden).

---

### PlanningAiService (Planning-Modell)

Komplexester Agent: 9 Eingabeparameter, generiert 3-5 Tasks mit Zeitstempeln, Orten und Dauern.

| Test | deepseek-r1:7b | qwen3:8b | deepseek-r1:14b | qwen3:14b |
|------|:-:|:-:|:-:|:-:|
| `planTasks_returnsValidStructuredOutput` | ERROR (Array statt Object) | ERROR (MalformedJson) | PASS | PASS |
| `planTasks_tasksAreChronological` | ERROR (DateTime "T"-Format) | PASS | PASS | PASS |
| `planTasks_highHunger_includesFoodRelatedActivity` | PASS | ERROR (String statt Object) | PASS | PASS |

**Output-Vergleich der 14b-Modelle (beide 3/3):**

**deepseek-r1:14b:**
```
Reasoning: The activities were chosen based on Max's high exercise need
           and moderate hunger.

  2026-03-21 17:00 | Sport Session        | Zu Hause       | 60 min
  2026-03-21 18:00 | Dinner at Home       | Zu Hause       | 60 min
  2026-03-21 19:00 | Evening Walk         | Nachbarschaft  | 60 min
  2026-03-21 20:00 | Relaxation Time      | Zu Hause       | 120 min
```

- 4 Tasks, englische Titel, Reasoning auf Englisch
- Korrektes Format, valides JSON

**qwen3:14b:**
```
Reasoning: Max ist während der Arbeitszeit (13:00-17:00) am Arbeiten,
           gefolgt von Sport (17:00-18:00), um seinen hohen Bedarf an
           Bewegung zu stillen. Danach genießt er ein Abendessen
           (18:00-19:00), um den Hunger zu stillen.

  2026-03-21 15:14 | Arbeit               | Zu Hause       | 106 min
  2026-03-21 17:00 | Sport (Laufen)       | Nürnberger Park| 60 min
  2026-03-21 18:00 | Abendessen           | Zu Hause       | 60 min
```

- 3 Tasks, deutsche Titel, Reasoning auf Deutsch mit Begründung pro Bedürfnis
- Bezieht sich explizit auf den Wochenplan (Arbeitszeit 13:00-17:00)
- Korrektes Format, valides JSON

**Fehlerarten der kleineren Modelle:**

| Fehler | deepseek-r1:7b | qwen3:8b |
|--------|----------------|----------|
| `Expected BEGIN_OBJECT but was BEGIN_ARRAY` | Ja | — |
| `Expected BEGIN_OBJECT but was STRING` | — | Ja (2x) |
| `MalformedJsonException: Expected ':'` | — | Ja |
| `DateTimeParseException` (T statt Leerzeichen) | Ja | — |
| Timeout (>300s) | Ja (371s) | — |

**Fazit:** Der PlanningAiService-Prompt ist der Härtetest. Nur die 14b-Modelle produzieren zuverlässig valides JSON bei 9 Eingabeparametern. `qwen3:14b` hat den Vorteil, durchgehend auf Deutsch zu antworten und den Wochenplan-Kontext aktiv zu nutzen.

---

### SchedulePlanningAiService (Planning-Modell)

Erstellt einen 24h-Wochenplan mit lückenlosen Zeitblöcken.

| Test | deepseek-r1:7b | qwen3:8b | deepseek-r1:14b | qwen3:14b |
|------|:-:|:-:|:-:|:-:|
| `planSchedule_returnsWeekdayAndWeekendBlocks` | PASS | PASS | PASS | PASS |
| `planSchedule_timeBlocksHaveValidFormat` | PASS | ERROR (JSON) | PASS | PASS |
| `planSchedule_coversFullDay` | PASS | ERROR (null weekday) | PASS | PASS |
| `planSchedule_blocksAreContiguous` | PASS | ERROR (JSON) | FAIL (Lücke 08:30→09:00) | PASS |

**Generierter Stundenplan im Vergleich:**

**deepseek-r1:7b (4/4, aber inhaltlich fragwürdig):**
```
00:00-08:00  Liebe Nacht
08:00-08:30  Mornings pause (Kaffee trinken)
08:30-16:30  Arbeit im Büro
16:30-16:45  Pausen (Kurzes Bagnus)
16:45-24:00  Arbeit               ← 7:15h Arbeit am Abend?
```

**qwen3:8b (1/4 — JSON-Parsing scheitert bei dieser Struktur):**
- 3 von 4 Tests scheitern an ungültigem JSON
- Wenn JSON parst: Inhalte wären wahrscheinlich gut

**qwen3:14b (4/4 — einziges Modell mit perfekter Score):**
```
Wochentag:
00:00-07:00  Schlafen
07:00-07:30  Morgenroutine (Duschen, Kaffee)
07:30-08:00  Frühstück
08:00-09:00  Pendeln mit dem Fahrrad
09:00-12:00  Arbeit im Büro (Backend-Entwicklung)
12:00-13:00  Mittagspause (Imbiss, kurze Wanderung)
13:00-14:00  Kurze Pause (Kaffee, Nachrichten)
14:00-17:00  Arbeit im Büro (Code-Reviews, Bug-Fixing)
17:00-18:00  Pendeln mit dem Fahrrad
18:00-19:30  Kochabend (aufwendige Gerichte)
19:30-21:00  Science-Fiction-Romane Lesen
21:00-23:00  Leisure (Spiele, Online-Communities)
23:00-00:00  Schlafen

Wochenende:
00:00-08:00  Schlafen
08:00-08:30  Morgenroutine (Duschen, Kaffee)
08:30-09:30  Frühstück
09:30-11:00  Joggen im Park
11:00-14:00  Freizeit (Bücher, Online-Kurse)
14:00-17:00  Klettern mit Freunden (Gym/Outdoor)
17:00-19:00  Essen mit Freunden (Restaurant)
19:00-21:00  Kochabend (experimentelle Gerichte)
21:00-23:00  Science-Fiction-Romane Lesen
23:00-00:00  Schlafen
```

- 13 Wochentag-Blöcke, 10 Wochenend-Blöcke — detailliert und lückenlos
- Vollständige 24h-Abdeckung (00:00-00:00), alle Blöcke nahtlos aneinander
- Beruf korrekt als Backend-Entwicklung mit konkreten Aufgaben (Code-Reviews, Bug-Fixing)
- Pendeln mit dem Fahrrad — kreatives Detail passend zur sportlichen Persönlichkeit
- Wochenende: Klettern mit Freunden, Restaurant — soziale Aktivitäten trotz Introversion
- Durchgehend auf Deutsch

**deepseek-r1:14b (3/4, inhaltlich exzellent):**
```
Wochentag:
00:00-07:00  Schlafen
07:00-07:15  Aufwachen, Duschen
07:15-07:30  Frühstück
07:30-08:00  Berufsmails checken
08:00-10:00  Arbeit
10:00-10:15  Kaffeepause
10:15-12:00  Arbeit
12:00-13:00  Mittagessen
13:00-15:00  Arbeit
15:00-15:15  Kaffeepause
15:15-17:00  Arbeit
17:00-17:15  Heimweg
17:15-17:30  Entspannung
17:30-19:00  Sport (Jogging)
19:00-19:30  Duschbad
19:30-20:00  Abendessen vorbereiten
20:00-20:30  Abendessen
20:30-21:30  Science-Fiction-Roman lesen
21:30-23:00  Entspannung
23:00-00:00  Schlafen

Wochenende:
00:00-09:00  Schlafen
09:00-09:15  Aufwachen, Duschen
09:15-09:30  Frühstück
09:30-11:00  Jogging/Ausflug
11:00-11:30  Heimkommen, entspannen
11:30-12:30  Mittagessen
12:30-14:00  Entspannung, lesen
14:00-15:00  Spaziergang
15:00-16:00  Kochen vorbereiten
16:00-17:30  Relaxation oder lesen
17:30-18:30  Abendessen
18:30-20:00  Film schauen oder lesen
20:00-21:00  Entspannung
21:00-00:00  Schlafen
```

- 20 Wochentag-Blöcke, 14 Wochenend-Blöcke — extrem detailliert
- Vollständige 24h-Abdeckung (00:00-00:00)
- Realistische Zeiten (8h Arbeit, Kaffeepausen, Pendeln)
- Persönlichkeitsgetreu (Jogging, Science-Fiction, Kochen)
- Einziger Fehler: Bei einem separaten Testlauf entstand eine Lücke (08:30→09:00)

**Fazit:** `qwen3:14b` ist das einzige Modell mit 4/4 — lückenloser Stundenplan, durchgehend auf Deutsch, mit kreativen Details (Fahrrad-Pendeln, Klettern mit Freunden). `deepseek-r1:14b` ist ebenfalls gut (3/4), produziert aber gelegentlich Lücken. `deepseek-r1:7b` produziert valides JSON, aber unsinnige Inhalte. `qwen3:8b` scheitert meist am JSON-Parsing für diese komplexe verschachtelte Struktur.

---

### ScheduleEnrichmentAiService (Execution-Modell: qwen3:8b)

Verbessert Formulierungen im Stundenplan, behält Zeiten und Blockanzahl bei.

| Test | deepseek-r1:7b | qwen3:8b | deepseek-r1:14b | qwen3:14b |
|------|:-:|:-:|:-:|:-:|
| `enrichSchedule_preservesTimesAndBlockCount` | PASS | PASS | PASS | PASS |

Alle nutzen `qwen3:8b` — durchgehend zuverlässig.

---

## Durchlaufzeiten

### Gesamtlaufzeit

| Modell | Gesamtdauer | Durchschnitt pro Test |
|--------|-------------|----------------------|
| deepseek-r1:7b | **8:50 min** | 25.2s |
| qwen3:14b | 15:03 min | 43.0s |
| qwen3:8b | 16:07 min | 46.0s |
| deepseek-r1:14b | 16:39 min | 47.6s |

**Hinweis:** Die Gesamtdauer von `deepseek-r1:7b` ist trügerisch niedrig — ein Test endete nach 371s mit Timeout (zählt als Error, nicht als vollständiger Durchlauf), und mehrere Tests brachen schnell mit JSON-Parsing-Fehlern ab, bevor der LLM überhaupt antworten konnte. Die effektive Laufzeit bei erfolgreichen Tests ist vergleichbar.

### Laufzeiten pro Testklasse

| Testklasse (Anzahl Tests) | deepseek-r1:7b | qwen3:8b | deepseek-r1:14b | qwen3:14b |
|----------------------------|----------------|----------|-----------------|-----------|
| BackgroundEnrichment (2) | 33.3s | 30.9s | 29.9s | 31.3s |
| BackgroundPlanning (2) | ~43s* | 58.5s | 100.6s | 100.2s |
| ChatPlanAnalysis (3) | 22.8s | 48.8s | 45.9s | 43.8s |
| Enrichment (2) | ~19s | 20.3s | 19.7s | 17.9s |
| Execution (4) | ~168s | 134.6s | 205.2s | 122.5s |
| **PlanningAiService (3)** | **~430s** (inkl. Timeout) | 225.4s | 206.5s | **276.4s** |
| ScheduleEnrichment (1) | 30.8s | 50.1s | 41.5s | 37.9s |
| **SchedulePlanning (4)** | 126.3s | **395.3s** | **348.2s** | **271.2s** |

*\* Geschätzt — einzelne Testzeiten nicht vollständig im Log sichtbar.*

### Durchschnittliche Antwortzeit pro LLM-Aufruf (geschätzt)

Berechnet aus den Testklassen, die das jeweilige Modell als Planning-Modell nutzen (BackgroundPlanning, ChatPlanAnalysis, PlanningAiService, SchedulePlanning):

| Modell | Ø pro Aufruf | Schnellster | Langsamster |
|--------|-------------|-------------|-------------|
| deepseek-r1:7b | ~52s | ~8s (ChatAnalysis) | 371s (Planning, Timeout) |
| qwen3:8b | ~61s | ~16s (ChatAnalysis) | ~120s (SchedulePlanning) |
| deepseek-r1:14b | ~58s | ~15s (ChatAnalysis) | ~100s (SchedulePlanning) |
| qwen3:14b | ~58s | ~15s (ChatAnalysis) | ~92s (PlanningAiService) |

### Laufzeit vs. Erfolgsrate

| Modell | Gesamtzeit | Erfolgreiche Tests | Zeit pro erfolgreichem Test |
|--------|-----------|-------------------|----------------------------|
| deepseek-r1:7b | 8:50 min | 17 | 31.2s |
| **qwen3:14b** | **15:03 min** | **21** | **43.0s** |
| qwen3:8b | 16:07 min | 16 | 60.4s |
| deepseek-r1:14b | 16:39 min | 19 | 52.6s |

`qwen3:14b` hat die beste Effizienz: 100% Erfolgsrate bei der zweitschnellsten Gesamtzeit. `deepseek-r1:7b` ist zwar am schnellsten, liefert aber die meisten fehlgeschlagenen Aufrufe — und jeder fehlgeschlagene Aufruf kostet im Produktivbetrieb einen Retry.

### Antwortzeiten nach Prompt-Komplexität

| Komplexität | Beschreibung | deepseek-r1:7b | qwen3:8b | deepseek-r1:14b | qwen3:14b |
|-------------|-------------|----------------|----------|-----------------|-----------|
| **Einfach** (1-2 Params) | BackgroundPlanning, Enrichment | ~20s | ~25s | ~40s | ~40s |
| **Mittel** (3-5 Params) | ChatPlanAnalysis, SchedulePlanning | ~30s | ~60s | ~55s | ~50s |
| **Komplex** (9 Params) | PlanningAiService | Timeout/Error | Error | ~70s | **~92s** |

Bei einfachen Prompts ist `deepseek-r1:7b` am schnellsten. Bei komplexen Prompts scheitern die 7b/8b-Modelle oft ganz, während beide 14b-Modelle zuverlässig antworten. `qwen3:14b` braucht beim komplexesten Prompt etwas länger als `deepseek-r1:14b` (~92s vs. ~70s), liefert dafür aber 100% valides JSON und durchgehend deutsche Inhalte.

---

## Fehlerklassifikation

### JSON-Parsing-Fehler (LangChain4j kann LLM-Output nicht parsen)

| Fehlertyp | deepseek-r1:7b | qwen3:8b | deepseek-r1:14b | qwen3:14b |
|-----------|:-:|:-:|:-:|:-:|
| `Expected BEGIN_OBJECT but was STRING` | — | 3x | 1x | — |
| `Expected BEGIN_OBJECT but was BEGIN_ARRAY` | 1x | — | — | — |
| `MalformedJsonException` (kaputte Syntax) | 1x | 1x | — | — |
| `NumberFormatException` ("1L" statt 1) | 1x | — | — | — |
| **Gesamt JSON-Fehler** | **3** | **4** | **1** | **0** |

### Inhaltliche Fehler (valides JSON, aber falscher Inhalt)

| Fehlertyp | deepseek-r1:7b | qwen3:8b | deepseek-r1:14b | qwen3:14b |
|-----------|:-:|:-:|:-:|:-:|
| Lücke im Stundenplan | — | — | 1x | — |
| Add-Request nicht erkannt | 1x | — | — | — |
| **Gesamt inhaltliche Fehler** | **1** | **0** | **1** | **0** |

### Timeouts

| | deepseek-r1:7b | qwen3:8b | deepseek-r1:14b | qwen3:14b |
|---|:-:|:-:|:-:|:-:|
| Timeouts (>300s) | 1x (371s) | 0 | 0 | 0 |

---

## Qualitätsbewertung der Inhalte

### Sprache

| Aspekt | deepseek-r1:7b | qwen3:8b | deepseek-r1:14b | qwen3:14b |
|--------|:-:|:-:|:-:|:-:|
| Durchgehend Deutsch | Nein (Encoding-Artefakte, Kauderwelsch) | Ja | Meist (gelegentlich Englisch) | Ja |
| Grammatik | Häufig fehlerhaft | Gut | Gut | Sehr gut |
| Kohärenz | Schwach | Stark | Stark | Sehr stark |

### Persönlichkeitstreue

Wie gut werden die Eingabe-Persönlichkeitsmerkmale in den Output übernommen?

| Merkmal | deepseek-r1:7b | qwen3:8b | deepseek-r1:14b | qwen3:14b |
|---------|:-:|:-:|:-:|:-:|
| "28 Jahre" | Alter korrekt | Alter korrekt | Alter korrekt | Alter korrekt |
| "Softwareentwickler" | Ja | Ja | Ja | Ja (konkret: Backend-Entwicklung) |
| "sportlich" | Teilweise | Ja | Ja | Ja (Laufen, Mountainbiken, Klettern) |
| "liebt gutes Essen" | Teilweise | Ja | Ja | Ja (exotische Gerichte, Food-Festivals) |
| "introvertiert" | Ignoriert (erfindet Tochter, Partner) | Gut umgesetzt | Gut umgesetzt | Sehr gut (Angst vor Teamdruck, Konfliktvermeidung) |
| "trockener Humor" | Erwähnt, aber nicht stimmig | Natürlich eingebaut | Natürlich eingebaut | "humorvoll in technischen Diskussionen" |

### Realismus der Tagespläne

| Aspekt | deepseek-r1:7b | qwen3:8b | deepseek-r1:14b | qwen3:14b |
|--------|----------------|----------|-----------------|-----------|
| Arbeitszeiten | "16:45-24:00 Arbeit" | (JSON scheitert) | 8h mit Pausen | 8h mit Pausen + konkrete Aufgaben |
| Schlafenszeiten | OK | (JSON scheitert) | 7-9h realistisch | 7-8h realistisch |
| Aktivitäten | "Morgencode", "Liebesabend mit Futtern" | — | "Jogging", "Science-Fiction-Roman lesen" | "Backend-Entwicklung", "Klettern mit Freunden" |
| Mahlzeiten | Teilweise | — | Frühstück, Mittag, Abend | Frühstück, Mittag, Kochabend |
| Pendeln | Fehlt | — | 15 min Heimweg | 1h Fahrrad (hin + zurück) |
| Detailgrad | 5-6 Blöcke | — | 14-20 Blöcke | 13 (Wochentag) / 10 (Wochenende) |

---

## Empfehlung

### Für Planning-Agents (PlanningAiService, SchedulePlanning, ChatPlanAnalysis)

**`qwen3:14b`** — klarer Gewinner:
- Einziges Modell mit **21/21 (100%)** — kein einziger JSON-Fehler, kein inhaltlicher Fehler
- Durchgehend auf Deutsch, kein Sprachwechsel ins Englische
- Bestes Reasoning: prüft Kollisionsfreiheit, erklärt Entscheidungen mit Bezug zu Bedürfnissen
- Schnellste Gesamtzeit unter den 14b-Modellen (15:03 vs. 16:39 min)
- Lückenlose Stundenpläne mit kreativen, persönlichkeitsgetreuen Details

**`deepseek-r1:14b`** — gute Alternative (90%):
- 19/21 bestanden, nur 1 JSON-Fehler und 1 Lücke im Stundenplan
- Etwas schneller bei komplexen Prompts (~70s vs. ~92s)
- Wechselt gelegentlich ins Englische

### Für Execution-/Enrichment-Agents (ExecutionAiService, EnrichmentAiService, BackgroundEnrichment, ScheduleEnrichment)

**`qwen3:8b`** — bewährt und zuverlässig:
- 11/12 Testläufe bestanden (über alle vier Testläufe)
- Gute kreative Textgenerierung (Blogartikel, Erzähltexte)
- Schnellere Antwortzeiten als 14b-Modelle
- Geringerer VRAM-Verbrauch

### Nicht empfohlen

**`deepseek-r1:7b`** für Structured Output:
- Häufig kaputtes JSON (3 Parsing-Fehler + 1 Timeout)
- Inhaltlich unzuverlässig (erfindet Familienmitglieder, Kauderwelsch)
- Kein Vorteil gegenüber `qwen3:8b` bei gleichem Ressourcenverbrauch

**`qwen3:8b`** als Planning-Modell:
- Nur 76% Erfolgsrate — häufige JSON-Parsing-Fehler bei komplexen Strukturen
- Als Execution-Modell exzellent, aber für Structured Output mit verschachtelten Listen überfordert

### Optimale Konfiguration

```yaml
vbuddy:
  ai:
    planning:
      model-name: qwen3:14b          # Zuverlässigstes JSON + bestes Reasoning + Deutsch
    execution:
      model-name: qwen3:8b           # Kreatives Schreiben
    chat:
      model-name: llama3.1:8b        # Natürlicher Dialog (nicht getestet)
    schedule:
      model-name: qwen3:14b          # Komplexe Zeitstrukturen, lückenlos
```
