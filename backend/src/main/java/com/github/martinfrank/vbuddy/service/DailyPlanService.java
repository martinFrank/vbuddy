package com.github.martinfrank.vbuddy.service;

import com.github.martinfrank.vbuddy.model.DailyPlan;
import com.github.martinfrank.vbuddy.repository.DailyPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DailyPlanService {

    private final DailyPlanRepository dailyPlanRepository;

    public List<DailyPlan> getPlans(Long buddyId) {
        return dailyPlanRepository.findByBuddyIdOrderByPlanDateDesc(buddyId);
    }

    public Optional<DailyPlan> getTodayPlan(Long buddyId) {
        return dailyPlanRepository.findByBuddyIdAndPlanDate(buddyId, LocalDate.now());
    }

    // TODO: LLM-gestützte Tagesplan-Generierung
}
