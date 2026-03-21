package com.github.martinfrank.vbuddy.ai;

import dev.langchain4j.model.output.structured.Description;

@Description("Eine einzelne Anpassung am Tagesplan")
public record TaskAdjustment(
        @Description("Art der Anpassung: CANCEL (Task absagen), UPDATE (Task ändern), ADD (neuen Task hinzufügen)")
        String action,

        @Description("ID des bestehenden Tasks für CANCEL/UPDATE (null bei ADD)")
        Long existingTaskId,

        @Description("Titel der Aktivität")
        String title,

        @Description("Beschreibung der Aktivität")
        String description,

        @Description("Ort der Aktivität")
        String location,

        @Description("Startzeit im Format 'yyyy-MM-dd HH:mm'")
        String startTime,

        @Description("Dauer in Minuten")
        int durationMinutes
) {}
