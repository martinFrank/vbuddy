package com.github.martinfrank.vbuddy.repository;

import com.github.martinfrank.vbuddy.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByBuddyIdOrderByCreatedAtAsc(Long buddyId);
}
