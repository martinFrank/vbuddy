package com.github.martinfrank.vbuddy.controller.dto;

import com.github.martinfrank.vbuddy.model.BlogPost;

import java.time.LocalDateTime;

public record BlogPostResponse(
        Long id,
        String title,
        String content,
        LocalDateTime createdAt
) {
    public static BlogPostResponse from(BlogPost post) {
        return new BlogPostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt()
        );
    }
}
