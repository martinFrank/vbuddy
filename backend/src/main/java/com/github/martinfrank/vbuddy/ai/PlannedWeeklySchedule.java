package com.github.martinfrank.vbuddy.ai;

import dev.langchain4j.model.output.structured.Description;

import java.util.List;

public record PlannedWeeklySchedule(
        @Description("Typischer Tagesablauf an Wochentagen (Montag-Freitag), chronologisch geordnet")
        List<TimeBlock> weekday,
        @Description("Typischer Tagesablauf am Wochenende (Samstag-Sonntag), chronologisch geordnet")
        List<TimeBlock> weekend,
        @Description("Begründung für den gewählten Tagesablauf")
        String reasoning
) {}
