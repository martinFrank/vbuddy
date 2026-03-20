package com.github.martinfrank.vbuddy.controller;

import com.github.martinfrank.vbuddy.controller.dto.BuddyBackgroundResponse;
import com.github.martinfrank.vbuddy.controller.dto.BuddyResponse;
import com.github.martinfrank.vbuddy.controller.dto.CreateBuddyRequest;
import com.github.martinfrank.vbuddy.controller.dto.NeedResponse;
import com.github.martinfrank.vbuddy.service.BackgroundAgentService;
import com.github.martinfrank.vbuddy.service.BuddyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buddies")
public class BuddyController {

    private final BuddyService buddyService;
    private final BackgroundAgentService backgroundAgentService;

    public BuddyController(BuddyService buddyService, BackgroundAgentService backgroundAgentService) {
        this.buddyService = buddyService;
        this.backgroundAgentService = backgroundAgentService;
    }

    @GetMapping
    public List<BuddyResponse> getAll() {
        return buddyService.findAll().stream().map(BuddyResponse::from).toList();
    }

    @GetMapping("/{id}")
    public BuddyResponse getById(@PathVariable Long id) {
        return BuddyResponse.from(buddyService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BuddyResponse create(@Valid @RequestBody CreateBuddyRequest request) {
        return BuddyResponse.from(buddyService.create(request.name(), request.personality()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        buddyService.delete(id);
    }

    @GetMapping("/{id}/needs")
    public List<NeedResponse> getNeeds(@PathVariable Long id) {
        return buddyService.getNeeds(id).stream().map(NeedResponse::from).toList();
    }

    @GetMapping("/{id}/background")
    public ResponseEntity<BuddyBackgroundResponse> getBackground(@PathVariable Long id) {
        return backgroundAgentService.getBackground(id)
                .map(BuddyBackgroundResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/background/generate")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void generateBackground(@PathVariable Long id) {
        buddyService.findById(id); // verify buddy exists
        backgroundAgentService.generateBackground(id);
    }
}
