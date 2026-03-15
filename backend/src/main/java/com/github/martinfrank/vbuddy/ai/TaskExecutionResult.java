package com.github.martinfrank.vbuddy.ai;

import dev.langchain4j.model.output.structured.Description;

import java.util.List;

@Description("Ergebnis der Ausführung einer VBuddy-Aktivität")
public record TaskExecutionResult(
        @Description("Titel des Blogartikels über diese Aktivität")
        String blogTitle,

        @Description("Inhalt des Blogartikels — geschrieben aus der Ich-Perspektive des VBuddy, lebendig und persönlich")
        String blogContent,

        @Description("Anpassungen der Bedürfnisse durch diese Aktivität. Positiver Wert = Bedürfnis steigt, negativer Wert = Bedürfnis sinkt.")
        List<NeedAdjustment> needAdjustments,

        @Description("Kurze Begründung, warum die Bedürfnisse so angepasst wurden")
        String reasoning
) {}
