package com.github.martinfrank.vbuddy.controller;

import com.github.martinfrank.vbuddy.controller.dto.VBuddyTaskResponse;
import com.github.martinfrank.vbuddy.model.TaskStatus;
import com.github.martinfrank.vbuddy.repository.VBuddyTaskRepository;
import com.github.martinfrank.vbuddy.service.BuddyLifecycleService;
import com.github.martinfrank.vbuddy.service.ExecutionAgentService;
import com.github.martinfrank.vbuddy.service.PlanningAgentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/buddies/{buddyId}/tasks")
public class VBuddyTaskController {

    private final VBuddyTaskRepository taskRepository;
    private final PlanningAgentService planningAgentService;
    private final ExecutionAgentService executionAgentService;
    private final BuddyLifecycleService buddyLifecycleService;

    public VBuddyTaskController(VBuddyTaskRepository taskRepository, PlanningAgentService planningAgentService,
                                ExecutionAgentService executionAgentService, BuddyLifecycleService buddyLifecycleService) {
        this.taskRepository = taskRepository;
        this.planningAgentService = planningAgentService;
        this.executionAgentService = executionAgentService;
        this.buddyLifecycleService = buddyLifecycleService;
    }

    @GetMapping
    public List<VBuddyTaskResponse> getTasks(@PathVariable Long buddyId) {
        return taskRepository.findByBuddyIdOrderByStartTimeAsc(buddyId)
                .stream().map(VBuddyTaskResponse::from).toList();
    }

    @GetMapping("/timeline")
    public List<VBuddyTaskResponse> getTimeline(@PathVariable Long buddyId) {
        LocalDateTime from = LocalDateTime.now().minusHours(8);
        LocalDateTime to = LocalDateTime.now().plusHours(16);
        return taskRepository.findByBuddyIdAndStartTimeBetweenOrderByStartTimeAsc(buddyId, from, to)
                .stream().map(VBuddyTaskResponse::from).toList();
    }

    @GetMapping("/current")
    public ResponseEntity<VBuddyTaskResponse> getCurrentTask(@PathVariable Long buddyId) {
        return taskRepository.findFirstByBuddyIdAndStatusOrderByStartTimeAsc(buddyId, TaskStatus.IN_PROGRESS)
                .map(VBuddyTaskResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/plan")
    @ResponseStatus(HttpStatus.CREATED)
    public List<VBuddyTaskResponse> planTasks(@PathVariable Long buddyId) {
        return planningAgentService.planTasks(buddyId)
                .stream().map(VBuddyTaskResponse::from).toList();
    }

    @PostMapping("/{taskId}/execute")
    public VBuddyTaskResponse executeTask(@PathVariable Long buddyId, @PathVariable Long taskId) {
        return VBuddyTaskResponse.from(executionAgentService.executeTask(taskId));
    }

    @PostMapping("/execute-all")
    public List<VBuddyTaskResponse> executeAllPlannedTasks(@PathVariable Long buddyId) {
        return executionAgentService.executeAllPlannedTasks(buddyId)
                .stream().map(VBuddyTaskResponse::from).toList();
    }

    @PostMapping("/tick")
    public void tick(@PathVariable Long buddyId) {
        buddyLifecycleService.processBuddy(buddyId);
    }
}
