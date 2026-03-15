package com.github.martinfrank.vbuddy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "need")
@Getter
@Setter
@NoArgsConstructor
public class Need {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buddy_id", nullable = false)
    @JsonIgnore
    private Buddy buddy;

    @Enumerated(EnumType.STRING)
    @Column(name = "need_type", nullable = false, length = 50)
    private NeedType needType;

    @Column(name = "current_value", nullable = false)
    private double currentValue = 50.0;

    @Column(name = "max_value", nullable = false)
    private double maxValue = 100.0;

    @Column(name = "decay_rate_per_hour", nullable = false)
    private double decayRatePerHour = 5.0;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
}
