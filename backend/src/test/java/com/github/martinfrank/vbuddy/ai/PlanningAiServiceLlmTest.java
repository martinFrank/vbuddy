package com.github.martinfrank.vbuddy.ai;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.github.martinfrank.vbuddy.util.UtcDateTimeUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.github.martinfrank.vbuddy.ai.LlmTestConfig.*;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("llm")
class PlanningAiServiceLlmTest {

    private static PlanningAiService planningAiService;

    @BeforeAll
    static void setUp() {
        planningAiService = buildService(PlanningAiService.class, planningModel());
    }

    @Test
    void planTasks_returnsValidStructuredOutput() {
        String currentTime = UtcDateTimeUtil.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        PlannedTasks result = planningAiService.planTasks(
                TEST_PERSONALITY,
                TEST_LOCATION,
                currentTime,
                TEST_NEEDS,
                "Keine bisherigen Aktivitäten",
                "Keine Suchergebnisse verfügbar",
                "Keine historischen Daten verfügbar",
                TEST_WEEKLY_SCHEDULE,
                "Wochentag"
        );

        System.out.println("=== Planning Output ===");
        System.out.println("Reasoning: " + result.reasoning());
        if (result.tasks() != null) {
            for (PlannedTask task : result.tasks()) {
                System.out.printf("  %s | %s | %s | %d min%n",
                        task.startTime(), task.title(), task.location(), task.durationMinutes());
            }
        }

        assertThat(result).isNotNull();
        assertThat(result.tasks()).as("LLM should return a non-null task list (check if model produces valid JSON)")
                .isNotNull().hasSizeBetween(3, 5);
        assertThat(result.reasoning()).isNotBlank();

        for (PlannedTask task : result.tasks()) {
            assertThat(task.title()).as("Task title").isNotBlank();
            assertThat(task.description()).as("Task description").isNotBlank();
            assertThat(task.location()).as("Task location").isNotBlank();
            // Accept both 'yyyy-MM-dd HH:mm' and 'yyyy-MM-ddTHH:mm'
            assertThat(task.startTime()).as("Task startTime format")
                    .matches("\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}(:\\d{2})?");
            assertThat(task.durationMinutes()).as("Task duration")
                    .isGreaterThan(0)
                    .isLessThanOrEqualTo(480);
        }
    }

    @Test
    void planTasks_tasksAreChronological() {
        String currentTime = UtcDateTimeUtil.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        PlannedTasks result = planningAiService.planTasks(
                TEST_PERSONALITY,
                TEST_LOCATION,
                currentTime,
                TEST_NEEDS,
                "Keine bisherigen Aktivitäten",
                "Keine Suchergebnisse verfügbar",
                "Keine historischen Daten verfügbar",
                TEST_WEEKLY_SCHEDULE,
                "Wochentag"
        );

        for (int i = 1; i < result.tasks().size(); i++) {
            LocalDateTime prev = parseFlexible(result.tasks().get(i - 1).startTime());
            LocalDateTime curr = parseFlexible(result.tasks().get(i).startTime());
            assertThat(curr).as("Task %d should be after task %d", i, i - 1)
                    .isAfterOrEqualTo(prev);
        }
    }

    @Test
    void planTasks_highHunger_includesFoodRelatedActivity() {
        String currentTime = UtcDateTimeUtil.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String highHungerNeeds = """
                HUNGER: 95/100
                BOREDOM: 10/100
                KNOWLEDGE: 10/100
                EXERCISE: 10/100
                SOCIAL: 10/100""";

        PlannedTasks result = planningAiService.planTasks(
                TEST_PERSONALITY,
                TEST_LOCATION,
                currentTime,
                highHungerNeeds,
                "Keine bisherigen Aktivitäten",
                "Keine Suchergebnisse verfügbar",
                "Keine historischen Daten verfügbar",
                TEST_WEEKLY_SCHEDULE,
                "Wochentag"
        );

        boolean hasFoodActivity = result.tasks().stream()
                .anyMatch(t -> {
                    String combined = (t.title() + " " + t.description()).toLowerCase();
                    return combined.contains("essen") || combined.contains("kochen") ||
                            combined.contains("frühstück") || combined.contains("mittag") ||
                            combined.contains("abendessen") || combined.contains("restaurant") ||
                            combined.contains("hunger") || combined.contains("snack") ||
                            combined.contains("küche") || combined.contains("food") ||
                            combined.contains("mahlzeit");
                });
        assertThat(hasFoodActivity)
                .as("At least one activity should address high hunger need")
                .isTrue();
    }
}
