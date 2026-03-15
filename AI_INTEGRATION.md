# AI-Integration — VBuddy

Dieses Dokument beschreibt die Details der LLM- und AI-Integration des VBuddy-Projekts.

## LLM-Provider

- **Provider**: Ollama (selfhosted)
- **Base-URL**: `http://localhost:11434/v1` (OpenAI-kompatible API)
- **Chat-Modell**: _TODO — z.B. llama3, mistral, gemma2_
- **Embedding-Modell**: _TODO — z.B. nomic-embed-text_
- **Integration**: LangChain4j (OpenAI-kompatibler Client)

## Agenten-Architektur

Die Tätigkeiten des VBuddy werden durch zwei spezialisierte Agenten gesteuert:

### Planungs-Agent

- **Aufgabe**: Plant die Tätigkeiten des VBuddy (Tagesplan erstellen und anpassen)
- **Input**: Aktuelle Bedürfnisse, Persönlichkeit, Aufenthaltsort, bisherige Aktivitäten des Tages
- **Output**: Geordnete Liste von Aktivitäten mit Zeitfenstern und Orten
- _TODO: Wann wird der Planungs-Agent ausgelöst (einmal täglich, bei Bedürfnis-Schwellenwert, ...)?_
- _TODO: Kann der Nutzer über den Chat den Plan beeinflussen?_

### Ausführungs-Agent

- **Aufgabe**: Führt die geplanten Tätigkeiten aus (Status-Updates, Bedürfnis-Anpassungen, Ortswechsel, Blog-Erstellung)
- **Input**: Aktuelle Aktivität aus dem Tagesplan, aktueller Zustand des VBuddy
- **Output**: Aktualisierte Bedürfnisse, neuer Aufenthaltsort, ggf. Blogartikel, Aktivitäts-Status-Update
- _TODO: Wie wird der Zeitablauf simuliert (Echtzeit, beschleunigt, ...)?_
- _TODO: Welche Nebeneffekte hat die Ausführung einer Aktivität?_

### Zusammenspiel

1. Der **Planungs-Agent** erstellt den Tagesplan
2. Der **Ausführungs-Agent** arbeitet die Aktivitäten der Reihe nach ab
3. Nach Abschluss einer Aktivität aktualisiert der Ausführungs-Agent den Zustand (Bedürfnisse, Ort, Status)
4. Bei Bedarf kann der Planungs-Agent den restlichen Tagesplan anpassen (z.B. durch Nutzer-Interaktion im Chat)
5. Alle Entscheidungen beider Agenten werden im AI-Decision-Log protokolliert

## Chat

_TODO: Details zur Chat-Integration_

- Welche Kontext-Informationen fließen in den Chat-Prompt ein?
- Wie viel Chat-History wird mitgegeben?
- Wird RAG für den Chat verwendet?
- System-Prompt-Aufbau

## Tagesplan-Generierung

_TODO: Details zur Tagesplan-Generierung_

- Wann und wie oft wird der Tagesplan generiert?
- Zeitraster (z.B. Stunden-Slots, frei wählbare Zeiträume)
- Welche Informationen fließen in die Planung ein?
- Output-Format (strukturiertes JSON, Freitext, ...)

## Blog-Generierung

- **Veröffentlichung**: Über **Browser Use** auf `https://elitegames.v6.rocks/vbuddy-blog/`
- _TODO: Wann werden Blogartikel erstellt (nach jeder Aktivität, am Ende des Tages, ...)?_
- _TODO: Stil und Tonalität der Artikel_
- _TODO: Browser-Use-Konfiguration und Ablauf_

## Bedürfnissystem

_TODO: Details zum Bedürfnissystem_

- Welche Bedürfnisse gibt es (aktuell: HUNGER, BOREDOM, KNOWLEDGE, EXERCISE, SOCIAL)?
- Decay-Rate: Wie schnell steigen Bedürfnisse über die Zeit?
- Welche Aktivitäten beeinflussen welche Bedürfnisse und wie stark?
- Schwellenwerte und deren Auswirkung auf Stimmung/Verhalten

## Persönlichkeit

_TODO: Details zur Persönlichkeitsdefinition_

- Wie wird die Persönlichkeit des VBuddy definiert (Freitext, Attribute, ...)?
- Wie beeinflusst die Persönlichkeit Chat-Antworten und Entscheidungen?

## Aufenthaltsort

_TODO: Details zum Aufenthaltsort_

- Wie wird der Ort bestimmt (aus Aktivität abgeleitet, eigene Entscheidung, ...)?
- Welche Orte stehen zur Verfügung (frei, vordefinierte Liste, ...)?
- Wie beeinflusst der Ort Chat-Antworten und Tagesplanung?

## RAG (Retrieval Augmented Generation)

- **Embedding-Store**: pgvector (PostgreSQL Extension)
- _TODO: Welche Inhalte werden eingebettet (Chat-History, Aktivitäten, Blogartikel, ...)?_
- _TODO: Wie wird RAG in die verschiedenen LLM-Aufrufe eingebunden?_

## AI-Decision-Log

- Alle LLM-gestützten Entscheidungen werden in `ai_decision_log` protokolliert
- Felder: Kontext, Entscheidung, Begründung, Zeitstempel
- _TODO: Welche Entscheidungen werden geloggt (Tagesplanung, Bedürfnis-Updates, Ortswechsel, ...)?_
