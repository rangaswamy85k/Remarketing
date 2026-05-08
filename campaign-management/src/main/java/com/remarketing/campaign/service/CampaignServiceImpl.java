package com.remarketing.campaign.service;

import com.remarketing.campaign.entity.Campaign;
import com.remarketing.campaign.entity.CampaignState;
import com.remarketing.campaign.entity.Notification;
import com.remarketing.campaign.repository.CampaignRepository;
import com.remarketing.campaign.repository.NotificationRepository;
import com.remarketing.campaign.grpc.*;
import com.remarketing.campaign.grpc.CampaignServiceGrpc;
import com.remarketing.product.entity.ProductEntity;
import com.remarketing.product.entity.UserSearch;
import com.remarketing.product.repository.ProductRepository;
import com.remarketing.product.repository.UserSearchRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@GrpcService
public class CampaignServiceImpl extends CampaignServiceGrpc.CampaignServiceImplBase {

    private final CampaignRepository campaignRepository;
    private final com.remarketing.auth.repository.UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final UserSearchRepository userSearchRepository;
    private final ProductRepository productRepository;
    private final Random random = new Random();

    @Autowired
    public CampaignServiceImpl(CampaignRepository campaignRepository,
                               com.remarketing.auth.repository.UserRepository userRepository,
                               NotificationRepository notificationRepository,
                               UserSearchRepository userSearchRepository,
                               ProductRepository productRepository) {
        this.campaignRepository = campaignRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.userSearchRepository = userSearchRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void createCampaign(CreateCampaignRequest request, StreamObserver<CampaignResponse> responseObserver) {
        Campaign campaign = new Campaign();
        campaign.setName(request.getName());
        campaign.setBudget(request.getBudget());
        // segment_id field in proto is used as category
        campaign.setCategory(request.getSegmentId());
        campaign.updateState(CampaignState.ACTIVE);

        System.out.println(campaign.handleCampaignState());
        System.out.println("Campaign created for category: " + campaign.getCategory());
        
        campaignRepository.save(campaign);

        responseObserver.onNext(CampaignResponse.newBuilder()
                .setCampaignId(campaign.getId())
                .setStatus(campaign.getStatusStr())
                .setMessage("Campaign created successfully for category: " + campaign.getCategory())
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
                
                String campaignCategory = c.getCategory();
                
                // Find all products in this campaign's category
                List<ProductEntity> categoryProducts = productRepository.findByCategoryIgnoreCase(campaignCategory);
                if (categoryProducts.isEmpty()) {
                    responseObserver.onNext(SendNotificationResponse.newBuilder()
                            .setSuccess(false)
                            .setMessage("No products found in category: " + campaignCategory)
                            .setNotificationsSent(0)
                            .build());
                    responseObserver.onCompleted();
                    return;
                }

                // Find users who searched for this campaign's category
                List<UserSearch> usersWhoSearched = userSearchRepository.findByCategoryIgnoreCase(campaignCategory);
                if (usersWhoSearched.isEmpty()) {
                    responseObserver.onNext(SendNotificationResponse.newBuilder()
                            .setSuccess(false)
                            .setMessage("No users have searched for category: " + campaignCategory)
                            .setNotificationsSent(0)
                            .build());
                    responseObserver.onCompleted();
                    return;
                }

                int numToSend = 0;
                java.util.List<String> notificationIds = new java.util.ArrayList<>();
                
                for (UserSearch userSearch : usersWhoSearched) {
                    String userId = userSearch.getUserId();
                    
                    // Check if this user already received a notification for this campaign
                    if (notificationRepository.findFirstByCampaignIdAndUserId(c.getId(), userId).isPresent()) {
                        continue;
                    }
                    
                    // Get user details
                    Optional<com.remarketing.auth.entity.User> userOpt = userRepository.findById(userId);
                    if (!userOpt.isPresent()) continue;
                    com.remarketing.auth.entity.User user = userOpt.get();
                    
                    // Only send to USERs
                    if (user.getRole() == null || !user.getRole().equalsIgnoreCase("USER")) continue;

                    // Pick a RANDOM product from this category for each user
                    ProductEntity randomProduct = categoryProducts.get(random.nextInt(categoryProducts.size()));

                    numToSend++;
                    
                    Notification notification = new Notification();
                    notification.setCampaignId(c.getId());
                    notification.setUserId(userId);
                    notification.setType(request.getNotificationType());
                    notification.setStatus("SENT");
                    notification.setProductId(randomProduct.getId());
                    notification.setProductName(randomProduct.getName());
                    notification.setProductPrice(randomProduct.getPrice());
                    notification.setMessage("Campaign: " + c.getName() + " | Product: " + randomProduct.getName() + " @ $" + randomProduct.getPrice());
                    
                    if ("EMAIL".equalsIgnoreCase(request.getNotificationType())) {
                        System.out.println("EMAIL sent to USER : " + user.getUsername() + " & EMAIL: " + user.getEmail() 
                            + " | Recommended Product: " + randomProduct.getName() + " ($" + randomProduct.getPrice() + ")"
                            + " [Notification ID: " + notification.getId() + "]");
                    } else {
                        System.out.println("PUSH NOTIFICATION sent to USER : " + user.getUsername() 
                            + " | Recommended Product: " + randomProduct.getName() + " ($" + randomProduct.getPrice() + ")"
                            + " [Notification ID: " + notification.getId() + "]");
                    }
                    
                    notificationRepository.save(notification);
                    notificationIds.add(notification.getId());
                }

                if (numToSend == 0) {
                    responseObserver.onNext(SendNotificationResponse.newBuilder()
                            .setSuccess(false)
                            .setMessage("All users who searched for '" + campaignCategory + "' have already been notified.")
                            .setNotificationsSent(0)
                            .build());
                    responseObserver.onCompleted();
                    return;
                }
                
                c.setNotificationsSent(c.getNotificationsSent() + numToSend);
                campaignRepository.save(c);
                
                responseObserver.onNext(SendNotificationResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Sent " + request.getNotificationType() + " notifications to " + numToSend 
                            + " users who searched for '" + campaignCategory + "'. Notification IDs: " + notificationIds)
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
                    // Use the actual product price from the notification
                    c.setRevenueGenerated(c.getRevenueGenerated() + notification.getProductPrice());
                    campaignRepository.save(c);
                }
                
                responseObserver.onNext(ClickNotificationResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Notification clicked! Product: " + notification.getProductName() 
                            + " ($" + notification.getProductPrice() + ") — Conversion tracked.")
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
