package com.github.martinfrank.vbuddy.service;

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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlanningAgentService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final PlanningAiService planningAiService;
    private final BuddyService buddyService;
    private final VBuddyTaskRepository taskRepository;
    private final AiDecisionLogRepository aiDecisionLogRepository;

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

        log.info("Planungs-Agent startet für Buddy '{}' (ID: {})", buddy.getName(), buddyId);

        PlannedTasks planned = planningAiService.planTasks(
                buddy.getPersonality(),
                buddy.getCurrentLocation(),
                currentTime,
                needsText,
                recentTasksText
        );

        List<VBuddyTask> savedTasks = planned.tasks().stream()
                .map(pt -> toEntity(pt, buddy))
                .map(taskRepository::save)
                .toList();

        logDecision(buddy, needsText, planned);

        log.info("Planungs-Agent hat {} Tasks für Buddy '{}' erstellt", savedTasks.size(), buddy.getName());

        return savedTasks;
    }

    private VBuddyTask toEntity(PlannedTask planned, Buddy buddy) {
        VBuddyTask task = new VBuddyTask();
        task.setBuddy(buddy);
        task.setTitle(planned.title());
        task.setDescription(planned.description());
        task.setLocation(planned.location());
        task.setStartTime(LocalDateTime.parse(planned.startTime(), FORMATTER));
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
        logEntry.setContext("Tagesplanung | Ort: " + buddy.getCurrentLocation() + " | Bedürfnisse:\n" + needsText);
        logEntry.setDecision(decision);
        logEntry.setReasoning(planned.reasoning());
        aiDecisionLogRepository.save(logEntry);
    }
}
