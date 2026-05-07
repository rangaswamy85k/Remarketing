package com.remarketing.execution.service;

import org.springframework.stereotype.Service;

@Service
public class ExecutionServiceImpl {
    public void sendEmailCampaign(String userId, String campaignId) {
        System.out.println("Sending email for campaign " + campaignId + " to user " + userId);
        // Future DB integration: Log delivery status in MySQL
    }
}
