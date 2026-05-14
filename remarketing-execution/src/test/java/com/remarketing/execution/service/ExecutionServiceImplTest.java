package com.remarketing.execution.service;

import com.remarketing.execution.grpc.ExecuteCampaignRequest;
import com.remarketing.execution.grpc.ExecuteCampaignResponse;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExecutionServiceImplTest {

    @Mock
    private StreamObserver<ExecuteCampaignResponse> responseObserver;

    @InjectMocks
    private ExecutionServiceImpl service;

    @Test
    void executeCampaign_EmailType_Success() {
        // Arrange
        ExecuteCampaignRequest request = ExecuteCampaignRequest.newBuilder()
                .setCampaignId("camp-1")
                .setUserId("user-1")
                .setNotificationType("EMAIL")
                .setMessage("Special offer")
                .build();

        // Act
        service.executeCampaign(request, responseObserver);

        // Assert
        ArgumentCaptor<ExecuteCampaignResponse> responseCaptor = ArgumentCaptor.forClass(ExecuteCampaignResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        ExecuteCampaignResponse response = responseCaptor.getValue();
        assertTrue(response.getSuccess());
    }

    @Test
    void executeCampaign_PushType_Success() {
        // Arrange
        ExecuteCampaignRequest request = ExecuteCampaignRequest.newBuilder()
                .setCampaignId("camp-2")
                .setUserId("user-2")
                .setNotificationType("PUSH")
                .setMessage("Alert")
                .build();

        // Act
        service.executeCampaign(request, responseObserver);

        // Assert
        ArgumentCaptor<ExecuteCampaignResponse> responseCaptor = ArgumentCaptor.forClass(ExecuteCampaignResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        ExecuteCampaignResponse response = responseCaptor.getValue();
        assertTrue(response.getSuccess());
    }
}
