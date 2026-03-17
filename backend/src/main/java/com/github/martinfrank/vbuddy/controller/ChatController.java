package com.github.martinfrank.vbuddy.controller;

import com.github.martinfrank.vbuddy.controller.dto.ChatMessageResponse;
import com.github.martinfrank.vbuddy.controller.dto.SendMessageRequest;
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
    public List<ChatMessageResponse> getHistory(@PathVariable Long buddyId) {
        return chatService.getHistory(buddyId).stream().map(ChatMessageResponse::from).toList();
    }

    @PostMapping
    public ChatMessageResponse sendMessage(@PathVariable Long buddyId,
                                           @Valid @RequestBody SendMessageRequest request) {
        return ChatMessageResponse.from(chatService.sendMessage(buddyId, request.content()));
    }
}
