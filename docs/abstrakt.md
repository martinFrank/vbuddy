# VBuddy — Abstrakt

**VBuddy** ist eine KI-gesteuerte Chat-Applikation, in der ein virtueller Freund (Virtual Buddy) ein eigenständiges, simuliertes Leben führt. Das Projekt demonstriert den durchgängigen Aufbau einer modernen, KI-integrierten Anwendung — von der Softwarearchitektur über das Deployment bis zur Qualitätssicherung.

## Kernkonzept

Ein VBuddy besitzt eine eigene Persönlichkeit, dynamische Bedürfnisse (Hunger, Langeweile, Wissensdurst, Bewegungsdrang, soziale Interaktion) und einen aktuellen Aufenthaltsort. Er plant seinen Tag autonom, führt Aktivitäten aus, verfasst Blogartikel in der Ich-Perspektive und reagiert kontextbezogen auf Chat-Nachrichten des Nutzers.

## Architektur-Highlights

- **Multi-Agent-Orchestrierung** — Mehrstufige Agenten-Pipelines (4-stufige Background-Pipeline, 3-stufige Task-Pipeline), in denen spezialisierte LLM-Aufrufe sequenziell aufeinander aufbauen und Zwischenergebnisse als Kontext weitergeben.

- **Autonome Tagesplanung** — Ein zeitgesteuerter Lifecycle-Loop (`@Scheduled`, 60s) orchestriert Planung, Ausführung, Blog-Veröffentlichung und Bedürfnis-Anpassung vollautomatisch für jeden Buddy.

- **Interaktive Planänderung via Chat** — Nutzer können den Tagesplan über natürliche Sprache beeinflussen. Ein nachgelagerter Analyse-Agent erkennt vereinbarte Planänderungen (CANCEL, UPDATE, ADD) und wendet sie an — mit Schutz vor False Positives.

- **Retrieval Augmented Generation (RAG)** — pgvector-Embeddings (768d) auf zwei Ebenen (Hintergrundgeschichte + Tasks) dienen als semantisches Langzeitgedächtnis. Der Kontext fließt sowohl in die Tagesplanung (Top 10) als auch in Chat-Antworten (Top 5) ein.

- **Aufgabenspezifische Modellstrategie** — Gezielte LLM-Selektion je Aufgabenprofil: Reasoning-Modell für Planung (deepseek-r1), kreatives Modell für Textgenerierung (qwen3), konversationelles Modell für Dialog (llama3.1). Alle Ausgaben als typisierte Java-Records via LangChain4j Structured Output.

- **Tool-Augmented Generation** — SearxNG-Metasuchmaschine als Kontextquelle für lokale Events, Geschäfte und Bilder.

- **Systemintegration** — Automatische Blog-Veröffentlichung auf WordPress via REST API v2, inklusive Bildersuche und Gutenberg-Block-Integration.

## Qualitätssicherung

Dreistufige Teststrategie: Unit-Tests mit gemockten AI-Interfaces, LLM-Integrationstests zur Validierung von Output-Struktur und Plausibilität, sowie Service-Integrationstests für externe Anbindungen. Die Interface-basierte AI-Schicht ermöglicht Tests auf jeder Ebene ohne Spring-Context oder Datenbankabhängigkeit.

## Technologie-Stack

Java 21, Spring Boot 3.4, LangChain4j, React 19, TypeScript, Vite, PostgreSQL 17 mit pgvector, Flyway, Docker Compose, Nginx, SearxNG, Ollama.

## Betriebsmodell

Vollständiger On-Premise-Betrieb mit selbst gehosteten LLMs über Ollama (OpenAI-kompatible API). Containerisierung via Docker Compose mit Multi-Stage Builds, Netzwerk-Isolation und Healthchecks. Session-basierte Authentifizierung mit Spring Security und rollenbasierter Zugriffskontrolle (ADMIN/USER).
