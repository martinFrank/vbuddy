package com.github.martinfrank.vbuddy.ai;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.github.martinfrank.vbuddy.ai.LlmTestConfig.*;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("llm")
class SchedulePlanningAiServiceLlmTest {

    private static SchedulePlanningAiService schedulePlanningAiService;

    @BeforeAll
    static void setUp() {
        schedulePlanningAiService = buildService(SchedulePlanningAiService.class, planningModel());
    }

    @Test
    void planSchedule_returnsWeekdayAndWeekendBlocks() {
        PlannedWeeklySchedule result = schedulePlanningAiService.planSchedule(
                TEST_PERSONALITY,
                TEST_BACKGROUND_NARRATIVE
        );

        assertThat(result).isNotNull();
        assertThat(result.weekday()).as("Weekday blocks").isNotNull().isNotEmpty();
        assertThat(result.weekend()).as("Weekend blocks").isNotNull().isNotEmpty();
        assertThat(result.reasoning()).as("Reasoning").isNotBlank();
    }

    @Test
    void planSchedule_timeBlocksHaveValidFormat() {
        PlannedWeeklySchedule result = schedulePlanningAiService.planSchedule(
                TEST_PERSONALITY,
                TEST_BACKGROUND_NARRATIVE
        );

        for (TimeBlock block : result.weekday()) {
            assertThat(block.start()).as("Weekday block start").matches("\\d{2}:\\d{2}");
            assertThat(block.end()).as("Weekday block end").matches("\\d{2}:\\d{2}");
            assertThat(block.activity()).as("Weekday block activity").isNotBlank();
        }
        for (TimeBlock block : result.weekend()) {
            assertThat(block.start()).as("Weekend block start").matches("\\d{2}:\\d{2}");
            assertThat(block.end()).as("Weekend block end").matches("\\d{2}:\\d{2}");
            assertThat(block.activity()).as("Weekend block activity").isNotBlank();
        }
    }

    @Test
    void planSchedule_covers24Hours() {
        PlannedWeeklySchedule result = schedulePlanningAiService.planSchedule(
                TEST_PERSONALITY,
                TEST_BACKGROUND_NARRATIVE
        );

        // First block should start at or near 00:00
        assertThat(result.weekday().getFirst().start())
                .as("Weekday should start at 00:00")
                .isEqualTo("00:00");
        assertThat(result.weekend().getFirst().start())
                .as("Weekend should start at 00:00")
                .isEqualTo("00:00");

        // Last block should end at 00:00 (next day)
        assertThat(result.weekday().getLast().end())
                .as("Weekday should end at 00:00")
                .isEqualTo("00:00");
        assertThat(result.weekend().getLast().end())
                .as("Weekend should end at 00:00")
                .isEqualTo("00:00");
    }

    @Test
    void planSchedule_blocksAreContiguous() {
        PlannedWeeklySchedule result = schedulePlanningAiService.planSchedule(
                TEST_PERSONALITY,
                TEST_BACKGROUND_NARRATIVE
        );

        // Each block's start should equal the previous block's end
        for (int i = 1; i < result.weekday().size(); i++) {
            assertThat(result.weekday().get(i).start())
                    .as("Weekday block %d start should equal block %d end", i, i - 1)
                    .isEqualTo(result.weekday().get(i - 1).end());
        }
        for (int i = 1; i < result.weekend().size(); i++) {
            assertThat(result.weekend().get(i).start())
                    .as("Weekend block %d start should equal block %d end", i, i - 1)
                    .isEqualTo(result.weekend().get(i - 1).end());
        }
    }
}
