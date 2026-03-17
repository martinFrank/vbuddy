package com.github.martinfrank.vbuddy.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface SchedulePlanningAiService {

    @SystemMessage("""
            Du bist ein Tagesplaner für virtuelle Persönlichkeiten (VBuddy).
            Deine Aufgabe ist es, einen realistischen, typischen Wochenstundenplan zu erstellen,
            der das reguläre Alltagsverhalten des VBuddy widerspiegelt.

            Regeln:
            - Erstelle zwei Tagesabläufe: einen für Wochentage (Mo-Fr) und einen für das Wochenende (Sa-So)
            - Der Plan muss die vollen 24 Stunden abdecken (00:00 bis 00:00), inklusive Schlafenszeit
            - Der erste Block beginnt bei 00:00, der letzte Block endet bei 00:00 (nächster Tag)
            - Berücksichtige: Schlaf, Morgenroutine, Arbeit/Studium/Beschäftigung, Pendeln, Mahlzeiten, Freizeit, Erholung
            - Der Plan muss zur Persönlichkeit und zur Hintergrundgeschichte passen
            - Wochentage sollen strukturierter sein (Arbeit, Pflichten)
            - Wochenenden sollen freier und erholsamer sein
            - Verwende das Zeitformat HH:mm
            - Zeitblöcke dürfen keine Lücken haben und sich nicht überlappen
            - Plane realistisch: z.B. 7-8h Schlaf, 8h Arbeit an Wochentagen, Pausen
            """)
    @UserMessage("""
            Erstelle einen typischen Wochenstundenplan für den VBuddy.

            **Persönlichkeit:** {{personality}}

            **Hintergrundgeschichte:**
            {{background}}
            """)
    PlannedWeeklySchedule planSchedule(
            @V("personality") String personality,
            @V("background") String background
    );
}
