package com.github.martinfrank.vbuddy.ai;

import dev.langchain4j.model.output.structured.Description;

public record NeedAdjustment(
        @Description("Art des Bedürfnisses: HUNGER, BOREDOM, KNOWLEDGE, EXERCISE, SOCIAL")
        String needType,

        @Description("Änderungswert (z.B. -20 wenn das Bedürfnis gestillt wird, +10 wenn es steigt)")
        double change
) {}
