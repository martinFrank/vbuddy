package com.github.martinfrank.vbuddy.service;

import com.github.martinfrank.vbuddy.model.BackgroundStatus;
import com.github.martinfrank.vbuddy.model.BuddyBackground;
import com.github.martinfrank.vbuddy.model.TaskStatus;
import com.github.martinfrank.vbuddy.model.VBuddyTask;
import com.github.martinfrank.vbuddy.repository.BuddyBackgroundRepository;
import com.github.martinfrank.vbuddy.repository.VBuddyTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingBackfillService {

    private final BuddyBackgroundRepository backgroundRepository;
    private final VBuddyTaskRepository taskRepository;
    private final EmbeddingService embeddingService;

    public void backfillAll() {
        int backgroundCount = 0;
        int taskCount = 0;

        List<BuddyBackground> backgrounds = backgroundRepository.findAll();
        for (BuddyBackground bg : backgrounds) {
            if (bg.getStatus() == BackgroundStatus.COMPLETED && bg.getNarrativeText() != null) {
                try {
                    embeddingService.embedBackground(bg.getBuddy().getId(), bg.getNarrativeText());
                    backgroundCount++;
                } catch (Exception e) {
                    log.error("Failed to embed background for buddy {}", bg.getBuddy().getId(), e);
                }
            }
        }

        List<VBuddyTask> completedTasks = taskRepository.findAll().stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                .toList();
        for (VBuddyTask task : completedTasks) {
            try {
                embeddingService.embedCompletedTask(
                        task.getBuddy().getId(), task.getId(),
                        task.getTitle(), task.getDescription(), task.getLocation());
                taskCount++;
            } catch (Exception e) {
                log.error("Failed to embed task {} for buddy {}", task.getId(), task.getBuddy().getId(), e);
            }
        }

        log.info("Backfill completed: {} backgrounds, {} tasks embedded", backgroundCount, taskCount);
    }
}
