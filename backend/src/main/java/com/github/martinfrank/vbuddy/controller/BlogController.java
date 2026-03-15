package com.github.martinfrank.vbuddy.controller;

import com.github.martinfrank.vbuddy.model.BlogPost;
import com.github.martinfrank.vbuddy.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buddies/{buddyId}/blog-posts")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    @GetMapping
    public List<BlogPost> getPosts(@PathVariable Long buddyId) {
        return blogService.getPosts(buddyId);
    }

    @GetMapping("/{postId}")
    public BlogPost getPost(@PathVariable Long buddyId, @PathVariable Long postId) {
        return blogService.getPost(postId);
    }
}
