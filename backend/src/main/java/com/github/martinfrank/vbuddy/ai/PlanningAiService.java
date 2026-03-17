package com.github.martinfrank.vbuddy.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface PlanningAiService {

    @SystemMessage("""
            Du bist der interne Planungs-Agent eines virtuellen Buddys (VBuddy).
            Deine Aufgabe ist es, die nächsten Aktivitäten des VBuddy zu planen.

            Regeln:
            - Plane realistische, konsistente Aktivitäten
            - Berücksichtige die aktuellen Bedürfnisse: hohe Werte bedeuten dringenden Bedarf
            - Berücksichtige den aktuellen Aufenthaltsort für logische Ortswechsel
            - Aktivitäten dürfen sich zeitlich nicht überlappen
            - Plane 3-5 Aktivitäten für die nächsten Stunden
            - Berücksichtige die Persönlichkeit des VBuddy
            - Berücksichtige die zuletzt erledigten Aktivitäten, um Wiederholungen zu vermeiden
            - Nutze lokale Veranstaltungen und Aktivitäten aus der Websuche als Inspiration, wenn sie zum VBuddy passen
            - Nutze Geschäfte, Restaurants und Cafés aus der Websuche als konkrete Orte für Aktivitäten (z.B. Einkaufen, Essen gehen, Kaffee trinken)
            - Verwende echte Namen von Geschäften, Restaurants oder Veranstaltungen aus den Suchergebnissen, wenn sie zur Aktivität passen
            - Verwende das Datumsformat 'yyyy-MM-dd HH:mm' für Startzeiten
            """)
    @UserMessage("""
            Plane die nächsten Aktivitäten für den VBuddy.

            **Persönlichkeit:** {{personality}}

            **Aktueller Ort:** {{currentLocation}}

            **Aktuelle Uhrzeit:** {{currentTime}}

            **Aktuelle Bedürfnisse (0=kein Bedarf, 100=maximaler Bedarf):**
            {{needs}}

            **Zuletzt erledigte Aktivitäten:**
            {{recentTasks}}

            **Lokale Veranstaltungen, Geschäfte und Aktivitäten (Websuche):**
            {{localActivities}}

            **Historischer Kontext (Hintergrundgeschichte und vergangene Erlebnisse):**
            {{historicalContext}}
            """)
    PlannedTasks planTasks(
            @V("personality") String personality,
            @V("currentLocation") String currentLocation,
            @V("currentTime") String currentTime,
            @V("needs") String needs,
            @V("recentTasks") String recentTasks,
            @V("localActivities") String localActivities,
            @V("historicalContext") String historicalContext
    );
}
