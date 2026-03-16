package com.github.martinfrank.vbuddy.controller;

import com.github.martinfrank.vbuddy.controller.dto.CreateBuddyRequest;
import com.github.martinfrank.vbuddy.model.Buddy;
import com.github.martinfrank.vbuddy.model.BuddyBackground;
import com.github.martinfrank.vbuddy.model.Need;
import com.github.martinfrank.vbuddy.service.BackgroundAgentService;
import com.github.martinfrank.vbuddy.service.BuddyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buddies")
@RequiredArgsConstructor
public class BuddyController {

    private final BuddyService buddyService;
    private final BackgroundAgentService backgroundAgentService;

    @GetMapping
    public List<Buddy> getAll() {
        return buddyService.findAll();
    }

    @GetMapping("/{id}")
    public Buddy getById(@PathVariable Long id) {
        return buddyService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Buddy create(@Valid @RequestBody CreateBuddyRequest request) {
        return buddyService.create(request.name(), request.personality());
    }

    @GetMapping("/{id}/needs")
    public List<Need> getNeeds(@PathVariable Long id) {
        return buddyService.getNeeds(id);
    }

    @GetMapping("/{id}/background")
    public ResponseEntity<BuddyBackground> getBackground(@PathVariable Long id) {
        return backgroundAgentService.getBackground(id)
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
