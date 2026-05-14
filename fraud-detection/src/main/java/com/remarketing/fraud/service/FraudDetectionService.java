package com.remarketing.fraud.service;

import com.remarketing.fraud.model.FraudAlert;
import com.remarketing.fraud.repository.FraudAlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.util.List;

@Service
public class FraudDetectionService {
    private static final Logger log = LoggerFactory.getLogger(FraudDetectionService.class);

    @Autowired
    private FraudAlertRepository repository;

    public FraudAlert createAlert(String campaignId, String alertType, String description) {
        FraudAlert alert = new FraudAlert();
        alert.setCampaignId(campaignId);
        alert.setAlertType(alertType);
        alert.setDescription(description);
        repository.save(alert);
        log.info("Fraud alert created: {} for campaign {}", alert.getId(), campaignId);
        return alert;
    }

    public List<FraudAlert> listAlerts(String campaignId, Instant from, Instant to) {
        return repository.findByCampaignIdAndCreatedAtBetween(campaignId, from, to);
    }
}
