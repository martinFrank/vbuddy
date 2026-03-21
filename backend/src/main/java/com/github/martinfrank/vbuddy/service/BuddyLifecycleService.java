package com.github.martinfrank.vbuddy.service;

import com.github.martinfrank.vbuddy.model.BackgroundStatus;
import com.github.martinfrank.vbuddy.model.Buddy;
import com.github.martinfrank.vbuddy.model.TaskStatus;
import com.github.martinfrank.vbuddy.model.VBuddyTask;
import com.github.martinfrank.vbuddy.repository.VBuddyTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BuddyLifecycleService {

    private static final Logger log = LoggerFactory.getLogger(BuddyLifecycleService.class);

    private final BuddyService buddyService;
    private final VBuddyTaskRepository taskRepository;
    private final PlanningAgentService planningAgentService;
    private final ExecutionAgentService executionAgentService;
    private final BackgroundAgentService backgroundAgentService;

    public BuddyLifecycleService(BuddyService buddyService, VBuddyTaskRepository taskRepository,
                                  PlanningAgentService planningAgentService, ExecutionAgentService executionAgentService,
                                  BackgroundAgentService backgroundAgentService) {
        this.buddyService = buddyService;
        this.taskRepository = taskRepository;
        this.planningAgentService = planningAgentService;
        this.executionAgentService = executionAgentService;
        this.backgroundAgentService = backgroundAgentService;
    }

    @Scheduled(fixedDelayString = "${vbuddy.lifecycle.interval-ms:60000}")
    public void tick() {
        List<Buddy> buddies = buddyService.findAll();
        for (Buddy buddy : buddies) {
            try {
                processBuddy(buddy.getId());
            } catch (Exception e) {
                log.error("Fehler im Lifecycle für Buddy '{}' (ID: {}): {}",
                        buddy.getName(), buddy.getId(), e.getMessage(), e);
            }
        }
    }

    public void processBuddy(Long buddyId) {
        Buddy buddy = buddyService.findById(buddyId);

        Optional<VBuddyTask> activeTask = taskRepository.findFirstByBuddyIdAndStatusOrderByStartTimeAsc(
                buddyId, TaskStatus.IN_PROGRESS);

        if (activeTask.isPresent()) {
            VBuddyTask task = activeTask.get();
            if (!executionAgentService.isTaskFinished(task)) {
                log.debug("Buddy '{}' arbeitet noch an Task '{}' — warte auf Abschluss", buddy.getName(), task.getTitle());
                return;
            }
            log.info("Task '{}' von Buddy '{}' ist zeitlich abgelaufen — wird abgeschlossen", task.getTitle(), buddy.getName());
            executionAgentService.completeTask(task.getId());
            return;
        }

        Optional<VBuddyTask> nextTask = taskRepository.findFirstByBuddyIdAndStatusOrderByStartTimeAsc(
                buddyId, TaskStatus.PLANNED);

        if (nextTask.isPresent()) {
            log.info("Buddy '{}' startet nächsten Task: '{}'", buddy.getName(), nextTask.get().getTitle());
            executionAgentService.startTask(nextTask.get().getId());
            return;
        }

        BackgroundStatus bgStatus = backgroundAgentService.getBackground(buddyId)
                .map(bg -> bg.getStatus())
                .orElse(BackgroundStatus.PENDING);

        if (bgStatus != BackgroundStatus.COMPLETED) {
            log.info("Buddy '{}' wartet auf Background-Generierung (Status: {}) — Tagesplanung wird übersprungen",
                    buddy.getName(), bgStatus);
            return;
        }

        log.info("Buddy '{}' hat keine geplanten Tasks — Planungs-Agent wird gestartet", buddy.getName());
        List<VBuddyTask> newTasks = planningAgentService.planTasks(buddyId);

        if (!newTasks.isEmpty()) {
            log.info("Buddy '{}' hat {} neue Tasks erhalten — starte ersten Task: '{}'",
                    buddy.getName(), newTasks.size(), newTasks.getFirst().getTitle());
            executionAgentService.startTask(newTasks.getFirst().getId());
        }
    }
}
