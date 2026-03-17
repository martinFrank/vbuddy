package com.github.martinfrank.vbuddy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.martinfrank.vbuddy.ai.BackgroundEnrichmentAiService;
import com.github.martinfrank.vbuddy.ai.BackgroundPlanningAiService;
import com.github.martinfrank.vbuddy.ai.EnrichedBackground;
import com.github.martinfrank.vbuddy.ai.PlannedBackground;
import com.github.martinfrank.vbuddy.controller.exception.EntityNotFoundException;
import com.github.martinfrank.vbuddy.model.AiDecisionLog;
import com.github.martinfrank.vbuddy.model.BackgroundStatus;
import com.github.martinfrank.vbuddy.model.Buddy;
import com.github.martinfrank.vbuddy.model.BuddyBackground;
import com.github.martinfrank.vbuddy.repository.AiDecisionLogRepository;
import com.github.martinfrank.vbuddy.repository.BuddyBackgroundRepository;
import com.github.martinfrank.vbuddy.repository.BuddyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackgroundAgentService {

    private final BuddyRepository buddyRepository;
    private final BuddyBackgroundRepository backgroundRepository;
    private final AiDecisionLogRepository aiDecisionLogRepository;
    private final BackgroundPlanningAiService backgroundPlanningAiService;
    private final BackgroundEnrichmentAiService backgroundEnrichmentAiService;
    private final ObjectMapper objectMapper;
    private final EmbeddingService embeddingService;

    @Async
    public void generateBackground(Long buddyId) {
        try {
            Buddy buddy = buddyRepository.findById(buddyId)
                    .orElseThrow(() -> new EntityNotFoundException("Buddy", buddyId));

            BuddyBackground background = backgroundRepository.findByBuddyId(buddyId)
                    .orElseGet(() -> {
                        BuddyBackground bg = new BuddyBackground();
                        bg.setBuddy(buddy);
                        return bg;
                    });

            if (background.getStatus() == BackgroundStatus.GENERATING) {
                log.info("Background generation already in progress for buddy {}", buddyId);
                return;
            }

            background.setStatus(BackgroundStatus.GENERATING);
            background.setUpdatedAt(LocalDateTime.now());
            background = backgroundRepository.save(background);

            // Step 1: Planning Agent — structured data
            log.info("Starting background planning for buddy {} ({})", buddyId, buddy.getName());
            PlannedBackground planned = backgroundPlanningAiService.planBackground(buddy.getPersonality());

            String structuredDataJson;
            try {
                structuredDataJson = objectMapper.writeValueAsString(planned);
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize planned background", e);
            }

            background.setStructuredData(structuredDataJson);
            background.setUpdatedAt(LocalDateTime.now());
            backgroundRepository.save(background);

            logAiDecision(buddy,
                    "Background-Planning für " + buddy.getName() + " | Persönlichkeit: " + buddy.getPersonality(),
                    "Strukturiertes Charakterprofil erstellt: Alter=" + planned.alter() + ", Herkunft=" + planned.herkunft(),
                    planned.reasoning());

            // Step 2: Enrichment Agent — narrative text
            log.info("Starting background enrichment for buddy {} ({})", buddyId, buddy.getName());
            EnrichedBackground enriched = backgroundEnrichmentAiService.enrichBackground(
                    buddy.getPersonality(), structuredDataJson);

            background.setNarrativeText(enriched.narrativeText());
            background.setStatus(BackgroundStatus.COMPLETED);
            background.setUpdatedAt(LocalDateTime.now());
            backgroundRepository.save(background);

            logAiDecision(buddy,
                    "Background-Enrichment für " + buddy.getName() + " | Strukturdaten vorhanden",
                    "Erzähltext erstellt (" + enriched.narrativeText().length() + " Zeichen)",
                    null);

            try {
                embeddingService.embedBackground(buddyId, enriched.narrativeText());
            } catch (Exception embeddingEx) {
                log.warn("Failed to embed background for buddy {}: {}", buddyId, embeddingEx.getMessage());
            }

            log.info("Background generation completed for buddy {} ({})", buddyId, buddy.getName());

        } catch (Exception e) {
            log.error("Background generation failed for buddy {}", buddyId, e);
            backgroundRepository.findByBuddyId(buddyId).ifPresent(bg -> {
                bg.setStatus(BackgroundStatus.FAILED);
                bg.setUpdatedAt(LocalDateTime.now());
                backgroundRepository.save(bg);
            });
        }
    }

    public Optional<BuddyBackground> getBackground(Long buddyId) {
        return backgroundRepository.findByBuddyId(buddyId);
    }

    private void logAiDecision(Buddy buddy, String context, String decision, String reasoning) {
        AiDecisionLog logEntry = new AiDecisionLog();
        logEntry.setBuddy(buddy);
        logEntry.setContext(context);
        logEntry.setDecision(decision);
        logEntry.setReasoning(reasoning);
        aiDecisionLogRepository.save(logEntry);
    }
}
