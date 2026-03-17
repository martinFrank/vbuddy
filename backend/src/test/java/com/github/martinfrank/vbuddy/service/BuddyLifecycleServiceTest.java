package com.github.martinfrank.vbuddy.service;

import com.github.martinfrank.vbuddy.model.Buddy;
import com.github.martinfrank.vbuddy.model.TaskStatus;
import com.github.martinfrank.vbuddy.model.VBuddyTask;
import com.github.martinfrank.vbuddy.repository.VBuddyTaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuddyLifecycleServiceTest {

    @Mock
    private BuddyService buddyService;
    @Mock
    private VBuddyTaskRepository taskRepository;
    @Mock
    private PlanningAgentService planningAgentService;
    @Mock
    private ExecutionAgentService executionAgentService;

    @InjectMocks
    private BuddyLifecycleService buddyLifecycleService;

    @Test
    void processBuddy_activeTaskNotFinished_waits() {
        Buddy buddy = createBuddy(1L);
        VBuddyTask activeTask = createTask(1L, TaskStatus.IN_PROGRESS);

        when(buddyService.findById(1L)).thenReturn(buddy);
        when(taskRepository.findFirstByBuddyIdAndStatusOrderByStartTimeAsc(1L, TaskStatus.IN_PROGRESS))
                .thenReturn(Optional.of(activeTask));
        when(executionAgentService.isTaskFinished(activeTask)).thenReturn(false);

        buddyLifecycleService.processBuddy(1L);

        verify(executionAgentService, never()).completeTask(anyLong());
        verify(executionAgentService, never()).startTask(anyLong());
    }

    @Test
    void processBuddy_activeTaskFinished_completesTask() {
        Buddy buddy = createBuddy(1L);
        VBuddyTask activeTask = createTask(1L, TaskStatus.IN_PROGRESS);

        when(buddyService.findById(1L)).thenReturn(buddy);
        when(taskRepository.findFirstByBuddyIdAndStatusOrderByStartTimeAsc(1L, TaskStatus.IN_PROGRESS))
                .thenReturn(Optional.of(activeTask));
        when(executionAgentService.isTaskFinished(activeTask)).thenReturn(true);

        buddyLifecycleService.processBuddy(1L);

        verify(executionAgentService).completeTask(1L);
        verify(executionAgentService, never()).startTask(anyLong());
    }

    @Test
    void processBuddy_noActiveTask_plannedTaskExists_startsNext() {
        Buddy buddy = createBuddy(1L);
        VBuddyTask plannedTask = createTask(2L, TaskStatus.PLANNED);

        when(buddyService.findById(1L)).thenReturn(buddy);
        when(taskRepository.findFirstByBuddyIdAndStatusOrderByStartTimeAsc(1L, TaskStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());
        when(taskRepository.findFirstByBuddyIdAndStatusOrderByStartTimeAsc(1L, TaskStatus.PLANNED))
                .thenReturn(Optional.of(plannedTask));

        buddyLifecycleService.processBuddy(1L);

        verify(executionAgentService).startTask(2L);
        verify(planningAgentService, never()).planTasks(anyLong());
    }

    @Test
    void processBuddy_noTasks_triggersPlanning() {
        Buddy buddy = createBuddy(1L);

        when(buddyService.findById(1L)).thenReturn(buddy);
        when(taskRepository.findFirstByBuddyIdAndStatusOrderByStartTimeAsc(eq(1L), any()))
                .thenReturn(Optional.empty());
        when(planningAgentService.planTasks(1L)).thenReturn(List.of());

        buddyLifecycleService.processBuddy(1L);

        verify(planningAgentService).planTasks(1L);
    }

    @Test
    void processBuddy_noTasks_planningReturnsNewTasks_startsFirst() {
        Buddy buddy = createBuddy(1L);
        VBuddyTask newTask = createTask(5L, TaskStatus.PLANNED);

        when(buddyService.findById(1L)).thenReturn(buddy);
        when(taskRepository.findFirstByBuddyIdAndStatusOrderByStartTimeAsc(eq(1L), any()))
                .thenReturn(Optional.empty());
        when(planningAgentService.planTasks(1L)).thenReturn(List.of(newTask));

        buddyLifecycleService.processBuddy(1L);

        verify(executionAgentService).startTask(5L);
    }

    private Buddy createBuddy(Long id) {
        Buddy buddy = new Buddy();
        buddy.setId(id);
        buddy.setName("Max");
        buddy.setPersonality("freundlich");
        return buddy;
    }

    private VBuddyTask createTask(Long id, TaskStatus status) {
        VBuddyTask task = new VBuddyTask();
        task.setId(id);
        task.setStatus(status);
        task.setTitle("Test Task");
        return task;
    }
}
