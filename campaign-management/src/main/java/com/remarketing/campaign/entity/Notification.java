package com.remarketing.campaign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
public class Notification {
    @Id
    private String id = UUID.randomUUID().toString();
    
    private String campaignId;
    private String userId;
    private String type; // EMAIL or PUSH
    
    @Column(length = 1000)
    private String message;
    
    private String status; // SENT, CLICKED
    
    // Product recommended in this notification
    private String productId;
    private String productName;
    private double productPrice;
}
