package com.github.martinfrank.vbuddy.repository;

import com.github.martinfrank.vbuddy.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
}
