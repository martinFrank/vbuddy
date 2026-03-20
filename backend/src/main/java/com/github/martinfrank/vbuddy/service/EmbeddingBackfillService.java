package com.github.martinfrank.vbuddy.service;

import com.github.martinfrank.vbuddy.model.BackgroundStatus;
import com.github.martinfrank.vbuddy.model.BuddyBackground;
import com.github.martinfrank.vbuddy.model.VBuddyTask;
import com.github.martinfrank.vbuddy.repository.BuddyBackgroundRepository;
import com.github.martinfrank.vbuddy.repository.VBuddyTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmbeddingBackfillService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingBackfillService.class);

    private final BuddyBackgroundRepository backgroundRepository;
    private final VBuddyTaskRepository taskRepository;
    private final EmbeddingService embeddingService;

    public EmbeddingBackfillService(BuddyBackgroundRepository backgroundRepository,
                                     VBuddyTaskRepository taskRepository, EmbeddingService embeddingService) {
        this.backgroundRepository = backgroundRepository;
        this.taskRepository = taskRepository;
        this.embeddingService = embeddingService;
    }

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

        List<VBuddyTask> tasks = taskRepository.findAll();
        for (VBuddyTask task : tasks) {
            try {
                embeddingService.embedTask(task);
                taskCount++;
            } catch (Exception e) {
                log.error("Failed to embed task {} for buddy {}", task.getId(), task.getBuddy().getId(), e);
            }
        }

        log.info("Backfill completed: {} backgrounds, {} tasks embedded", backgroundCount, taskCount);
    }
}
