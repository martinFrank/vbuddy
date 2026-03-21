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
                - [ID:1] Joggen im Park (Stadtpark, 2026-03-21 17:00, 45 min)
                - [ID:2] Abendessen kochen (Zu Hause, 2026-03-21 18:00, 60 min)""";

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

        System.out.println("=== Smalltalk Analysis ===");
        System.out.println("adjustmentNeeded: " + result.adjustmentNeeded());
        System.out.println("reasoning: " + result.reasoning());

        assertThat(result).isNotNull();
        assertThat(result.adjustmentNeeded())
                .as("Smalltalk should not trigger plan adjustment")
                .isFalse();
        assertThat(result.reasoning()).isNotBlank();
    }

    @Test
    void analyzeChatForPlanAdjustment_cancelRequest_detectsAdjustment() {
        // Use the same format the production code uses (ChatPlanAdjustmentService)
        String plannedTasks = """
                - [ID:1] Joggen im Park (Stadtpark, 2026-03-21 17:00, 45 min)
                - [ID:2] Abendessen kochen (Zu Hause, 2026-03-21 18:00, 60 min)""";

        String recentMessages = """
                Nutzer: Es regnet draußen, ich finde du solltest das Joggen heute absagen.
                VBuddy: Ja, du hast recht. Bei dem starken Regen macht Joggen keinen Spaß. Ich sage das Joggen ab und bleibe lieber drin.""";

        PlanAdjustmentAnalysis result = chatPlanAnalysisAiService.analyzeChatForPlanAdjustment(
                "2026-03-21 16:00",
                TEST_PERSONALITY,
                plannedTasks,
                recentMessages
        );

        System.out.println("=== Cancel Analysis ===");
        System.out.println("adjustmentNeeded: " + result.adjustmentNeeded());
        System.out.println("reasoning: " + result.reasoning());
        if (result.adjustments() != null) {
            result.adjustments().forEach(a -> System.out.printf("  %s | ID:%s | %s%n",
                    a.action(), a.existingTaskId(), a.title()));
        }

        assertThat(result).isNotNull();
        assertThat(result.adjustmentNeeded())
                .as("Cancel request agreed by VBuddy should trigger adjustment")
                .isTrue();
        assertThat(result.adjustments()).isNotNull().isNotEmpty();

        boolean hasCancelAction = result.adjustments().stream()
                .anyMatch(adj -> "CANCEL".equalsIgnoreCase(adj.action()));
        assertThat(hasCancelAction)
                .as("Should contain a CANCEL adjustment")
                .isTrue();
    }

    @Test
    void analyzeChatForPlanAdjustment_addRequest_detectsNewTask() {
        String plannedTasks = """
                - [ID:5] Abendessen kochen (Zu Hause, 2026-03-21 18:00, 60 min)""";

        // Very explicit agreement to a new task with all details
        String recentMessages = """
                Nutzer: Ich schlage vor, dass du um 15:00 Uhr ins Kino gehst. Da läuft ein Science-Fiction-Film, der würde dir gefallen!
                VBuddy: Das ist eine tolle Idee! Ich gehe um 15:00 Uhr ins Kino, ein Science-Fiction-Film klingt perfekt für mich. Das nehme ich in meinen Plan auf!""";

        PlanAdjustmentAnalysis result = chatPlanAnalysisAiService.analyzeChatForPlanAdjustment(
                "2026-03-21 14:00",
                TEST_PERSONALITY,
                plannedTasks,
                recentMessages
        );

        System.out.println("=== Add Analysis ===");
        System.out.println("adjustmentNeeded: " + result.adjustmentNeeded());
        System.out.println("reasoning: " + result.reasoning());
        if (result.adjustments() != null) {
            result.adjustments().forEach(a -> System.out.printf("  %s | %s | %s | %s%n",
                    a.action(), a.title(), a.startTime(), a.location()));
        }

        assertThat(result).isNotNull();
        assertThat(result.adjustmentNeeded())
                .as("New activity agreed by VBuddy should trigger adjustment")
                .isTrue();
        assertThat(result.adjustments()).isNotNull().isNotEmpty();

        boolean hasAddAction = result.adjustments().stream()
                .anyMatch(adj -> "ADD".equalsIgnoreCase(adj.action()));
        assertThat(hasAddAction)
                .as("Should contain an ADD adjustment")
                .isTrue();
    }
}
