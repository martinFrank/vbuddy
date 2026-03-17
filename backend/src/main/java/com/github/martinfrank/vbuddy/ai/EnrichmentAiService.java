package com.github.martinfrank.vbuddy.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface EnrichmentAiService {

    @SystemMessage("""
            Du bist ein kreativer Autor, der Aktivitätsbeschreibungen für einen VBuddy ausführlich und lebendig gestaltet.

            Deine Aufgabe:
            - Nimm die kurze Beschreibung einer geplanten Aktivität und schreibe sie zu einer detaillierten, lebendigen Beschreibung um
            - Berücksichtige die Persönlichkeit des VBuddy — der Text soll zu seinem Charakter passen
            - Nutze den Planungskontext und die Websuche-Ergebnisse, um konkrete Details einzubauen
            - Verwende echte Namen von Geschäften, Restaurants, Cafés oder Veranstaltungen aus den Suchergebnissen, wenn sie zur Aktivität passen
            - Integriere konkrete Informationen wie Adressen, Url, Angebote oder Besonderheiten aus den Suchergebnissen
            - Schreibe aus der Perspektive eines Erzählers, der den VBuddy beschreibt
            - Die Beschreibung soll 3-5 Sätze lang sein
            - Schreibe auf Deutsch
            """)
    @UserMessage("""
            Schreibe eine ausführliche Beschreibung für folgende Aktivität des VBuddy:

            **Persönlichkeit des VBuddy:** {{personality}}

            **Aktivität:** {{taskTitle}}
            **Ort:** {{taskLocation}}
            **Bisherige Kurzbeschreibung:** {{taskDescription}}
            **Dauer:** {{durationMinutes}} Minuten

            **Planungskontext (Reasoning des Planungs-Agents):**
            {{planningContext}}

            **Lokale Veranstaltungen, Geschäfte und Aktivitäten (Websuche):**
            {{localActivities}}
            """)
    EnrichedTask enrichTask(
            @V("personality") String personality,
            @V("taskTitle") String taskTitle,
            @V("taskLocation") String taskLocation,
            @V("taskDescription") String taskDescription,
            @V("durationMinutes") int durationMinutes,
            @V("planningContext") String planningContext,
            @V("localActivities") String localActivities
    );
}
