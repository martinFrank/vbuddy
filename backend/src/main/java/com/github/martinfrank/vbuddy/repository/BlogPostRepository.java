package com.github.martinfrank.vbuddy.repository;

import com.github.martinfrank.vbuddy.model.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    List<BlogPost> findByBuddyIdOrderByCreatedAtDesc(Long buddyId);
}
