package com.github.martinfrank.vbuddy.ai;

import dev.langchain4j.model.output.structured.Description;

import java.util.List;

public record EnrichedWeeklySchedule(
        @Description("Zeitblöcke für Wochentage (Mo-Fr) mit verbesserten Beschreibungen, chronologisch von 00:00 bis 00:00")
        List<TimeBlock> weekday,
        @Description("Zeitblöcke für Wochenende (Sa-So) mit verbesserten Beschreibungen, chronologisch von 00:00 bis 00:00")
        List<TimeBlock> weekend
) {}
