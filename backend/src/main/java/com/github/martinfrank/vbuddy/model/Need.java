package com.github.martinfrank.vbuddy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "need")
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

    public Need() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Buddy getBuddy() {
        return buddy;
    }

    public void setBuddy(Buddy buddy) {
        this.buddy = buddy;
    }

    public NeedType getNeedType() {
        return needType;
    }

    public void setNeedType(NeedType needType) {
        this.needType = needType;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }

    public double getDecayRatePerHour() {
        return decayRatePerHour;
    }

    public void setDecayRatePerHour(double decayRatePerHour) {
        this.decayRatePerHour = decayRatePerHour;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
