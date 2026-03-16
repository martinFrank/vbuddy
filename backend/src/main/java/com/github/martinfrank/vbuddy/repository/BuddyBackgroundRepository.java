package com.github.martinfrank.vbuddy.repository;

import com.github.martinfrank.vbuddy.model.BuddyBackground;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BuddyBackgroundRepository extends JpaRepository<BuddyBackground, Long> {
    Optional<BuddyBackground> findByBuddyId(Long buddyId);
}
