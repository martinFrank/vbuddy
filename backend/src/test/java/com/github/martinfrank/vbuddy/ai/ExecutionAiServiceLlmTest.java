package com.github.martinfrank.vbuddy.ai;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.github.martinfrank.vbuddy.util.UtcDateTimeUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import static com.github.martinfrank.vbuddy.ai.LlmTestConfig.*;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("llm")
class ExecutionAiServiceLlmTest {

    private static final Set<String> VALID_NEED_TYPES = Set.of("HUNGER", "BOREDOM", "KNOWLEDGE", "EXERCISE", "SOCIAL");

    private static ExecutionAiService executionAiService;

    @BeforeAll
    static void setUp() {
        executionAiService = buildService(ExecutionAiService.class, executionModel());
    }

    @Test
    void executeTask_returnsValidBlogAndNeedAdjustments() {
        String currentTime = UtcDateTimeUtil.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        TaskExecutionResult result = executionAiService.executeTask(
                currentTime,
                TEST_PERSONALITY,
                "Joggen im Stadtpark",
                "Max geht eine Runde joggen, um sich zu bewegen und den Kopf freizubekommen.",
                "Stadtpark Nürnberg",
                45,
                TEST_NEEDS
        );

        assertThat(result).isNotNull();
        assertThat(result.blogTitle()).as("Blog title").isNotBlank();
        assertThat(result.blogContent()).as("Blog content").isNotBlank();
        assertThat(result.blogContent().length()).as("Blog content length")
                .isGreaterThan(100);
        assertThat(result.reasoning()).as("Reasoning").isNotBlank();
    }

    @Test
    void executeTask_needAdjustmentsAreValid() {
        String currentTime = UtcDateTimeUtil.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        TaskExecutionResult result = executionAiService.executeTask(
                currentTime,
                TEST_PERSONALITY,
                "Abendessen kochen",
                "Max kocht sich ein leckeres Pasta-Gericht mit frischen Zutaten.",
                "Zu Hause",
                60,
                TEST_NEEDS
        );

        assertThat(result.needAdjustments()).isNotNull().isNotEmpty();
        for (NeedAdjustment adj : result.needAdjustments()) {
            assertThat(adj.needType()).as("Need type")
                    .isIn(VALID_NEED_TYPES);
            assertThat(adj.change()).as("Need change for %s", adj.needType())
                    .isBetween(-30.0, 10.0);
        }
    }

    @Test
    void executeTask_cookingReducesHunger() {
        String currentTime = UtcDateTimeUtil.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        TaskExecutionResult result = executionAiService.executeTask(
                currentTime,
                TEST_PERSONALITY,
                "Abendessen kochen und essen",
                "Max bereitet ein aufwendiges Abendessen zu und genießt es.",
                "Zu Hause",
                90,
                TEST_NEEDS
        );

        boolean reducesHunger = result.needAdjustments().stream()
                .anyMatch(adj -> adj.needType().equals("HUNGER") && adj.change() < 0);
        assertThat(reducesHunger)
                .as("Cooking and eating should reduce HUNGER")
                .isTrue();
    }

    @Test
    void executeTask_blogIsWrittenInFirstPerson() {
        String currentTime = UtcDateTimeUtil.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        TaskExecutionResult result = executionAiService.executeTask(
                currentTime,
                TEST_PERSONALITY,
                "Spaziergang am Fluss",
                "Ein entspannter Spaziergang entlang der Pegnitz.",
                "Pegnitzufer, Nürnberg",
                30,
                TEST_NEEDS
        );

        String content = result.blogContent().toLowerCase();
        boolean isFirstPerson = content.contains("ich ") || content.contains("mir ") ||
                content.contains("mein") || content.contains("mich ");
        assertThat(isFirstPerson)
                .as("Blog should be written in first person (Ich-Perspektive)")
                .isTrue();
    }
}
