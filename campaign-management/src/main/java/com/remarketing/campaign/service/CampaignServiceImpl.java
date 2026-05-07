package com.remarketing.campaign.service;

import com.remarketing.campaign.entity.Campaign;
import com.remarketing.campaign.grpc.*;
import com.remarketing.campaign.grpc.CampaignServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class CampaignServiceImpl extends CampaignServiceGrpc.CampaignServiceImplBase {

    @Override
    public void createCampaign(CreateCampaignRequest request, StreamObserver<CampaignResponse> responseObserver) {
        Campaign campaign = new Campaign();
        campaign.setName(request.getName());
        campaign.setBudget(request.getBudget());
        campaign.setSegmentId(request.getSegmentId());

        // Note: Java 21 Switch Pattern Matching demonstrated here:
        System.out.println(campaign.handleCampaignState());

        // Future DB integration: Save campaign to MySQL table

        responseObserver.onNext(CampaignResponse.newBuilder()
                .setCampaignId(campaign.getId())
                .setStatus(campaign.getStatusStr())
                .build());
        responseObserver.onCompleted();
    }
}
