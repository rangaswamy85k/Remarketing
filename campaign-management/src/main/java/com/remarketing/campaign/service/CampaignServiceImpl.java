package com.remarketing.campaign.service;

import com.remarketing.campaign.entity.Campaign;
import com.remarketing.campaign.entity.CampaignState;
import com.remarketing.campaign.entity.Notification;
import com.remarketing.campaign.repository.CampaignRepository;
import com.remarketing.campaign.repository.NotificationRepository;
import com.remarketing.campaign.grpc.*;
import com.remarketing.campaign.grpc.CampaignServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.Random;

@GrpcService
public class CampaignServiceImpl extends CampaignServiceGrpc.CampaignServiceImplBase {

    private final CampaignRepository campaignRepository;
    private final com.remarketing.auth.repository.UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final Random random = new Random();

    @Autowired
    public CampaignServiceImpl(CampaignRepository campaignRepository, com.remarketing.auth.repository.UserRepository userRepository, NotificationRepository notificationRepository) {
        this.campaignRepository = campaignRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void createCampaign(CreateCampaignRequest request, StreamObserver<CampaignResponse> responseObserver) {
        Campaign campaign = new Campaign();
        campaign.setName(request.getName());
        campaign.setBudget(request.getBudget());
        campaign.setSegmentId(request.getSegmentId());
        campaign.updateState(CampaignState.ACTIVE);

        System.out.println(campaign.handleCampaignState());
        
        campaignRepository.save(campaign);

        responseObserver.onNext(CampaignResponse.newBuilder()
                .setCampaignId(campaign.getId())
                .setStatus(campaign.getStatusStr())
                .setMessage("Campaign created successfully")
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void terminateCampaign(TerminateCampaignRequest request, StreamObserver<CampaignResponse> responseObserver) {
        Optional<Campaign> opt = campaignRepository.findById(request.getCampaignId());
        if (opt.isPresent()) {
            Campaign c = opt.get();
            c.updateState(CampaignState.TERMINATED);
            campaignRepository.save(c);
            
            responseObserver.onNext(CampaignResponse.newBuilder()
                    .setCampaignId(c.getId())
                    .setStatus(c.getStatusStr())
                    .setMessage("Campaign terminated")
                    .build());
        } else {
            responseObserver.onNext(CampaignResponse.newBuilder()
                    .setCampaignId(request.getCampaignId())
                    .setStatus("NOT_FOUND")
                    .setMessage("Campaign not found")
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void sendNotification(SendNotificationRequest request, StreamObserver<SendNotificationResponse> responseObserver) {
        Optional<Campaign> opt = campaignRepository.findById(request.getCampaignId());
        if (opt.isPresent()) {
            Campaign c = opt.get();
            if (c.getState() == CampaignState.ACTIVE || "ACTIVE".equals(c.getStatusStr())) {
                
                java.util.List<com.remarketing.auth.entity.User> allUsers = userRepository.findAll();
                
                if (allUsers.isEmpty()) {
                    responseObserver.onNext(SendNotificationResponse.newBuilder()
                            .setSuccess(false)
                            .setMessage("No users found in database to send notifications.")
                            .setNotificationsSent(0)
                            .build());
                    responseObserver.onCompleted();
                    return;
                }

                int numToSend = 0;
                for (com.remarketing.auth.entity.User user : allUsers) {
                    if (user.getRole() != null && user.getRole().equalsIgnoreCase("USER")) {
                        
                        // Check if this user already received a notification for this specific campaign
                        if (notificationRepository.findFirstByCampaignIdAndUserId(c.getId(), user.getId()).isPresent()) {
                            continue; // Skip if already notified
                        }

                        numToSend++;
                        
                        Notification notification = new Notification();
                        notification.setCampaignId(c.getId());
                        notification.setUserId(user.getId());
                        notification.setType(request.getNotificationType());
                        notification.setStatus("SENT");
                        
                        if ("EMAIL".equalsIgnoreCase(request.getNotificationType())) {
                            notification.setMessage("Campaign Offer: " + c.getName());
                            System.out.println("EMAIL sent to USER : " + user.getUsername() + " & EMAIL: " + user.getEmail() + "  Campaign Offer: " + c.getName() + " [Notification ID: " + notification.getId() + "]");
                        } else {
                            notification.setMessage("Campaign Offer: " + c.getName());
                            System.out.println("PUSH NOTIFICATION sent to USER : " + user.getUsername() + " Campaign Offer: " + c.getName() + " [Notification ID: " + notification.getId() + "]");
                        }
                        
                        notificationRepository.save(notification);
                    }
                }

                if (numToSend == 0) {
                    responseObserver.onNext(SendNotificationResponse.newBuilder()
                            .setSuccess(false)
                            .setMessage("No new users found in database to send notifications (they may have already been notified).")
                            .setNotificationsSent(0)
                            .build());
                    responseObserver.onCompleted();
                    return;
                }
                
                c.setNotificationsSent(c.getNotificationsSent() + numToSend);
                campaignRepository.save(c);
                
                responseObserver.onNext(SendNotificationResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Sent " + request.getNotificationType() + " notifications to " + numToSend + " DB users.")
                        .setNotificationsSent(numToSend)
                        .build());
            } else {
                responseObserver.onNext(SendNotificationResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Campaign is not ACTIVE")
                        .setNotificationsSent(0)
                        .build());
            }
        } else {
            responseObserver.onNext(SendNotificationResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Campaign not found")
                        .setNotificationsSent(0)
                        .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void clickNotification(ClickNotificationRequest request, StreamObserver<ClickNotificationResponse> responseObserver) {
        Optional<Notification> notifOpt = notificationRepository.findById(request.getNotificationId());
        if (notifOpt.isPresent()) {
            Notification notification = notifOpt.get();
            if (!"CLICKED".equals(notification.getStatus())) {
                notification.setStatus("CLICKED");
                notificationRepository.save(notification);
                
                Optional<Campaign> cOpt = campaignRepository.findById(notification.getCampaignId());
                if (cOpt.isPresent()) {
                    Campaign c = cOpt.get();
                    c.setUsersConverted(c.getUsersConverted() + 1);
                    // Add real conversion value (e.g. standard product value or cart value)
                    c.setRevenueGenerated(c.getRevenueGenerated() + 49.99); 
                    campaignRepository.save(c);
                }
                
                responseObserver.onNext(ClickNotificationResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Notification marked as read/clicked and conversion tracked.")
                        .build());
            } else {
                responseObserver.onNext(ClickNotificationResponse.newBuilder()
                        .setSuccess(false)
                        .setMessage("Notification was already clicked.")
                        .build());
            }
        } else {
            responseObserver.onNext(ClickNotificationResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Notification not found")
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getCampaignConversions(GetConversionsRequest request, StreamObserver<ConversionsResponse> responseObserver) {
        Optional<Campaign> opt = campaignRepository.findById(request.getCampaignId());
        if (opt.isPresent()) {
            Campaign c = opt.get();
            responseObserver.onNext(ConversionsResponse.newBuilder()
                    .setCampaignId(c.getId())
                    .setCampaignName(c.getName())
                    .setNotificationsSent(c.getNotificationsSent())
                    .setUsersConverted(c.getUsersConverted())
                    .setRevenueGenerated(c.getRevenueGenerated())
                    .setCampaignBudget(c.getBudget())
                    .build());
        } else {
            responseObserver.onNext(ConversionsResponse.newBuilder().build());
        }
        responseObserver.onCompleted();
    }
}
