package com.github.martinfrank.vbuddy.controller;

import com.github.martinfrank.vbuddy.model.DailyPlan;
import com.github.martinfrank.vbuddy.service.DailyPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buddies/{buddyId}/daily-plans")
@RequiredArgsConstructor
public class DailyPlanController {

    private final DailyPlanService dailyPlanService;

    @GetMapping
    public List<DailyPlan> getPlans(@PathVariable Long buddyId) {
        return dailyPlanService.getPlans(buddyId);
    }

    @GetMapping("/today")
    public ResponseEntity<DailyPlan> getTodayPlan(@PathVariable Long buddyId) {
        return dailyPlanService.getTodayPlan(buddyId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
