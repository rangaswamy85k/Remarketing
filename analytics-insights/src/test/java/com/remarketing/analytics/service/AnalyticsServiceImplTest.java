package com.remarketing.analytics.service;

import com.remarketing.analytics.grpc.*;
import com.remarketing.campaign.entity.Campaign;
import com.remarketing.campaign.entity.Notification;
import com.remarketing.campaign.repository.CampaignRepository;
import com.remarketing.campaign.repository.NotificationRepository;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private StreamObserver<ROIResponse> roiObserver;

    @Mock
    private StreamObserver<ConversionReportResponse> conversionObserver;

    @InjectMocks
    private AnalyticsServiceImpl service;

    @Test
    void getROIAnalytics_Success() {
        // Arrange
        ROIRequest request = ROIRequest.newBuilder()
                .setCampaignId("camp-1")
                .build();

        Campaign campaign = new Campaign();
        campaign.setId("camp-1");
        campaign.setName("Test Camp");
        campaign.setBudget(100.0);

        Notification n1 = new Notification();
        n1.setStatus("CLICKED");
        n1.setProductPrice(50.0);

        Notification n2 = new Notification();
        n2.setStatus("SENT");
        n2.setProductPrice(150.0);

        when(campaignRepository.findById("camp-1")).thenReturn(Optional.of(campaign));
        when(notificationRepository.findByCampaignId("camp-1")).thenReturn(Arrays.asList(n1, n2));

        // Act
        service.getROIAnalytics(request, roiObserver);

        // Assert
        ArgumentCaptor<ROIResponse> responseCaptor = ArgumentCaptor.forClass(ROIResponse.class);
        verify(roiObserver).onNext(responseCaptor.capture());
        verify(roiObserver).onCompleted();

        ROIResponse response = responseCaptor.getValue();
        assertEquals("camp-1", response.getCampaignId());
        // Revenue = 50. Cost = 100. ROI = (50 - 100) / 100 * 100 = -50%
        assertEquals(-50.0, response.getRoiPercentage());
        assertTrue(response.getMessage().contains("Revenue: $50.00"));
    }

    @Test
    void getConversionReport_Success() {
        // Arrange
        ConversionReportRequest request = ConversionReportRequest.newBuilder()
                .setCampaignId("camp-1")
                .build();

        Campaign campaign = new Campaign();
        campaign.setId("camp-1");

        Notification n1 = new Notification();
        n1.setStatus("CLICKED");
        n1.setProductPrice(75.0);

        when(campaignRepository.findById("camp-1")).thenReturn(Optional.of(campaign));
        when(notificationRepository.findByCampaignId("camp-1")).thenReturn(Collections.singletonList(n1));

        // Act
        service.getConversionReport(request, conversionObserver);

        // Assert
        ArgumentCaptor<ConversionReportResponse> responseCaptor = ArgumentCaptor.forClass(ConversionReportResponse.class);
        verify(conversionObserver).onNext(responseCaptor.capture());
        verify(conversionObserver).onCompleted();

        ConversionReportResponse response = responseCaptor.getValue();
        assertEquals("camp-1", response.getCampaignId());
        assertEquals(1, response.getTotalConvertedUsers());
        assertEquals(75.0, response.getTotalRevenue());
    }
}
