package com.github.martinfrank.vbuddy.repository;

import com.github.martinfrank.vbuddy.model.AiDecisionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiDecisionLogRepository extends JpaRepository<AiDecisionLog, Long> {
    List<AiDecisionLog> findByBuddyIdOrderByCreatedAtDesc(Long buddyId);
}
