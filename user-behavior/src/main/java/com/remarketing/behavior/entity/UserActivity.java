package com.remarketing.behavior.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_activities")
@Data
public class UserActivity {
    @Id
    private String id = UUID.randomUUID().toString();
    
    private String userId;
    private String activityType; // SEARCH, ADD_TO_CART, CLICK, CHECKOUT
    private String details;
    
    private LocalDateTime timestamp = LocalDateTime.now();
}
