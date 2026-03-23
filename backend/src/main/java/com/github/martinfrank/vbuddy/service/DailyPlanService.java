package com.github.martinfrank.vbuddy.service;

import com.github.martinfrank.vbuddy.model.DailyPlan;
import com.github.martinfrank.vbuddy.repository.DailyPlanRepository;
import org.springframework.stereotype.Service;

import com.github.martinfrank.vbuddy.util.UtcDateTimeUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DailyPlanService {

    private final DailyPlanRepository dailyPlanRepository;

    public DailyPlanService(DailyPlanRepository dailyPlanRepository) {
        this.dailyPlanRepository = dailyPlanRepository;
    }

    public List<DailyPlan> getPlans(Long buddyId) {
        return dailyPlanRepository.findByBuddyIdOrderByPlanDateDesc(buddyId);
    }

    public Optional<DailyPlan> getTodayPlan(Long buddyId) {
        return dailyPlanRepository.findByBuddyIdAndPlanDate(buddyId, UtcDateTimeUtil.today());
    }

    // TODO: LLM-gestützte Tagesplan-Generierung
}
