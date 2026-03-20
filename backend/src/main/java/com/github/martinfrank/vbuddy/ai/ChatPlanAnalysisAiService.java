package com.github.martinfrank.vbuddy.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ChatPlanAnalysisAiService {

    @SystemMessage("""
            Du analysierst Chat-Konversationen eines VBuddy mit seinem Nutzer.
            Prüfe, ob der Nutzer eine Änderung am Tagesplan vorschlägt UND ob der VBuddy
            dem zugestimmt hat (in seiner letzten Antwort).

            Regeln:
            - Nur wenn der Nutzer explizit eine Planänderung vorschlägt (neuer Task, Task absagen, Task ändern)
            - UND der VBuddy dem in seiner Antwort zugestimmt hat oder es positiv aufgenommen hat
            - Rückfragen des VBuddy = KEINE Anpassung (adjustmentNeeded=false)
            - Smalltalk, Fragen, allgemeines Gespräch = KEINE Anpassung
            - Die Änderungen müssen zur Persönlichkeit des VBuddy passen
            - Bei CANCEL und UPDATE muss existingTaskId die ID des betroffenen Tasks sein
            - Bei UPDATE und ADD müssen title, description, location, startTime und durationMinutes gesetzt sein
            - Verwende das Datumsformat 'yyyy-MM-dd HH:mm' für Startzeiten
            """)
    @UserMessage("""
            Analysiere ob eine Planänderung vereinbart wurde.

            **Persönlichkeit:** {{personality}}

            **Aktuelle geplante Tasks:**
            {{plannedTasks}}

            **Letzte Chat-Nachrichten:**
            {{recentMessages}}
            """)
    PlanAdjustmentAnalysis analyzeChatForPlanAdjustment(
            @V("personality") String personality,
            @V("plannedTasks") String plannedTasks,
            @V("recentMessages") String recentMessages
    );
}
