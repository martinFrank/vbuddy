package com.github.martinfrank.vbuddy.controller;

import com.github.martinfrank.vbuddy.controller.exception.EntityNotFoundException;
import com.github.martinfrank.vbuddy.controller.exception.GlobalExceptionHandler;
import com.github.martinfrank.vbuddy.model.ChatMessage;
import com.github.martinfrank.vbuddy.model.MessageRole;
import com.github.martinfrank.vbuddy.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
@Import(GlobalExceptionHandler.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @Test
    void getHistory_returnsChatMessages() throws Exception {
        ChatMessage msg = new ChatMessage();
        msg.setId(1L);
        msg.setRole(MessageRole.USER);
        msg.setContent("Hallo!");
        when(chatService.getHistory(1L)).thenReturn(List.of(msg));

        mockMvc.perform(get("/api/buddies/1/chat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].content", is("Hallo!")))
                .andExpect(jsonPath("$[0].role", is("USER")));
    }

    @Test
    void sendMessage_validRequest_returnsAssistantMessage() throws Exception {
        ChatMessage reply = new ChatMessage();
        reply.setId(2L);
        reply.setRole(MessageRole.ASSISTANT);
        reply.setContent("Hi! Ich bin Max.");
        when(chatService.sendMessage(1L, "Hallo!")).thenReturn(reply);

        mockMvc.perform(post("/api/buddies/1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content": "Hallo!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", is("ASSISTANT")))
                .andExpect(jsonPath("$.content", is("Hi! Ich bin Max.")));
    }

    @Test
    void sendMessage_blankContent_returns400() throws Exception {
        mockMvc.perform(post("/api/buddies/1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content": ""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")));
    }

    @Test
    void sendMessage_buddyNotFound_returns404() throws Exception {
        when(chatService.sendMessage(99L, "Hi")).thenThrow(new EntityNotFoundException("Buddy", 99L));

        mockMvc.perform(post("/api/buddies/99/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content": "Hi"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")));
    }
}
