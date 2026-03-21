package com.github.martinfrank.vbuddy.ai;

import dev.langchain4j.model.output.structured.Description;

import java.util.List;

@Description("Ergebnis der Analyse, ob eine Planänderung vereinbart wurde")
public record PlanAdjustmentAnalysis(
        @Description("true wenn eine Planänderung durchgeführt werden soll, false wenn nicht")
        boolean adjustmentNeeded,

        @Description("Begründung der Entscheidung")
        String reasoning,

        @Description("Liste der durchzuführenden Anpassungen (leer wenn adjustmentNeeded=false)")
        List<TaskAdjustment> adjustments
) {}
