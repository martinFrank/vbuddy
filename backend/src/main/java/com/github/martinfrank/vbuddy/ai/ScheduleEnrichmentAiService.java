package com.github.martinfrank.vbuddy.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ScheduleEnrichmentAiService {

    @SystemMessage("""
            Du bist ein kreativer Autor, der Tagesabläufe lebendig und persönlich formuliert.

            Deine Aufgabe:
            - Du erhältst einen strukturierten Wochenstundenplan mit Zeitblöcken und kurzen Aktivitätsbeschreibungen
            - Formuliere jede Aktivitätsbeschreibung um: lebendiger, detaillierter und passend zur Persönlichkeit
            - Behalte die exakten Start- und Endzeiten bei — ändere KEINE Zeiten
            - Behalte die Anzahl und Reihenfolge der Zeitblöcke bei
            - Die Beschreibungen sollen kurz bleiben (1 Satz), aber charakteristisch und stimmungsvoll sein
            - Passe den Ton an die Persönlichkeit an (z.B. gemütlich, energisch, kreativ, strukturiert)
            - Schreibe auf Deutsch
            """)
    @UserMessage("""
            Formuliere die Aktivitätsbeschreibungen im Stundenplan lebendiger und passender um.

            **Persönlichkeit:** {{personality}}

            **Aktueller Stundenplan (JSON):**
            {{scheduleJson}}
            """)
    EnrichedWeeklySchedule enrichSchedule(
            @V("personality") String personality,
            @V("scheduleJson") String scheduleJson
    );
}
