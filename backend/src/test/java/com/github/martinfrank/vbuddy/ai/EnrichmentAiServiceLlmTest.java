package com.github.martinfrank.vbuddy.ai;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.github.martinfrank.vbuddy.ai.LlmTestConfig.*;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("llm")
class EnrichmentAiServiceLlmTest {

    private static EnrichmentAiService enrichmentAiService;

    @BeforeAll
    static void setUp() {
        enrichmentAiService = buildService(EnrichmentAiService.class, executionModel());
    }

    @Test
    void enrichTask_returnsDetailedDescription() {
        EnrichedTask result = enrichmentAiService.enrichTask(
                TEST_PERSONALITY,
                "Joggen im Stadtpark",
                "Stadtpark Nürnberg",
                "Max geht joggen.",
                45,
                "Max hat hohen Bewegungsdrang und braucht frische Luft nach einem langen Arbeitstag.",
                "Keine Suchergebnisse verfügbar"
        );

        assertThat(result).isNotNull();
        assertThat(result.enrichedDescription()).isNotBlank();
        assertThat(result.enrichedDescription().length())
                .as("Enriched description should be longer than original")
                .isGreaterThan("Max geht joggen.".length());
    }

    @Test
    void enrichTask_isWrittenInGerman() {
        EnrichedTask result = enrichmentAiService.enrichTask(
                TEST_PERSONALITY,
                "Kaffee trinken im Café",
                "Café am Hauptmarkt",
                "Max trinkt einen Kaffee.",
                30,
                "Pause nach der Arbeit, soziale Interaktion gewünscht.",
                "Keine Suchergebnisse verfügbar"
        );

        String desc = result.enrichedDescription().toLowerCase();
        boolean containsGermanWords = desc.contains("und") || desc.contains("der") ||
                desc.contains("die") || desc.contains("das") || desc.contains("ein");
        assertThat(containsGermanWords)
                .as("Description should be in German")
                .isTrue();
    }
}
