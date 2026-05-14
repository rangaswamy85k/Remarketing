package com.remarketing.campaign.service;

import com.remarketing.campaign.entity.Campaign;
import com.remarketing.campaign.entity.CampaignState;
import com.remarketing.campaign.entity.Notification;
import com.remarketing.campaign.grpc.*;
import com.remarketing.campaign.repository.CampaignRepository;
import com.remarketing.campaign.repository.NotificationRepository;
import com.remarketing.product.entity.ProductEntity;
import com.remarketing.product.entity.UserSearch;
import com.remarketing.product.repository.ProductRepository;
import com.remarketing.product.repository.UserSearchRepository;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplTest {

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private com.remarketing.auth.repository.UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserSearchRepository userSearchRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StreamObserver<CampaignResponse> campaignObserver;

    @Mock
    private StreamObserver<ClickNotificationResponse> clickObserver;

    @InjectMocks
    private CampaignServiceImpl service;

    @Test
    void createCampaign_Success() {
        // Arrange
        CreateCampaignRequest request = CreateCampaignRequest.newBuilder()
                .setName("Test Campaign")
                .setBudget(100.0)
                .setSegmentId("Books")
                .build();

        // Act
        service.createCampaign(request, campaignObserver);

        // Assert
        verify(campaignRepository).save(any(Campaign.class));
        ArgumentCaptor<CampaignResponse> responseCaptor = ArgumentCaptor.forClass(CampaignResponse.class);
        verify(campaignObserver).onNext(responseCaptor.capture());
        verify(campaignObserver).onCompleted();

        CampaignResponse response = responseCaptor.getValue();
        assertEquals("ACTIVE", response.getStatus());
        assertTrue(response.getMessage().contains("Books"));
    }

    @Test
    void clickNotification_Success() {
        // Arrange
        ClickNotificationRequest request = ClickNotificationRequest.newBuilder()
                .setNotificationId("notif-1")
                .build();

        Notification notification = new Notification();
        notification.setId("notif-1");
        notification.setStatus("SENT");
        notification.setCampaignId("camp-1");
        notification.setProductName("Book A");
        notification.setProductPrice(20.0);

        Campaign campaign = new Campaign();
        campaign.setId("camp-1");
        campaign.setUsersConverted(0);
        campaign.setRevenueGenerated(0.0);

        when(notificationRepository.findById("notif-1")).thenReturn(Optional.of(notification));
        when(campaignRepository.findById("camp-1")).thenReturn(Optional.of(campaign));

        // Act
        service.clickNotification(request, clickObserver);

        // Assert
        verify(notificationRepository).save(any(Notification.class));
        verify(campaignRepository).save(any(Campaign.class));

        ArgumentCaptor<ClickNotificationResponse> responseCaptor = ArgumentCaptor.forClass(ClickNotificationResponse.class);
        verify(clickObserver).onNext(responseCaptor.capture());
        verify(clickObserver).onCompleted();

        ClickNotificationResponse response = responseCaptor.getValue();
        assertTrue(response.getSuccess());
        assertEquals("CLICKED", notification.getStatus());
        assertEquals(1, campaign.getUsersConverted());
        assertEquals(20.0, campaign.getRevenueGenerated());
    }
}
