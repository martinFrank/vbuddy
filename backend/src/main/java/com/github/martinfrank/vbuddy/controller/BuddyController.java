package com.github.martinfrank.vbuddy.controller;

import com.github.martinfrank.vbuddy.controller.dto.CreateBuddyRequest;
import com.github.martinfrank.vbuddy.model.Buddy;
import com.github.martinfrank.vbuddy.model.Need;
import com.github.martinfrank.vbuddy.service.BuddyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buddies")
@RequiredArgsConstructor
public class BuddyController {

    private final BuddyService buddyService;

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
}
