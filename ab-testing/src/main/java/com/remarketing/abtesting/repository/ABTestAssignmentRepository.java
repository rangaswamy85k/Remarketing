package com.remarketing.abtesting.repository;

import com.remarketing.abtesting.entity.ABTestAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ABTestAssignmentRepository extends JpaRepository<ABTestAssignment, String> {
    Optional<ABTestAssignment> findByUserIdAndCampaignId(String userId, String campaignId);
    int countByCampaignIdAndVariant(String campaignId, String variant);
    int countByCampaignId(String campaignId);
}
