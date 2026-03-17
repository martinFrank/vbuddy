package com.github.martinfrank.vbuddy.controller;

import com.github.martinfrank.vbuddy.controller.dto.BlogPostResponse;
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
    public List<BlogPostResponse> getPosts(@PathVariable Long buddyId) {
        return blogService.getPosts(buddyId).stream().map(BlogPostResponse::from).toList();
    }

    @GetMapping("/{postId}")
    public BlogPostResponse getPost(@PathVariable Long buddyId, @PathVariable Long postId) {
        return BlogPostResponse.from(blogService.getPost(postId));
    }
}
