package com.github.martinfrank.vbuddy.service;

import com.github.martinfrank.vbuddy.model.Buddy;
import com.github.martinfrank.vbuddy.model.TaskStatus;
import com.github.martinfrank.vbuddy.model.VBuddyTask;
import com.github.martinfrank.vbuddy.repository.VBuddyTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuddyLifecycleService {

    private final BuddyService buddyService;
    private final VBuddyTaskRepository taskRepository;
    private final PlanningAgentService planningAgentService;
    private final ExecutionAgentService executionAgentService;

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

        if (taskRepository.existsByBuddyIdAndStatus(buddyId, TaskStatus.IN_PROGRESS)) {
            log.debug("Buddy '{}' arbeitet gerade an einem Task — nichts zu tun", buddy.getName());
            return;
        }

        Optional<VBuddyTask> nextTask = taskRepository.findFirstByBuddyIdAndStatusOrderByStartTimeAsc(
                buddyId, TaskStatus.PLANNED);

        if (nextTask.isPresent()) {
            log.info("Buddy '{}' startet nächsten Task: '{}'", buddy.getName(), nextTask.get().getTitle());
            executionAgentService.executeTask(nextTask.get().getId());
            return;
        }

        log.info("Buddy '{}' hat keine geplanten Tasks — Planungs-Agent wird gestartet", buddy.getName());
        List<VBuddyTask> newTasks = planningAgentService.planTasks(buddyId);

        if (!newTasks.isEmpty()) {
            log.info("Buddy '{}' hat {} neue Tasks erhalten — starte ersten Task: '{}'",
                    buddy.getName(), newTasks.size(), newTasks.getFirst().getTitle());
            executionAgentService.executeTask(newTasks.getFirst().getId());
        }
    }
}
