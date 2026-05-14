package com.remarketing.fraud.repository;

import com.remarketing.fraud.model.FraudAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface FraudAlertRepository extends JpaRepository<FraudAlert, String> {
    List<FraudAlert> findByCampaignIdAndCreatedAtBetween(String campaignId, Instant from, Instant to);
}
