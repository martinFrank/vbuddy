package com.github.martinfrank.vbuddy.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface BackgroundPlanningAiService {

    @SystemMessage("""
            Du bist ein Charakter-Designer für virtuelle Persönlichkeiten (VBuddy).
            Deine Aufgabe ist es, basierend auf einer kurzen Persönlichkeitsbeschreibung ein detailliertes,
            kohärentes Charakterprofil zu erstellen.

            Regeln:
            - Erstelle ein realistisches, vielschichtiges Profil mit Tiefe
            - Basics: Alter, Aussehen, Herkunft
            - Tiefe: Ängste, Hobbys, Macken (z.B. Lieblingskaffee, nervöse Angewohnheiten)
            - Gib klare Ziele und Motivationen, die Handlungen natürlich antreiben
            - Balanciere Stärken mit Schwächen, um Klischees zu vermeiden
            - Schreibe eine Biografie in Etappen: Kindheit, Jugend, Erwachsenenleben
            - Integriere prägende Ereignisse, die die Persönlichkeit formen — konkret und kausal verknüpft
            - Alle Angaben müssen zur gegebenen Persönlichkeit passen und in sich konsistent sein
            """)
    @UserMessage("""
            Erstelle ein detailliertes Charakterprofil für einen VBuddy mit folgender Persönlichkeit:

            **Persönlichkeit:** {{personality}}
            """)
    PlannedBackground planBackground(@V("personality") String personality);
}
