package com.github.martinfrank.vbuddy.ai;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.github.martinfrank.vbuddy.ai.LlmTestConfig.*;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("llm")
class BackgroundPlanningAiServiceLlmTest {

    private static BackgroundPlanningAiService backgroundPlanningAiService;

    @BeforeAll
    static void setUp() {
        backgroundPlanningAiService = buildService(BackgroundPlanningAiService.class, planningModel());
    }

    @Test
    void planBackground_returnsCompleteCharacterProfile() {
        PlannedBackground result = backgroundPlanningAiService.planBackground(TEST_PERSONALITY);

        assertThat(result).isNotNull();
        assertThat(result.alter()).as("Alter").isBetween(18, 80);
        assertThat(result.aussehen()).as("Aussehen").isNotBlank();
        assertThat(result.herkunft()).as("Herkunft").isNotBlank();
        assertThat(result.hobbys()).as("Hobbys").isNotBlank();
        assertThat(result.macken()).as("Macken").isNotBlank();
        assertThat(result.aengste()).as("Ängste").isNotBlank();
        assertThat(result.zieleUndMotivationen()).as("Ziele und Motivationen").isNotBlank();
        assertThat(result.staerken()).as("Stärken").isNotBlank();
        assertThat(result.schwaechen()).as("Schwächen").isNotBlank();
        assertThat(result.kindheit()).as("Kindheit").isNotBlank();
        assertThat(result.jugend()).as("Jugend").isNotBlank();
        assertThat(result.erwachsenenleben()).as("Erwachsenenleben").isNotBlank();
        assertThat(result.reasoning()).as("Reasoning").isNotBlank();
    }

    @Test
    void planBackground_ageIsConsistentWithPersonality() {
        // Personality says 28 years old
        PlannedBackground result = backgroundPlanningAiService.planBackground(TEST_PERSONALITY);

        assertThat(result.alter()).as("Age should be close to described 28")
                .isBetween(25, 35);
    }
}
