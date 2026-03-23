package com.github.martinfrank.vbuddy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.martinfrank.vbuddy.util.UtcDateTimeUtil;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "daily_plan")
public class DailyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buddy_id", nullable = false)
    @JsonIgnore
    private Buddy buddy;

    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = UtcDateTimeUtil.now();

    @OneToMany(mappedBy = "dailyPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startTime ASC")
    private List<Activity> activities = new ArrayList<>();

    public DailyPlan() {
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

    public LocalDate getPlanDate() {
        return planDate;
    }

    public void setPlanDate(LocalDate planDate) {
        this.planDate = planDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }
}
