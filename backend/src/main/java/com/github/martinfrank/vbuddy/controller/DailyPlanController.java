package com.github.martinfrank.vbuddy.controller;

import com.github.martinfrank.vbuddy.controller.dto.DailyPlanResponse;
import com.github.martinfrank.vbuddy.service.DailyPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buddies/{buddyId}/daily-plans")
public class DailyPlanController {

    private final DailyPlanService dailyPlanService;

    public DailyPlanController(DailyPlanService dailyPlanService) {
        this.dailyPlanService = dailyPlanService;
    }

    @GetMapping
    public List<DailyPlanResponse> getPlans(@PathVariable Long buddyId) {
        return dailyPlanService.getPlans(buddyId).stream().map(DailyPlanResponse::from).toList();
    }

    @GetMapping("/today")
    public ResponseEntity<DailyPlanResponse> getTodayPlan(@PathVariable Long buddyId) {
        return dailyPlanService.getTodayPlan(buddyId)
                .map(DailyPlanResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
