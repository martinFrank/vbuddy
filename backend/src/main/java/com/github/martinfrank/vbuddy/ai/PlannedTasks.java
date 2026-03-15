package com.github.martinfrank.vbuddy.ai;

import dev.langchain4j.model.output.structured.Description;

import java.util.List;

@Description("Liste der geplanten Aktivitäten für den VBuddy")
public record PlannedTasks(
        @Description("Die geplanten Aktivitäten in chronologischer Reihenfolge")
        List<PlannedTask> tasks,

        @Description("Kurze Begründung, warum diese Aktivitäten gewählt wurden")
        String reasoning
) {}
