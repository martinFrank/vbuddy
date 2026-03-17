package com.github.martinfrank.vbuddy.controller;

import com.github.martinfrank.vbuddy.controller.exception.EntityNotFoundException;
import com.github.martinfrank.vbuddy.controller.exception.GlobalExceptionHandler;
import com.github.martinfrank.vbuddy.model.*;
import com.github.martinfrank.vbuddy.service.BackgroundAgentService;
import com.github.martinfrank.vbuddy.service.BuddyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BuddyController.class)
@Import(GlobalExceptionHandler.class)
class BuddyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BuddyService buddyService;

    @MockitoBean
    private BackgroundAgentService backgroundAgentService;

    @Test
    void getAll_returnsListOfBuddies() throws Exception {
        Buddy buddy = createBuddy(1L, "Max");
        when(buddyService.findAll()).thenReturn(List.of(buddy));

        mockMvc.perform(get("/api/buddies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Max")))
                .andExpect(jsonPath("$[0].currentLocation", is("Zu Hause")));
    }

    @Test
    void getById_existingBuddy_returnsOk() throws Exception {
        Buddy buddy = createBuddy(1L, "Max");
        when(buddyService.findById(1L)).thenReturn(buddy);

        mockMvc.perform(get("/api/buddies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Max")))
                .andExpect(jsonPath("$.personality", is("freundlich")));
    }

    @Test
    void getById_nonExistingBuddy_returns404() throws Exception {
        when(buddyService.findById(99L)).thenThrow(new EntityNotFoundException("Buddy", 99L));

        mockMvc.perform(get("/api/buddies/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("Buddy")));
    }

    @Test
    void create_validRequest_returns201() throws Exception {
        Buddy buddy = createBuddy(1L, "Max");
        when(buddyService.create(anyString(), anyString())).thenReturn(buddy);

        mockMvc.perform(post("/api/buddies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Max", "personality": "freundlich und hilfsbereit"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Max")));
    }

    @Test
    void create_blankName_returns400() throws Exception {
        mockMvc.perform(post("/api/buddies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "personality": "freundlich"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")));
    }

    @Test
    void create_missingFields_returns400() throws Exception {
        mockMvc.perform(post("/api/buddies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getNeeds_returnsNeedsList() throws Exception {
        Need need = new Need();
        need.setId(1L);
        need.setNeedType(NeedType.HUNGER);
        need.setCurrentValue(60.0);
        need.setMaxValue(100.0);
        need.setDecayRatePerHour(5.0);
        when(buddyService.getNeeds(1L)).thenReturn(List.of(need));

        mockMvc.perform(get("/api/buddies/1/needs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].needType", is("HUNGER")))
                .andExpect(jsonPath("$[0].currentValue", is(60.0)));
    }

    @Test
    void getBackground_exists_returnsOk() throws Exception {
        BuddyBackground bg = new BuddyBackground();
        bg.setId(1L);
        bg.setNarrativeText("Eine Geschichte...");
        bg.setStatus(BackgroundStatus.COMPLETED);
        when(backgroundAgentService.getBackground(1L)).thenReturn(Optional.of(bg));

        mockMvc.perform(get("/api/buddies/1/background"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.narrativeText", is("Eine Geschichte...")))
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }

    @Test
    void getBackground_notExists_returns404() throws Exception {
        when(backgroundAgentService.getBackground(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/buddies/1/background"))
                .andExpect(status().isNotFound());
    }

    @Test
    void generateBackground_returns202() throws Exception {
        Buddy buddy = createBuddy(1L, "Max");
        when(buddyService.findById(1L)).thenReturn(buddy);

        mockMvc.perform(post("/api/buddies/1/background/generate"))
                .andExpect(status().isAccepted());

        verify(backgroundAgentService).generateBackground(1L);
    }

    private Buddy createBuddy(Long id, String name) {
        Buddy buddy = new Buddy();
        buddy.setId(id);
        buddy.setName(name);
        buddy.setPersonality("freundlich");
        return buddy;
    }
}
