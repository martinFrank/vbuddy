package com.github.martinfrank.vbuddy.ai;

import dev.langchain4j.model.output.structured.Description;

public record TimeBlock(
        @Description("Startzeit im Format HH:mm")
        String start,
        @Description("Endzeit im Format HH:mm")
        String end,
        @Description("Kurze Beschreibung der Aktivität, z.B. 'Arbeit im Büro', 'Mittagspause', 'Pendeln'")
        String activity
) {}
