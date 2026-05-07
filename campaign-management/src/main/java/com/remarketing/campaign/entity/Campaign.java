package com.remarketing.campaign.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "campaigns")
@Data
public class Campaign {
    @Id
    private String id = UUID.randomUUID().toString();
    private String name;
    private double budget;
    private String segmentId;
    
    // Tracking fields
    private int notificationsSent = 0;
    private int usersConverted = 0;
    private double revenueGenerated = 0.0;
    
    @Transient
    private CampaignState state = CampaignState.DRAFT;

    private String statusStr = state.name();

    public void updateState(CampaignState newState) {
        this.state = newState;
        this.statusStr = newState.name();
    }

    public String handleCampaignState() {
        switch (state) {
            case DRAFT: return "Campaign is in draft, not charging budget.";
            case ACTIVE: return "Campaign is active, deducting budget.";
            case PAUSED: return "Campaign is paused temporarily.";
            case COMPLETED: return "Campaign has completed successfully.";
            case TERMINATED: return "Campaign was terminated early.";
            default: return "Unknown state";
        }
    }
}
