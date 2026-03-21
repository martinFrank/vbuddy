package com.github.martinfrank.vbuddy.ai;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.github.martinfrank.vbuddy.ai.LlmTestConfig.*;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("llm")
class ScheduleEnrichmentAiServiceLlmTest {

    private static ScheduleEnrichmentAiService scheduleEnrichmentAiService;

    @BeforeAll
    static void setUp() {
        scheduleEnrichmentAiService = buildService(ScheduleEnrichmentAiService.class, executionModel());
    }

    @Test
    void enrichSchedule_preservesTimesAndBlockCount() {
        String scheduleJson = """
                {
                  "weekday": [
                    {"start": "00:00", "end": "07:00", "activity": "Schlafen"},
                    {"start": "07:00", "end": "07:30", "activity": "Morgenroutine"},
                    {"start": "07:30", "end": "08:00", "activity": "Frühstück"},
                    {"start": "08:00", "end": "12:00", "activity": "Arbeit"},
                    {"start": "12:00", "end": "13:00", "activity": "Mittagspause"},
                    {"start": "13:00", "end": "17:00", "activity": "Arbeit"},
                    {"start": "17:00", "end": "18:00", "activity": "Sport"},
                    {"start": "18:00", "end": "19:00", "activity": "Abendessen"},
                    {"start": "19:00", "end": "22:00", "activity": "Freizeit"},
                    {"start": "22:00", "end": "00:00", "activity": "Schlafen"}
                  ],
                  "weekend": [
                    {"start": "00:00", "end": "09:00", "activity": "Schlafen"},
                    {"start": "09:00", "end": "10:00", "activity": "Frühstück"},
                    {"start": "10:00", "end": "13:00", "activity": "Freizeit"},
                    {"start": "13:00", "end": "14:00", "activity": "Mittagessen"},
                    {"start": "14:00", "end": "18:00", "activity": "Aktivitäten"},
                    {"start": "18:00", "end": "19:00", "activity": "Abendessen"},
                    {"start": "19:00", "end": "23:00", "activity": "Freizeit"},
                    {"start": "23:00", "end": "00:00", "activity": "Schlafen"}
                  ]
                }""";

        EnrichedWeeklySchedule result = scheduleEnrichmentAiService.enrichSchedule(
                TEST_PERSONALITY,
                scheduleJson
        );

        assertThat(result).isNotNull();
        assertThat(result.weekday()).as("Weekday block count").hasSize(10);
        assertThat(result.weekend()).as("Weekend block count").hasSize(8);

        // Times should be preserved
        assertThat(result.weekday().get(0).start()).isEqualTo("00:00");
        assertThat(result.weekday().get(0).end()).isEqualTo("07:00");
        assertThat(result.weekday().get(3).start()).isEqualTo("08:00");
        assertThat(result.weekday().get(3).end()).isEqualTo("12:00");

        // Activities should be enriched (non-blank)
        for (TimeBlock block : result.weekday()) {
            assertThat(block.activity()).as("Activity should be non-blank").isNotBlank();
        }
        for (TimeBlock block : result.weekend()) {
            assertThat(block.activity()).as("Activity should be non-blank").isNotBlank();
        }
    }
}
