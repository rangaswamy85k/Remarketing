package com.remarketing.analytics.service;

import com.remarketing.analytics.grpc.*;
import com.remarketing.campaign.entity.Campaign;
import com.remarketing.campaign.entity.Notification;
import com.remarketing.campaign.repository.CampaignRepository;
import com.remarketing.campaign.repository.NotificationRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@GrpcService
public class AnalyticsServiceImpl extends AnalyticsServiceGrpc.AnalyticsServiceImplBase {

    private final CampaignRepository campaignRepository;
    private final NotificationRepository notificationRepository;

    @Autowired
    public AnalyticsServiceImpl(CampaignRepository campaignRepository, NotificationRepository notificationRepository) {
        this.campaignRepository = campaignRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void getROIAnalytics(ROIRequest request, StreamObserver<ROIResponse> responseObserver) {
        Optional<Campaign> opt = campaignRepository.findById(request.getCampaignId());
        if (opt.isPresent()) {
            Campaign c = opt.get();
            
            List<Notification> notifications = notificationRepository.findByCampaignId(c.getId());
            long totalSent = notifications.size();
            
            // Get clicked notifications and sum their actual product prices
            List<Notification> clickedNotifications = notifications.stream()
                    .filter(n -> "CLICKED".equals(n.getStatus()))
                    .toList();
            long totalClicked = clickedNotifications.size();
            
            // Revenue = sum of actual product prices from clicked notifications
            double revenue = clickedNotifications.stream()
                    .mapToDouble(Notification::getProductPrice)
                    .sum();
            
            double campaignCost = c.getBudget();
            
            // Conversion Rate = (conversions / total_users) * 100
            double conversionRate = 0.0;
            if (totalSent > 0) {
                conversionRate = ((double) totalClicked / totalSent) * 100;
            }
            
            // ROI = (Revenue - Campaign Cost) / Campaign Cost * 100
            double roi = 0.0;
            if (campaignCost > 0) {
                roi = ((revenue - campaignCost) / campaignCost) * 100;
            }
            
            System.out.println("=== ROI Analytics for Campaign: " + c.getName() + " (Category: " + c.getCategory() + ") ===");
            System.out.println("Total Notifications Sent: " + totalSent);
            System.out.println("Total Users Converted (Clicked): " + totalClicked);
            System.out.println("Conversion Rate: " + String.format("%.2f", conversionRate) + "%");
            System.out.println("Revenue Generated: $" + String.format("%.2f", revenue));
            System.out.println("Campaign Cost: $" + campaignCost);
            System.out.println("ROI: " + String.format("%.2f", roi) + "%");
            
            responseObserver.onNext(ROIResponse.newBuilder()
                    .setCampaignId(c.getId())
                    .setRoiPercentage(roi)
                    .setMessage("Category: " + c.getCategory()
                        + ", Sent: " + totalSent 
                        + ", Converted: " + totalClicked 
                        + ", Conversion Rate: " + String.format("%.2f", conversionRate) + "%" 
                        + ", Revenue: $" + String.format("%.2f", revenue)
                        + ", Campaign Cost: $" + campaignCost 
                        + ", ROI: " + String.format("%.2f", roi) + "%")
                    .build());
        } else {
            responseObserver.onNext(ROIResponse.newBuilder()
                    .setCampaignId(request.getCampaignId())
                    .setRoiPercentage(0.0)
                    .setMessage("Campaign not found")
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getConversionReport(ConversionReportRequest request, StreamObserver<ConversionReportResponse> responseObserver) {
        Optional<Campaign> opt = campaignRepository.findById(request.getCampaignId());
        if (opt.isPresent()) {
            Campaign c = opt.get();
            
            List<Notification> notifications = notificationRepository.findByCampaignId(c.getId());
            long totalSent = notifications.size();
            
            List<Notification> clickedNotifications = notifications.stream()
                    .filter(n -> "CLICKED".equals(n.getStatus()))
                    .toList();
            long totalClicked = clickedNotifications.size();
            
            // Revenue = sum of actual product prices
            double revenue = clickedNotifications.stream()
                    .mapToDouble(Notification::getProductPrice)
                    .sum();
            
            double conversionRate = 0.0;
            if (totalSent > 0) {
                conversionRate = ((double) totalClicked / totalSent) * 100;
            }
            
            System.out.println("=== Conversion Report for Campaign: " + c.getName() + " (Category: " + c.getCategory() + ") ===");
            System.out.println("Total Notifications Sent: " + totalSent);
            System.out.println("Total Users Converted: " + totalClicked);
            System.out.println("Conversion Rate: " + String.format("%.2f", conversionRate) + "%");
            System.out.println("Revenue: $" + String.format("%.2f", revenue));
            
            responseObserver.onNext(ConversionReportResponse.newBuilder()
                    .setCampaignId(c.getId())
                    .setTotalConvertedUsers((int) totalClicked)
                    .setTotalRevenue(revenue)
                    .build());
        } else {
            responseObserver.onNext(ConversionReportResponse.newBuilder().build());
        }
        responseObserver.onCompleted();
    }
}
