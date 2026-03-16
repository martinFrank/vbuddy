package com.github.martinfrank.vbuddy.service;

import com.github.martinfrank.vbuddy.ai.ExecutionAiService;
import com.github.martinfrank.vbuddy.ai.NeedAdjustment;
import com.github.martinfrank.vbuddy.ai.TaskExecutionResult;
import com.github.martinfrank.vbuddy.model.*;
import com.github.martinfrank.vbuddy.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExecutionAgentService {

    private final ExecutionAiService executionAiService;
    private final BuddyService buddyService;
    private final VBuddyTaskRepository taskRepository;
    private final NeedRepository needRepository;
    private final BuddyRepository buddyRepository;
    private final BlogPostRepository blogPostRepository;
    private final AiDecisionLogRepository aiDecisionLogRepository;
    private final EmbeddingService embeddingService;

    @Transactional
    public VBuddyTask startTask(Long taskId) {
        VBuddyTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        if (task.getStatus() != TaskStatus.PLANNED) {
            throw new RuntimeException("Task is not in PLANNED status: " + taskId);
        }

        Buddy buddy = buddyService.findById(task.getBuddy().getId());

        log.info("Buddy '{}' beginnt Task '{}' (Dauer: {} min, Ort: {})",
                buddy.getName(), task.getTitle(), task.getDurationMinutes(), task.getLocation());

        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setStartTime(LocalDateTime.now());
        taskRepository.save(task);

        updateLocation(buddy, task.getLocation());

        return task;
    }

    public boolean isTaskFinished(VBuddyTask task) {
        LocalDateTime endTime = task.getStartTime().plusMinutes(task.getDurationMinutes());
        return LocalDateTime.now().isAfter(endTime);
    }

    @Transactional
    public VBuddyTask completeTask(Long taskId) {
        VBuddyTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        if (task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new RuntimeException("Task is not IN_PROGRESS: " + taskId);
        }

        Buddy buddy = buddyService.findById(task.getBuddy().getId());
        List<Need> needs = buddyService.getNeeds(buddy.getId());

        String needsText = needs.stream()
                .map(n -> String.format("- %s: %.0f/%.0f", n.getNeedType(), n.getCurrentValue(), n.getMaxValue()))
                .collect(Collectors.joining("\n"));

        log.info("Ausführungs-Agent verarbeitet abgeschlossenen Task '{}' für Buddy '{}'",
                task.getTitle(), buddy.getName());

        TaskExecutionResult result = executionAiService.executeTask(
                buddy.getPersonality(),
                task.getTitle(),
                task.getDescription(),
                task.getLocation(),
                task.getDurationMinutes(),
                needsText
        );

        saveBlogPost(buddy, task, result);
        adjustNeeds(needs, result.needAdjustments());

        task.setStatus(TaskStatus.COMPLETED);
        taskRepository.save(task);

        try {
            embeddingService.embedCompletedTask(buddy.getId(), task.getId(),
                    task.getTitle(), task.getDescription(), task.getLocation());
        } catch (Exception e) {
            log.warn("Failed to embed completed task {} for buddy {}: {}", task.getId(), buddy.getId(), e.getMessage());
        }

        logDecision(buddy, task, result);

        log.info("Task '{}' für Buddy '{}' abgeschlossen", task.getTitle(), buddy.getName());

        return task;
    }

    @Transactional
    public VBuddyTask executeTask(Long taskId) {
        startTask(taskId);
        return completeTask(taskId);
    }

    @Transactional
    public List<VBuddyTask> executeAllPlannedTasks(Long buddyId) {
        List<VBuddyTask> plannedTasks = taskRepository.findByBuddyIdAndStatusOrderByStartTimeAsc(
                buddyId, TaskStatus.PLANNED);

        return plannedTasks.stream()
                .map(task -> executeTask(task.getId()))
                .toList();
    }

    private void saveBlogPost(Buddy buddy, VBuddyTask task, TaskExecutionResult result) {
        BlogPost blogPost = new BlogPost();
        blogPost.setBuddy(buddy);
        blogPost.setTitle(result.blogTitle());
        blogPost.setContent(result.blogContent());
        blogPostRepository.save(blogPost);

        log.info("Blogartikel '{}' erstellt", result.blogTitle());
    }

    private void adjustNeeds(List<Need> needs, List<NeedAdjustment> adjustments) {
        Map<String, Need> needMap = needs.stream()
                .collect(Collectors.toMap(n -> n.getNeedType().name(), n -> n));

        for (NeedAdjustment adjustment : adjustments) {
            Need need = needMap.get(adjustment.needType());
            if (need != null) {
                double newValue = Math.max(0, Math.min(need.getMaxValue(),
                        need.getCurrentValue() + adjustment.change()));
                need.setCurrentValue(newValue);
                need.setUpdatedAt(LocalDateTime.now());
                needRepository.save(need);
                log.info("Bedürfnis {} angepasst: {} -> {}",
                        adjustment.needType(), Math.round(need.getCurrentValue() - adjustment.change()), Math.round(newValue));
            }
        }
    }

    private void updateLocation(Buddy buddy, String newLocation) {
        buddy.setCurrentLocation(newLocation);
        buddyRepository.save(buddy);
    }

    private void logDecision(Buddy buddy, VBuddyTask task, TaskExecutionResult result) {
        String context = String.format("Task-Ausführung | Task: %s | Ort: %s | Dauer: %d min",
                task.getTitle(), task.getLocation(), task.getDurationMinutes());

        String decision = String.format("Blog: '%s' | Ort aktualisiert: %s | Bedürfnis-Anpassungen: %s",
                result.blogTitle(),
                task.getLocation(),
                result.needAdjustments().stream()
                        .map(a -> String.format("%s: %+.0f", a.needType(), a.change()))
                        .collect(Collectors.joining(", ")));

        AiDecisionLog logEntry = new AiDecisionLog();
        logEntry.setBuddy(buddy);
        logEntry.setContext(context);
        logEntry.setDecision(decision);
        logEntry.setReasoning(result.reasoning());
        aiDecisionLogRepository.save(logEntry);
    }
}
