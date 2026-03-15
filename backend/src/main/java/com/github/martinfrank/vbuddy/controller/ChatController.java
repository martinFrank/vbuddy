package com.github.martinfrank.vbuddy.controller;

import com.github.martinfrank.vbuddy.controller.dto.SendMessageRequest;
import com.github.martinfrank.vbuddy.model.ChatMessage;
import com.github.martinfrank.vbuddy.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buddies/{buddyId}/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping
    public List<ChatMessage> getHistory(@PathVariable Long buddyId) {
        return chatService.getHistory(buddyId);
    }

    @PostMapping
    public ChatMessage sendMessage(@PathVariable Long buddyId,
                                   @Valid @RequestBody SendMessageRequest request) {
        return chatService.sendMessage(buddyId, request.content());
    }
}
