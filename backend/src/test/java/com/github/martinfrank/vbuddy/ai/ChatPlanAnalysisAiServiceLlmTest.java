package com.github.martinfrank.vbuddy.ai;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.github.martinfrank.vbuddy.ai.LlmTestConfig.*;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("llm")
class ChatPlanAnalysisAiServiceLlmTest {

    private static ChatPlanAnalysisAiService chatPlanAnalysisAiService;

    @BeforeAll
    static void setUp() {
        chatPlanAnalysisAiService = buildService(ChatPlanAnalysisAiService.class, planningModel());
    }

    @Test
    void analyzeChatForPlanAdjustment_smalltalk_noAdjustment() {
        String plannedTasks = """
                ID: 1, Titel: Joggen im Park, Start: 2026-03-21 17:00, Dauer: 45min
                ID: 2, Titel: Abendessen kochen, Start: 2026-03-21 18:00, Dauer: 60min""";

        String recentMessages = """
                Nutzer: Hey, wie geht's dir?
                VBuddy: Mir geht's gut, danke! Ich freue mich auf mein Joggen später.
                Nutzer: Das klingt toll, viel Spaß!
                VBuddy: Danke, das wird bestimmt schön bei dem Wetter!""";

        PlanAdjustmentAnalysis result = chatPlanAnalysisAiService.analyzeChatForPlanAdjustment(
                "2026-03-21 16:00",
                TEST_PERSONALITY,
                plannedTasks,
                recentMessages
        );

        assertThat(result).isNotNull();
        assertThat(result.adjustmentNeeded())
                .as("Smalltalk should not trigger plan adjustment")
                .isFalse();
        assertThat(result.reasoning()).isNotBlank();
    }

    @Test
    void analyzeChatForPlanAdjustment_cancelRequest_detectsAdjustment() {
        String plannedTasks = """
                ID: 1, Titel: Joggen im Park, Start: 2026-03-21 17:00, Dauer: 45min
                ID: 2, Titel: Abendessen kochen, Start: 2026-03-21 18:00, Dauer: 60min""";

        String recentMessages = """
                Nutzer: Hey, es regnet draußen. Können wir das Joggen absagen?
                VBuddy: Stimmt, der Regen ist ziemlich stark. Dann lasse ich das Joggen heute ausfallen und bleibe lieber drin.""";

        PlanAdjustmentAnalysis result = chatPlanAnalysisAiService.analyzeChatForPlanAdjustment(
                "2026-03-21 16:00",
                TEST_PERSONALITY,
                plannedTasks,
                recentMessages
        );

        assertThat(result).isNotNull();
        assertThat(result.adjustmentNeeded())
                .as("Cancel request agreed by VBuddy should trigger adjustment")
                .isTrue();
        assertThat(result.adjustments()).isNotNull().isNotEmpty();
        assertThat(result.adjustments().getFirst().action())
                .as("Action should be CANCEL")
                .isEqualTo("CANCEL");
    }

    @Test
    void analyzeChatForPlanAdjustment_addRequest_detectsNewTask() {
        String plannedTasks = """
                ID: 1, Titel: Abendessen kochen, Start: 2026-03-21 18:00, Dauer: 60min""";

        String recentMessages = """
                Nutzer: Hast du Lust, vorher noch ins Kino zu gehen? Um 15 Uhr läuft ein guter Film.
                VBuddy: Oh ja, das klingt super! Lass uns um 15 Uhr ins Kino gehen, da freue ich mich drauf!""";

        PlanAdjustmentAnalysis result = chatPlanAnalysisAiService.analyzeChatForPlanAdjustment(
                "2026-03-21 14:00",
                TEST_PERSONALITY,
                plannedTasks,
                recentMessages
        );

        assertThat(result).isNotNull();
        assertThat(result.adjustmentNeeded())
                .as("New activity agreed by VBuddy should trigger adjustment")
                .isTrue();
        assertThat(result.adjustments()).isNotNull().isNotEmpty();

        boolean hasAddAction = result.adjustments().stream()
                .anyMatch(adj -> "ADD".equals(adj.action()));
        assertThat(hasAddAction)
                .as("Should contain an ADD adjustment")
                .isTrue();
    }
}
