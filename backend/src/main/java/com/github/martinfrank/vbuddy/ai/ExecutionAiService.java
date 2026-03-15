package com.github.martinfrank.vbuddy.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ExecutionAiService {

    @SystemMessage("""
            Du bist der Ausführungs-Agent eines virtuellen Buddys (VBuddy).
            Deine Aufgabe ist es, eine geplante Aktivität auszuführen und das Ergebnis zu liefern.

            Du musst:
            1. Einen Blogartikel schreiben — aus der Ich-Perspektive des VBuddy, lebendig, persönlich und passend zur Persönlichkeit.
            2. Die Bedürfnisse anpassen — bestimme, welche Bedürfnisse durch die Aktivität beeinflusst werden.

            Regeln für Bedürfnis-Anpassungen:
            - Werte zwischen -30 und +10 pro Bedürfnis
            - Negativer Wert = Bedürfnis wird gestillt (z.B. Essen stillt HUNGER um -25)
            - Positiver Wert = Bedürfnis steigt (z.B. alleine lesen erhöht SOCIAL um +5)
            - Nicht jede Aktivität beeinflusst jedes Bedürfnis — nur relevante angeben
            - Verfügbare Bedürfnisse: HUNGER, BOREDOM, KNOWLEDGE, EXERCISE, SOCIAL
            """)
    @UserMessage("""
            Führe folgende Aktivität für den VBuddy aus:

            **Persönlichkeit:** {{personality}}

            **Aktivität:** {{taskTitle}}
            **Beschreibung:** {{taskDescription}}
            **Ort:** {{taskLocation}}
            **Dauer:** {{durationMinutes}} Minuten

            **Aktuelle Bedürfnisse (0=kein Bedarf, 100=maximaler Bedarf):**
            {{needs}}
            """)
    TaskExecutionResult executeTask(
            @V("personality") String personality,
            @V("taskTitle") String taskTitle,
            @V("taskDescription") String taskDescription,
            @V("taskLocation") String taskLocation,
            @V("durationMinutes") int durationMinutes,
            @V("needs") String needs
    );
}
