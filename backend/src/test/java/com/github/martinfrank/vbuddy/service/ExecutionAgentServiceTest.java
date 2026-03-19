package com.github.martinfrank.vbuddy.service;

import com.github.martinfrank.vbuddy.ai.ExecutionAiService;
import com.github.martinfrank.vbuddy.ai.NeedAdjustment;
import com.github.martinfrank.vbuddy.ai.TaskExecutionResult;
import com.github.martinfrank.vbuddy.controller.exception.EntityNotFoundException;
import com.github.martinfrank.vbuddy.controller.exception.InvalidStateException;
import com.github.martinfrank.vbuddy.model.*;
import com.github.martinfrank.vbuddy.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExecutionAgentServiceTest {

    @Mock
    private ExecutionAiService executionAiService;
    @Mock
    private BuddyService buddyService;
    @Mock
    private VBuddyTaskRepository taskRepository;
    @Mock
    private NeedRepository needRepository;
    @Mock
    private BuddyRepository buddyRepository;
    @Mock
    private BlogPostRepository blogPostRepository;
    @Mock
    private AiDecisionLogRepository aiDecisionLogRepository;
    @Mock
    private EmbeddingService embeddingService;
    @Mock
    private WordPressService wordPressService;
    @Mock
    private SearxngSearchService searxngSearchService;

    @InjectMocks
    private ExecutionAgentService executionAgentService;

    @Test
    void startTask_plannedTask_setsStatusToInProgress() {
        VBuddyTask task = createTask(1L, TaskStatus.PLANNED);
        Buddy buddy = createBuddy(1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(buddyService.findById(1L)).thenReturn(buddy);

        VBuddyTask result = executionAgentService.startTask(1L);

        assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(result.getStartTime()).isNotNull();
        verify(taskRepository).save(task);
        verify(buddyRepository).save(buddy);
    }

    @Test
    void startTask_nonExistingTask_throwsEntityNotFoundException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> executionAgentService.startTask(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Task");
    }

    @Test
    void startTask_notPlannedTask_throwsInvalidStateException() {
        VBuddyTask task = createTask(1L, TaskStatus.COMPLETED);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> executionAgentService.startTask(1L))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining("PLANNED");
    }

    @Test
    void completeTask_inProgressTask_setsStatusToCompleted() {
        VBuddyTask task = createTask(1L, TaskStatus.IN_PROGRESS);
        Buddy buddy = createBuddy(1L);
        Need hungerNeed = createNeed(NeedType.HUNGER, 60.0);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(buddyService.findById(1L)).thenReturn(buddy);
        when(buddyService.getNeeds(1L)).thenReturn(List.of(hungerNeed));
        when(executionAiService.executeTask(anyString(), anyString(), anyString(), anyString(), anyInt(), anyString()))
                .thenReturn(new TaskExecutionResult(
                        "Blog Titel",
                        "Blog Inhalt",
                        List.of(new NeedAdjustment("HUNGER", -20.0)),
                        "Reasoning"
                ));

        VBuddyTask result = executionAgentService.completeTask(1L);

        assertThat(result.getStatus()).isEqualTo(TaskStatus.COMPLETED);
        verify(blogPostRepository).save(any(BlogPost.class));
        verify(aiDecisionLogRepository).save(any(AiDecisionLog.class));
    }

    @Test
    void completeTask_adjustsNeedsWithinBounds() {
        VBuddyTask task = createTask(1L, TaskStatus.IN_PROGRESS);
        Buddy buddy = createBuddy(1L);
        Need hungerNeed = createNeed(NeedType.HUNGER, 15.0);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(buddyService.findById(1L)).thenReturn(buddy);
        when(buddyService.getNeeds(1L)).thenReturn(List.of(hungerNeed));
        when(executionAiService.executeTask(anyString(), anyString(), anyString(), anyString(), anyInt(), anyString()))
                .thenReturn(new TaskExecutionResult(
                        "Titel",
                        "Inhalt",
                        List.of(new NeedAdjustment("HUNGER", -30.0)),
                        "Reasoning"
                ));

        executionAgentService.completeTask(1L);

        ArgumentCaptor<Need> needCaptor = ArgumentCaptor.forClass(Need.class);
        verify(needRepository).save(needCaptor.capture());
        assertThat(needCaptor.getValue().getCurrentValue()).isEqualTo(0.0);
    }

    @Test
    void completeTask_notInProgress_throwsInvalidStateException() {
        VBuddyTask task = createTask(1L, TaskStatus.PLANNED);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> executionAgentService.completeTask(1L))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining("IN_PROGRESS");
    }

    @Test
    void completeTask_embeddingFailure_doesNotPreventCompletion() {
        VBuddyTask task = createTask(1L, TaskStatus.IN_PROGRESS);
        Buddy buddy = createBuddy(1L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(buddyService.findById(1L)).thenReturn(buddy);
        when(buddyService.getNeeds(1L)).thenReturn(List.of());
        when(executionAiService.executeTask(anyString(), anyString(), anyString(), anyString(), anyInt(), anyString()))
                .thenReturn(new TaskExecutionResult("Titel", "Inhalt", List.of(), "Reasoning"));
        doThrow(new RuntimeException("Embedding failed"))
                .when(embeddingService).embedTask(any(VBuddyTask.class));

        VBuddyTask result = executionAgentService.completeTask(1L);

        assertThat(result.getStatus()).isEqualTo(TaskStatus.COMPLETED);
    }

    @Test
    void isTaskFinished_taskStillRunning_returnsFalse() {
        VBuddyTask task = new VBuddyTask();
        task.setStartTime(LocalDateTime.now().minusMinutes(10));
        task.setDurationMinutes(60);

        assertThat(executionAgentService.isTaskFinished(task)).isFalse();
    }

    @Test
    void isTaskFinished_taskExpired_returnsTrue() {
        VBuddyTask task = new VBuddyTask();
        task.setStartTime(LocalDateTime.now().minusMinutes(120));
        task.setDurationMinutes(60);

        assertThat(executionAgentService.isTaskFinished(task)).isTrue();
    }

    @Test
    void startTask_updatesLocation() {
        VBuddyTask task = createTask(1L, TaskStatus.PLANNED);
        task.setLocation("Im Park");
        Buddy buddy = createBuddy(1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(buddyService.findById(1L)).thenReturn(buddy);

        executionAgentService.startTask(1L);

        ArgumentCaptor<Buddy> buddyCaptor = ArgumentCaptor.forClass(Buddy.class);
        verify(buddyRepository).save(buddyCaptor.capture());
        assertThat(buddyCaptor.getValue().getCurrentLocation()).isEqualTo("Im Park");
    }

    private VBuddyTask createTask(Long id, TaskStatus status) {
        VBuddyTask task = new VBuddyTask();
        task.setId(id);
        task.setStatus(status);
        task.setTitle("Test Task");
        task.setDescription("Test Beschreibung");
        task.setLocation("Zu Hause");
        task.setDurationMinutes(30);
        task.setStartTime(LocalDateTime.now().minusMinutes(5));
        Buddy buddy = createBuddy(1L);
        task.setBuddy(buddy);
        return task;
    }

    private Buddy createBuddy(Long id) {
        Buddy buddy = new Buddy();
        buddy.setId(id);
        buddy.setName("Max");
        buddy.setPersonality("freundlich");
        return buddy;
    }

    private Need createNeed(NeedType type, double currentValue) {
        Need need = new Need();
        need.setNeedType(type);
        need.setCurrentValue(currentValue);
        need.setMaxValue(100.0);
        return need;
    }
}
