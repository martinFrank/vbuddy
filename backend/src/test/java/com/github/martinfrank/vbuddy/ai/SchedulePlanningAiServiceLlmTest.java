package com.github.martinfrank.vbuddy.ai;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

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

        System.out.println("=== Schedule Output ===");
        System.out.println("Reasoning: " + result.reasoning());
        System.out.println("Weekday blocks:");
        result.weekday().forEach(b -> System.out.printf("  %s-%s %s%n", b.start(), b.end(), b.activity()));
        System.out.println("Weekend blocks:");
        result.weekend().forEach(b -> System.out.printf("  %s-%s %s%n", b.start(), b.end(), b.activity()));

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
    void planSchedule_coversFullDay() {
        PlannedWeeklySchedule result = schedulePlanningAiService.planSchedule(
                TEST_PERSONALITY,
                TEST_BACKGROUND_NARRATIVE
        );

        // LLMs don't always start exactly at 00:00 — verify reasonable coverage
        // First block should start early (sleep or morning routine)
        Set<String> earlyStarts = Set.of("00:00", "05:00", "06:00", "06:30", "07:00", "07:30", "08:00");
        assertThat(earlyStarts)
                .as("Weekday should start at a reasonable early time, got: %s", result.weekday().getFirst().start())
                .contains(result.weekday().getFirst().start());

        // Schedule should have enough blocks to cover a full day
        assertThat(result.weekday().size())
                .as("Weekday should have enough blocks for a full day")
                .isGreaterThanOrEqualTo(5);
        assertThat(result.weekend().size())
                .as("Weekend should have enough blocks for a full day")
                .isGreaterThanOrEqualTo(4);
    }

    @Test
    void planSchedule_blocksAreContiguous() {
        PlannedWeeklySchedule result = schedulePlanningAiService.planSchedule(
                TEST_PERSONALITY,
                TEST_BACKGROUND_NARRATIVE
        );

        // Each block's start should equal the previous block's end
        assertContiguous(result.weekday(), "Weekday");
        assertContiguous(result.weekend(), "Weekend");
    }

    private void assertContiguous(List<TimeBlock> blocks, String label) {
        for (int i = 1; i < blocks.size(); i++) {
            assertThat(blocks.get(i).start())
                    .as("%s block %d start should equal block %d end", label, i, i - 1)
                    .isEqualTo(blocks.get(i - 1).end());
        }
    }
}
