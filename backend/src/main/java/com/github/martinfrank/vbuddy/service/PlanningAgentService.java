package com.github.martinfrank.vbuddy.service;

import com.github.martinfrank.vbuddy.ai.EnrichmentAiService;
import com.github.martinfrank.vbuddy.ai.PlanningAiService;
import com.github.martinfrank.vbuddy.ai.PlannedTask;
import com.github.martinfrank.vbuddy.ai.PlannedTasks;
import com.github.martinfrank.vbuddy.model.*;
import com.github.martinfrank.vbuddy.repository.AiDecisionLogRepository;
import com.github.martinfrank.vbuddy.repository.VBuddyTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanningAgentService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter FLEXIBLE_PARSER = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            .optionalStart().appendLiteral('T').optionalEnd()
            .optionalStart().appendLiteral(' ').optionalEnd()
            .append(DateTimeFormatter.ofPattern("HH:mm"))
            .optionalStart().appendPattern(":ss").optionalEnd()
            .toFormatter();

    private final PlanningAiService planningAiService;
    private final EnrichmentAiService enrichmentAiService;
    private final BuddyService buddyService;
    private final VBuddyTaskRepository taskRepository;
    private final AiDecisionLogRepository aiDecisionLogRepository;
    private final SearxngSearchService searxngSearchService;
    private final EmbeddingService embeddingService;

    @Transactional
    public List<VBuddyTask> planTasks(Long buddyId) {
        Buddy buddy = buddyService.findById(buddyId);
        List<Need> needs = buddyService.getNeeds(buddyId);
        List<VBuddyTask> recentTasks = taskRepository.findByBuddyIdAndStatusInOrderByStartTimeDesc(
                buddyId, List.of(TaskStatus.COMPLETED, TaskStatus.IN_PROGRESS));

        String needsText = needs.stream()
                .map(n -> String.format("- %s: %.0f/%.0f", n.getNeedType(), n.getCurrentValue(), n.getMaxValue()))
                .collect(Collectors.joining("\n"));

        String recentTasksText = recentTasks.isEmpty()
                ? "Keine bisherigen Aktivitäten."
                : recentTasks.stream()
                .limit(10)
                .map(t -> String.format("- %s (%s, %s, %d min)",
                        t.getTitle(), t.getLocation(),
                        t.getStartTime().format(FORMATTER), t.getDurationMinutes()))
                .collect(Collectors.joining("\n"));

        String currentTime = LocalDateTime.now().format(FORMATTER);

        List<SearchResult> searchResults = searxngSearchService.searchLocalActivities(
                buddy.getCurrentLocation(), LocalDate.now());
        String localActivitiesText = searxngSearchService.formatResultsAsText(searchResults);

        String historicalContext;
        try {
            String ragQuery = buddy.getPersonality() + " " + needsText;
            List<String> ragResults = embeddingService.retrieveRelevantContext(buddyId, ragQuery, 10);
            historicalContext = ragResults.isEmpty()
                    ? "Keine historischen Daten verfügbar."
                    : String.join("\n", ragResults);
        } catch (Exception e) {
            log.warn("Failed to retrieve RAG context for planning buddy {}: {}", buddyId, e.getMessage());
            historicalContext = "Keine historischen Daten verfügbar.";
        }

        log.info("Planungs-Agent startet für Buddy '{}' (ID: {})", buddy.getName(), buddyId);

        PlannedTasks planned;
        try {
            planned = planningAiService.planTasks(
                    buddy.getPersonality(),
                    buddy.getCurrentLocation(),
                    currentTime,
                    needsText,
                    recentTasksText,
                    localActivitiesText,
                    historicalContext
            );
        } catch (Exception e) {
            log.warn("Planungs-Agent konnte keine Tasks parsen für Buddy '{}': {}", buddy.getName(), e.getMessage());
            return Collections.emptyList();
        }

        if (planned == null || planned.tasks() == null || planned.tasks().isEmpty()) {
            log.warn("Planungs-Agent hat keine Tasks zurückgeliefert für Buddy '{}'", buddy.getName());
            return Collections.emptyList();
        }

        List<PlannedTask> enrichedPlannedTasks = planned.tasks().stream()
                .map(pt -> enrichTask(pt, buddy.getPersonality(), planned.reasoning(), localActivitiesText))
                .toList();

        List<VBuddyTask> savedTasks = enrichedPlannedTasks.stream()
                .map(pt -> toEntity(pt, buddy))
                .map(taskRepository::save)
                .toList();

        logDecision(buddy, needsText, planned);

        log.info("Planungs-Agent hat {} Tasks für Buddy '{}' erstellt", savedTasks.size(), buddy.getName());

        return savedTasks;
    }

    private PlannedTask enrichTask(PlannedTask task, String personality, String planningReasoning, String localActivities) {
        try {
            log.info("Enrichment-Agent bearbeitet Task '{}'", task.title());
            var enriched = enrichmentAiService.enrichTask(
                    personality,
                    task.title(),
                    task.location(),
                    task.description(),
                    task.durationMinutes(),
                    planningReasoning,
                    localActivities
            );
            if (enriched != null && enriched.enrichedDescription() != null && !enriched.enrichedDescription().isBlank()) {
                log.info("Enrichment erfolgreich für Task '{}'", task.title());
                return new PlannedTask(task.title(), enriched.enrichedDescription(), task.location(), task.startTime(), task.durationMinutes());
            }
        } catch (Exception e) {
            log.warn("Enrichment fehlgeschlagen für Task '{}': {} — verwende Original-Beschreibung", task.title(), e.getMessage());
        }
        return task;
    }

    private VBuddyTask toEntity(PlannedTask planned, Buddy buddy) {
        VBuddyTask task = new VBuddyTask();
        task.setBuddy(buddy);
        task.setTitle(planned.title());
        task.setDescription(planned.description());
        task.setLocation(planned.location());
        task.setStartTime(LocalDateTime.parse(planned.startTime().trim(), FLEXIBLE_PARSER));
        task.setDurationMinutes(planned.durationMinutes());
        task.setStatus(TaskStatus.PLANNED);
        return task;
    }

    private void logDecision(Buddy buddy, String needsText, PlannedTasks planned) {
        String decision = planned.tasks().stream()
                .map(t -> String.format("%s — %s (%s, %d min)", t.startTime(), t.title(), t.location(), t.durationMinutes()))
                .collect(Collectors.joining("\n"));

        AiDecisionLog logEntry = new AiDecisionLog();
        logEntry.setBuddy(buddy);
        logEntry.setContext("Tagesplanung | Ort: " + buddy.getCurrentLocation() + " | Bedürfnisse:\n" + needsText
                + "\n| Websuche-Ergebnisse verfügbar: ja");
        logEntry.setDecision(decision);
        logEntry.setReasoning(planned.reasoning());
        aiDecisionLogRepository.save(logEntry);
    }
}
