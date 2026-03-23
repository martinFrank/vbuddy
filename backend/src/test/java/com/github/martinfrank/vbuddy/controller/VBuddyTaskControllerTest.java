package com.github.martinfrank.vbuddy.controller;

import com.github.martinfrank.vbuddy.controller.exception.EntityNotFoundException;
import com.github.martinfrank.vbuddy.controller.exception.GlobalExceptionHandler;
import com.github.martinfrank.vbuddy.controller.exception.InvalidStateException;
import com.github.martinfrank.vbuddy.model.TaskStatus;
import com.github.martinfrank.vbuddy.model.VBuddyTask;
import com.github.martinfrank.vbuddy.repository.VBuddyTaskRepository;
import com.github.martinfrank.vbuddy.service.BuddyLifecycleService;
import com.github.martinfrank.vbuddy.service.ExecutionAgentService;
import com.github.martinfrank.vbuddy.service.PlanningAgentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.github.martinfrank.vbuddy.util.UtcDateTimeUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VBuddyTaskController.class)
@Import(GlobalExceptionHandler.class)
class VBuddyTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VBuddyTaskRepository taskRepository;
    @MockitoBean
    private PlanningAgentService planningAgentService;
    @MockitoBean
    private ExecutionAgentService executionAgentService;
    @MockitoBean
    private BuddyLifecycleService buddyLifecycleService;

    @Test
    void getTasks_returnsList() throws Exception {
        VBuddyTask task = createTask(1L, "Frühstück", TaskStatus.PLANNED);
        when(taskRepository.findByBuddyIdOrderByStartTimeAsc(1L)).thenReturn(List.of(task));

        mockMvc.perform(get("/api/buddies/1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Frühstück")))
                .andExpect(jsonPath("$[0].status", is("PLANNED")));
    }

    @Test
    void getCurrentTask_exists_returnsOk() throws Exception {
        VBuddyTask task = createTask(1L, "Joggen", TaskStatus.IN_PROGRESS);
        when(taskRepository.findFirstByBuddyIdAndStatusOrderByStartTimeAsc(1L, TaskStatus.IN_PROGRESS))
                .thenReturn(Optional.of(task));

        mockMvc.perform(get("/api/buddies/1/tasks/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Joggen")))
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")));
    }

    @Test
    void getCurrentTask_notExists_returns404() throws Exception {
        when(taskRepository.findFirstByBuddyIdAndStatusOrderByStartTimeAsc(1L, TaskStatus.IN_PROGRESS))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/buddies/1/tasks/current"))
                .andExpect(status().isNotFound());
    }

    @Test
    void planTasks_returns201() throws Exception {
        VBuddyTask task = createTask(1L, "Neuer Task", TaskStatus.PLANNED);
        when(planningAgentService.planTasks(1L)).thenReturn(List.of(task));

        mockMvc.perform(post("/api/buddies/1/tasks/plan"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Neuer Task")));
    }

    @Test
    void executeTask_success_returnsTask() throws Exception {
        VBuddyTask task = createTask(1L, "Fertig", TaskStatus.COMPLETED);
        when(executionAgentService.executeTask(1L)).thenReturn(task);

        mockMvc.perform(post("/api/buddies/1/tasks/1/execute"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    void executeTask_taskNotFound_returns404() throws Exception {
        when(executionAgentService.executeTask(99L)).thenThrow(new EntityNotFoundException("Task", 99L));

        mockMvc.perform(post("/api/buddies/1/tasks/99/execute"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")));
    }

    @Test
    void executeTask_invalidState_returns409() throws Exception {
        when(executionAgentService.executeTask(1L))
                .thenThrow(new InvalidStateException("Task is not in PLANNED status: 1"));

        mockMvc.perform(post("/api/buddies/1/tasks/1/execute"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("INVALID_STATE")));
    }

    @Test
    void getTimeline_returnsTasks() throws Exception {
        VBuddyTask task = createTask(1L, "Mittag", TaskStatus.COMPLETED);
        when(taskRepository.findByBuddyIdAndStartTimeBetweenOrderByStartTimeAsc(eq(1L), any(), any()))
                .thenReturn(List.of(task));

        mockMvc.perform(get("/api/buddies/1/tasks/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Mittag")));
    }

    private VBuddyTask createTask(Long id, String title, TaskStatus status) {
        VBuddyTask task = new VBuddyTask();
        task.setId(id);
        task.setTitle(title);
        task.setDescription("Beschreibung");
        task.setLocation("Zu Hause");
        task.setStartTime(UtcDateTimeUtil.now());
        task.setDurationMinutes(30);
        task.setStatus(status);
        return task;
    }
}
