package com.remarketing.abtesting.service;

import com.remarketing.abtesting.grpc.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Random;

@GrpcService
public class ABTestServiceImpl extends ABTestingServiceGrpc.ABTestingServiceImplBase {

    @Override
    public void assignVariant(AssignVariantRequest request, StreamObserver<AssignVariantResponse> responseObserver) {
        // Simple A/B test variant assignment logic
        // 50% chance to get Variant A (e.g., EMAIL), 50% for Variant B (e.g., PUSH)
        String variant = new Random().nextBoolean() ? "A" : "B";
        
        System.out.println("Assigned Variant " + variant + " to User " + request.getUserId() + " for Campaign " + request.getCampaignId());
        
        responseObserver.onNext(AssignVariantResponse.newBuilder()
                .setVariant(variant)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getTestResults(GetTestResultsRequest request, StreamObserver<GetTestResultsResponse> responseObserver) {
        // In a real scenario, this would query a database to count conversions per variant
        responseObserver.onNext(GetTestResultsResponse.newBuilder()
                .setVariantACount(100) // Dummy data
                .setVariantBCount(150)
                .build());
        responseObserver.onCompleted();
    }
}
