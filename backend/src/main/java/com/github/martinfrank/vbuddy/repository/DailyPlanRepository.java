package com.github.martinfrank.vbuddy.repository;

import com.github.martinfrank.vbuddy.model.DailyPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyPlanRepository extends JpaRepository<DailyPlan, Long> {
    List<DailyPlan> findByBuddyIdOrderByPlanDateDesc(Long buddyId);
    Optional<DailyPlan> findByBuddyIdAndPlanDate(Long buddyId, LocalDate planDate);
}
