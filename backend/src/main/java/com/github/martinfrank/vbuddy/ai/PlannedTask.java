package com.github.martinfrank.vbuddy.ai;

import dev.langchain4j.model.output.structured.Description;

public record PlannedTask(
        @Description("Titel der Aktivität, z.B. 'Frühstück zubereiten', 'Spaziergang im Park'")
        String title,

        @Description("Detaillierte Beschreibung, was der VBuddy bei dieser Aktivität macht und erlebt")
        String description,

        @Description("Ort, an dem die Aktivität stattfindet, z.B. 'Zu Hause', 'Im Park', 'Im Restaurant'")
        String location,

        @Description("Startzeit im Format 'yyyy-MM-dd HH:mm'")
        String startTime,

        @Description("Dauer der Aktivität in Minuten")
        int durationMinutes,

        @Description("URL der Websuche-Quelle, falls die Aktivität auf einem Suchergebnis basiert. Null, wenn keine Quelle verwendet wurde.")
        String sourceUrl
) {}
