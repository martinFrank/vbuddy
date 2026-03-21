package com.github.martinfrank.vbuddy.ai;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.github.martinfrank.vbuddy.ai.LlmTestConfig.*;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("llm")
class BackgroundEnrichmentAiServiceLlmTest {

    private static BackgroundEnrichmentAiService backgroundEnrichmentAiService;

    @BeforeAll
    static void setUp() {
        backgroundEnrichmentAiService = buildService(BackgroundEnrichmentAiService.class, executionModel());
    }

    @Test
    void enrichBackground_returnsNarrativeText() {
        String structuredData = """
                Alter: 28
                Aussehen: Mittelgroß, braune Haare, grüne Augen, sportliche Figur
                Herkunft: Geboren und aufgewachsen in Nürnberg
                Hobbys: Joggen, Klettern, Kochen, Science-Fiction lesen
                Macken: Trinkt Kaffee nur schwarz, sortiert Bücher nach Farbe
                Ängste: Höhenangst trotz Klettern, Angst vor Langeweile
                Ziele: Senior Developer werden, einen Marathon laufen
                Stärken: Analytisches Denken, Ausdauer, Kreativität beim Kochen
                Schwächen: Ungeduldig, introvertiert in großen Gruppen
                Kindheit: Wuchs im Vorort auf, bastelte früh an Computern
                Jugend: Entdeckte Sport als Ausgleich, gewann Informatik-Wettbewerb
                Erwachsenenleben: Arbeitet als Backend-Entwickler, lebt allein in einer Altbauwohnung""";

        EnrichedBackground result = backgroundEnrichmentAiService.enrichBackground(
                TEST_PERSONALITY,
                structuredData
        );

        System.out.println(result.narrativeText());

        assertThat(result).isNotNull();
        assertThat(result.narrativeText()).isNotBlank();
        assertThat(result.narrativeText().length())
                .as("Narrative should be substantial (3-5 paragraphs)")
                .isGreaterThan(500);
    }

    @Test
    void enrichBackground_isWrittenInThirdPerson() {
        String structuredData = """
                Alter: 28
                Aussehen: Sportlich, braune Haare
                Herkunft: Nürnberg
                Hobbys: Joggen, Kochen
                Macken: Sortiert alles alphabetisch
                Ängste: Höhenangst
                Ziele: Marathon laufen
                Stärken: Ausdauer
                Schwächen: Ungeduldig
                Kindheit: Normales Aufwachsen
                Jugend: Sport entdeckt
                Erwachsenenleben: Softwareentwickler""";

        EnrichedBackground result = backgroundEnrichmentAiService.enrichBackground(
                TEST_PERSONALITY,
                structuredData
        );

        System.out.println(result.narrativeText());

        String text = result.narrativeText().toLowerCase();
        // Third person should use "er", "sein", "ihm" etc., not "ich"
        boolean usesThirdPerson = text.contains(" er ") || text.contains(" sein") || text.contains(" ihm ");
        assertThat(usesThirdPerson)
                .as("Narrative should be in third person")
                .isTrue();
    }
}
