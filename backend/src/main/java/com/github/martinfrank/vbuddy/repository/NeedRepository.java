package com.github.martinfrank.vbuddy.repository;

import com.github.martinfrank.vbuddy.model.Need;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NeedRepository extends JpaRepository<Need, Long> {
    List<Need> findByBuddyId(Long buddyId);
}
