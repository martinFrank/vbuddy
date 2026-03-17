package com.github.martinfrank.vbuddy.controller;

import com.github.martinfrank.vbuddy.controller.dto.AiDecisionLogResponse;
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
    public List<AiDecisionLogResponse> getLogs(@PathVariable Long buddyId) {
        return aiDecisionLogRepository.findByBuddyIdOrderByCreatedAtDesc(buddyId)
                .stream().map(AiDecisionLogResponse::from).toList();
    }
}
