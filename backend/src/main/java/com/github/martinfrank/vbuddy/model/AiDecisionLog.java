package com.github.martinfrank.vbuddy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.martinfrank.vbuddy.util.UtcDateTimeUtil;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_decision_log")
public class AiDecisionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buddy_id", nullable = false)
    @JsonIgnore
    private Buddy buddy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String context;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String decision;

    @Column(columnDefinition = "TEXT")
    private String reasoning;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = UtcDateTimeUtil.now();

    public AiDecisionLog() {
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

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
