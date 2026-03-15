package com.github.martinfrank.vbuddy.repository;

import com.github.martinfrank.vbuddy.model.TaskStatus;
import com.github.martinfrank.vbuddy.model.VBuddyTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VBuddyTaskRepository extends JpaRepository<VBuddyTask, Long> {
    List<VBuddyTask> findByBuddyIdOrderByStartTimeAsc(Long buddyId);
    List<VBuddyTask> findByBuddyIdAndStatusOrderByStartTimeAsc(Long buddyId, TaskStatus status);
    List<VBuddyTask> findByBuddyIdAndStatusInOrderByStartTimeDesc(Long buddyId, List<TaskStatus> statuses);
    boolean existsByBuddyIdAndStatus(Long buddyId, TaskStatus status);
    Optional<VBuddyTask> findFirstByBuddyIdAndStatusOrderByStartTimeAsc(Long buddyId, TaskStatus status);
    List<VBuddyTask> findByBuddyIdAndStartTimeBetweenOrderByStartTimeAsc(Long buddyId, LocalDateTime from, LocalDateTime to);
}
