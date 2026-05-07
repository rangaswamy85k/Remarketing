package com.remarketing.campaign.repository;

import com.remarketing.campaign.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByCampaignId(String campaignId);
    Optional<Notification> findFirstByCampaignIdAndUserId(String campaignId, String userId);
}
