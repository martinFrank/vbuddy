package com.github.martinfrank.vbuddy.repository;

import com.github.martinfrank.vbuddy.model.Buddy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuddyRepository extends JpaRepository<Buddy, Long> {
}
