package com.remarketing.execution.service;

import com.remarketing.execution.grpc.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class ExecutionServiceImpl extends ExecutionServiceGrpc.ExecutionServiceImplBase {

    @Override
    public void executeCampaign(ExecuteCampaignRequest request, StreamObserver<ExecuteCampaignResponse> responseObserver) {
        String type = request.getNotificationType();
        
        System.out.println("=== EXECUTION MODULE ===");
        if ("EMAIL".equalsIgnoreCase(type)) {
            System.out.println("[SENDING EMAIL] To User: " + request.getUserId());
            System.out.println("Subject: Important update for Campaign " + request.getCampaignId());
            System.out.println("Message: " + request.getMessage());
        } else if ("PUSH".equalsIgnoreCase(type)) {
            System.out.println("[SENDING PUSH] To User: " + request.getUserId());
            System.out.println("Alert: " + request.getMessage());
        } else {
            System.out.println("[UNKNOWN TYPE] " + type + " to User: " + request.getUserId());
        }
        System.out.println("========================");
        
        // Future DB integration: Log delivery status in MySQL table `delivery_logs`

        responseObserver.onNext(ExecuteCampaignResponse.newBuilder()
                .setSuccess(true)
                .setStatusMessage("Notification executed successfully")
                .build());
        responseObserver.onCompleted();
    }
}
