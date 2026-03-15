package com.github.martinfrank.vbuddy.controller;

import com.github.martinfrank.vbuddy.model.AiDecisionLog;
import com.github.martinfrank.vbuddy.repository.AiDecisionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buddies/{buddyId}/ai-decision-log")
@RequiredArgsConstructor
public class AiDecisionLogController {

    private final AiDecisionLogRepository aiDecisionLogRepository;

    @GetMapping
    public List<AiDecisionLog> getLogs(@PathVariable Long buddyId) {
        return aiDecisionLogRepository.findByBuddyIdOrderByCreatedAtDesc(buddyId);
    }
}
