package com.github.martinfrank.vbuddy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "buddy")
@Getter
@Setter
@NoArgsConstructor
public class Buddy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String personality;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "buddy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Need> needs = new ArrayList<>();

    @OneToMany(mappedBy = "buddy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailyPlan> dailyPlans = new ArrayList<>();

    @OneToMany(mappedBy = "buddy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlogPost> blogPosts = new ArrayList<>();

    @OneToMany(mappedBy = "buddy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> chatMessages = new ArrayList<>();
}
