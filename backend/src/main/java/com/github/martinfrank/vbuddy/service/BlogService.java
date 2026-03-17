package com.github.martinfrank.vbuddy.service;

import com.github.martinfrank.vbuddy.controller.exception.EntityNotFoundException;
import com.github.martinfrank.vbuddy.model.BlogPost;
import com.github.martinfrank.vbuddy.repository.BlogPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogService {

    private final BlogPostRepository blogPostRepository;

    public List<BlogPost> getPosts(Long buddyId) {
        return blogPostRepository.findByBuddyIdOrderByCreatedAtDesc(buddyId);
    }

    public BlogPost getPost(Long id) {
        return blogPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("BlogPost", id));
    }

    // TODO: LLM-gestützte Blog-Generierung basierend auf Aktivitäten
}
