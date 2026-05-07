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

    public double calculateROI(double revenue, double cost) {
        if (cost == 0) return 0.0;
        return ((revenue - cost) / cost) * 100;
    }

    @Override
    public void getROIAnalytics(ROIRequest request, StreamObserver<ROIResponse> responseObserver) {
        Optional<Campaign> opt = campaignRepository.findById(request.getCampaignId());
        if (opt.isPresent()) {
            Campaign c = opt.get();
            
            // Get real notification data from DB
            List<Notification> notifications = notificationRepository.findByCampaignId(c.getId());
            long totalSent = notifications.size();
            long totalClicked = notifications.stream().filter(n -> "CLICKED".equals(n.getStatus())).count();
            double revenue = totalClicked * 49.99; // Each conversion = $49.99
            double cost = c.getBudget();
            
            double roi = calculateROI(revenue, cost);
            
            System.out.println("=== ROI Analytics for Campaign: " + c.getName() + " ===");
            System.out.println("Total Notifications Sent: " + totalSent);
            System.out.println("Total Users Converted (Clicked): " + totalClicked);
            System.out.println("Revenue Generated: $" + revenue);
            System.out.println("Campaign Budget: $" + cost);
            System.out.println("ROI: " + roi + "%");
            
            responseObserver.onNext(ROIResponse.newBuilder()
                    .setCampaignId(c.getId())
                    .setRoiPercentage(roi)
                    .setMessage("ROI calculated from real notification data. Sent: " + totalSent + ", Converted: " + totalClicked + ", Revenue: $" + revenue)
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
            
            // Get real notification data from DB
            List<Notification> notifications = notificationRepository.findByCampaignId(c.getId());
            long totalSent = notifications.size();
            long totalClicked = notifications.stream().filter(n -> "CLICKED".equals(n.getStatus())).count();
            double revenue = totalClicked * 49.99;
            
            System.out.println("=== Conversion Report for Campaign: " + c.getName() + " ===");
            System.out.println("Total Notifications Sent: " + totalSent);
            System.out.println("Total Users Converted: " + totalClicked);
            System.out.println("Revenue: $" + revenue);
            
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
