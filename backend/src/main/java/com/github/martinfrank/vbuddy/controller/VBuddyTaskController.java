package com.github.martinfrank.vbuddy.controller;

import com.github.martinfrank.vbuddy.model.VBuddyTask;
import com.github.martinfrank.vbuddy.repository.VBuddyTaskRepository;
import com.github.martinfrank.vbuddy.service.BuddyLifecycleService;
import com.github.martinfrank.vbuddy.service.ExecutionAgentService;
import com.github.martinfrank.vbuddy.service.PlanningAgentService;
import lombok.RequiredArgsConstructor;
import com.github.martinfrank.vbuddy.model.TaskStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buddies/{buddyId}/tasks")
@RequiredArgsConstructor
public class VBuddyTaskController {

    private final VBuddyTaskRepository taskRepository;
    private final PlanningAgentService planningAgentService;
    private final ExecutionAgentService executionAgentService;
    private final BuddyLifecycleService buddyLifecycleService;

    @GetMapping
    public List<VBuddyTask> getTasks(@PathVariable Long buddyId) {
        return taskRepository.findByBuddyIdOrderByStartTimeAsc(buddyId);
    }

    @GetMapping("/current")
    public ResponseEntity<VBuddyTask> getCurrentTask(@PathVariable Long buddyId) {
        return taskRepository.findFirstByBuddyIdAndStatusOrderByStartTimeAsc(buddyId, TaskStatus.IN_PROGRESS)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/plan")
    @ResponseStatus(HttpStatus.CREATED)
    public List<VBuddyTask> planTasks(@PathVariable Long buddyId) {
        return planningAgentService.planTasks(buddyId);
    }

    @PostMapping("/{taskId}/execute")
    public VBuddyTask executeTask(@PathVariable Long buddyId, @PathVariable Long taskId) {
        return executionAgentService.executeTask(taskId);
    }

    @PostMapping("/execute-all")
    public List<VBuddyTask> executeAllPlannedTasks(@PathVariable Long buddyId) {
        return executionAgentService.executeAllPlannedTasks(buddyId);
    }

    @PostMapping("/tick")
    public void tick(@PathVariable Long buddyId) {
        buddyLifecycleService.processBuddy(buddyId);
    }
}
