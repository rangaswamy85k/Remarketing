package com.remarketing.abtesting.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ab_test_assignments")
@Data
public class ABTestAssignment {

    @Id
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String campaignId;

    @Column(nullable = false)
    private String variant; // "A" or "B"

    @Column(nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();
}
