package com.remarketing.abtesting.service;

import com.remarketing.abtesting.entity.ABTestAssignment;
import com.remarketing.abtesting.grpc.*;
import com.remarketing.abtesting.repository.ABTestAssignmentRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@GrpcService
public class ABTestServiceImpl extends ABTestingServiceGrpc.ABTestingServiceImplBase {

    private final ABTestAssignmentRepository repository;

    @Autowired
    public ABTestServiceImpl(ABTestAssignmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public void assignVariant(AssignVariantRequest request, StreamObserver<AssignVariantResponse> responseObserver) {
        // Check if user already has an assigned variant for this campaign
        Optional<ABTestAssignment> existingOpt = repository.findByUserIdAndCampaignId(request.getUserId(), request.getCampaignId());
        
        ABTestAssignment assignment;
        if (existingOpt.isPresent()) {
            assignment = existingOpt.get();
            System.out.println("Returning existing Variant " + assignment.getVariant() + " to User " + request.getUserId() + " for Campaign " + request.getCampaignId());
        } else {
            // Simple A/B test variant assignment logic (50% chance)
            String variant = new Random().nextBoolean() ? "A" : "B";
            
            assignment = new ABTestAssignment();
            assignment.setUserId(request.getUserId());
            assignment.setCampaignId(request.getCampaignId());
            assignment.setVariant(variant);
            assignment.setAssignedAt(LocalDateTime.now());
            
            repository.save(assignment);
            System.out.println("Assigned NEW Variant " + variant + " to User " + request.getUserId() + " for Campaign " + request.getCampaignId());
        }
        
        responseObserver.onNext(AssignVariantResponse.newBuilder()
                .setAssignmentId(assignment.getId())
                .setUserId(assignment.getUserId())
                .setCampaignId(assignment.getCampaignId())
                .setVariant(assignment.getVariant())
                .setAssignedAt(assignment.getAssignedAt().toString())
                .setMessage("Variant assigned successfully")
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getTestResults(GetTestResultsRequest request, StreamObserver<GetTestResultsResponse> responseObserver) {
        String campaignId = request.getCampaignId();
        
        int total = repository.countByCampaignId(campaignId);
        int countA = repository.countByCampaignIdAndVariant(campaignId, "A");
        int countB = repository.countByCampaignIdAndVariant(campaignId, "B");
        
        responseObserver.onNext(GetTestResultsResponse.newBuilder()
                .setCampaignId(campaignId)
                .setTotalParticipants(total)
                .setVariantACount(countA)
                .setVariantBCount(countB)
                .setMessage("Test results fetched successfully")
                .build());
        responseObserver.onCompleted();
    }
}
