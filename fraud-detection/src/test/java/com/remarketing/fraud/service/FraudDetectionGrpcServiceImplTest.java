package com.remarketing.fraud.service;

import com.remarketing.fraud.grpc.FraudDetectionGrpcServiceImpl;
import com.remarketing.fraud.model.FraudAlert;
import com.remarketing.fraud.proto.CreateAlertRequest;
import com.remarketing.fraud.proto.CreateAlertResponse;
import com.remarketing.fraud.proto.FraudProto.*;
import com.remarketing.fraud.proto.ListAlertsRequest;
import com.remarketing.fraud.proto.ListAlertsResponse;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudDetectionGrpcServiceImplTest {

    @Mock
    private FraudDetectionService fraudDetectionService;

    @Mock
    private StreamObserver<CreateAlertResponse> createAlertObserver;

    @Mock
    private StreamObserver<ListAlertsResponse> listAlertsObserver;

    @InjectMocks
    private FraudDetectionGrpcServiceImpl grpcService;

    @Test
    void createAlert_Success() {
        // Arrange
        CreateAlertRequest request = CreateAlertRequest.newBuilder()
                .setCampaignId("camp-1")
                .setAlertType("BOT")
                .setDescription("Test desc")
                .build();

        FraudAlert savedAlert = new FraudAlert();
        savedAlert.setId("alert-uuid");
        savedAlert.setCampaignId("camp-1");
        savedAlert.setAlertType("BOT");
        savedAlert.setDescription("Test desc");

        when(fraudDetectionService.createAlert(anyString(), anyString(), anyString())).thenReturn(savedAlert);

        // Act
        grpcService.createAlert(request, createAlertObserver);

        // Assert
        ArgumentCaptor<CreateAlertResponse> responseCaptor = ArgumentCaptor.forClass(CreateAlertResponse.class);
        verify(createAlertObserver).onNext(responseCaptor.capture());
        verify(createAlertObserver).onCompleted();

        CreateAlertResponse response = responseCaptor.getValue();
        assertEquals("alert-uuid", response.getAlertId());
        assertEquals("Alert recorded", response.getMessage());
    }

    @Test
    void listAlerts_Success() {
        // Arrange
        ListAlertsRequest request = ListAlertsRequest.newBuilder()
                .setCampaignId("camp-1")
                .setFromTs(0)
                .setToTs(0)
                .build();

        FraudAlert alert = new FraudAlert();
        alert.setId("alert-1");
        alert.setCampaignId("camp-1");
        alert.setAlertType("SPIKE");
        alert.setDescription("High traffic");
        alert.setCreatedAt(Instant.now());

        when(fraudDetectionService.listAlerts(anyString(), any(Instant.class), any(Instant.class)))
                .thenReturn(Collections.singletonList(alert));

        // Act
        grpcService.listAlerts(request, listAlertsObserver);

        // Assert
        ArgumentCaptor<ListAlertsResponse> responseCaptor = ArgumentCaptor.forClass(ListAlertsResponse.class);
        verify(listAlertsObserver).onNext(responseCaptor.capture());
        verify(listAlertsObserver).onCompleted();

        ListAlertsResponse response = responseCaptor.getValue();
        assertEquals(1, response.getAlertsCount());
        assertEquals("alert-1", response.getAlerts(0).getAlertId());
    }
}
