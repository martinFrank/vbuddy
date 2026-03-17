package com.github.martinfrank.vbuddy.service;

import com.github.martinfrank.vbuddy.controller.exception.EntityNotFoundException;
import com.github.martinfrank.vbuddy.model.BlogPost;
import com.github.martinfrank.vbuddy.repository.BlogPostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogServiceTest {

    @Mock
    private BlogPostRepository blogPostRepository;

    @InjectMocks
    private BlogService blogService;

    @Test
    void getPosts_returnsPosts() {
        BlogPost post = new BlogPost();
        post.setTitle("Mein Tag im Park");
        when(blogPostRepository.findByBuddyIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(post));

        List<BlogPost> result = blogService.getPosts(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitle()).isEqualTo("Mein Tag im Park");
    }

    @Test
    void getPost_existingPost_returnsPost() {
        BlogPost post = new BlogPost();
        post.setId(1L);
        post.setTitle("Mein Tag im Park");
        when(blogPostRepository.findById(1L)).thenReturn(Optional.of(post));

        BlogPost result = blogService.getPost(1L);

        assertThat(result.getTitle()).isEqualTo("Mein Tag im Park");
    }

    @Test
    void getPost_nonExistingPost_throwsEntityNotFoundException() {
        when(blogPostRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blogService.getPost(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("BlogPost")
                .hasMessageContaining("99");
    }
}
