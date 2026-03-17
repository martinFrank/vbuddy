package com.github.martinfrank.vbuddy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "buddy_background")
@Getter
@Setter
@NoArgsConstructor
public class BuddyBackground {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buddy_id", nullable = false, unique = true)
    @JsonIgnore
    private Buddy buddy;

    @Column(name = "structured_data", columnDefinition = "TEXT")
    private String structuredData;

    @Column(name = "narrative_text", columnDefinition = "TEXT")
    private String narrativeText;

    @Column(name = "weekly_schedule", columnDefinition = "TEXT")
    private String weeklySchedule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BackgroundStatus status = BackgroundStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
