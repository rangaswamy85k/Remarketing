package com.remarketing.abtesting.service;

import com.remarketing.abtesting.entity.ABTestAssignment;
import com.remarketing.abtesting.grpc.*;
import com.remarketing.abtesting.repository.ABTestAssignmentRepository;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ABTestServiceImplTest {

    @Mock
    private ABTestAssignmentRepository repository;

    @Mock
    private StreamObserver<AssignVariantResponse> assignVariantObserver;

    @Mock
    private StreamObserver<GetTestResultsResponse> getTestResultsObserver;

    @InjectMocks
    private ABTestServiceImpl service;

    @Test
    void assignVariant_NewAssignment_Success() {
        // Arrange
        AssignVariantRequest request = AssignVariantRequest.newBuilder()
                .setUserId("user-1")
                .setCampaignId("camp-1")
                .build();

        when(repository.findByUserIdAndCampaignId("user-1", "camp-1")).thenReturn(Optional.empty());
        when(repository.save(any(ABTestAssignment.class))).thenAnswer(invocation -> {
            ABTestAssignment a = invocation.getArgument(0);
            a.setId("generated-id");
            return a;
        });

        // Act
        service.assignVariant(request, assignVariantObserver);

        // Assert
        ArgumentCaptor<AssignVariantResponse> responseCaptor = ArgumentCaptor.forClass(AssignVariantResponse.class);
        verify(assignVariantObserver).onNext(responseCaptor.capture());
        verify(assignVariantObserver).onCompleted();
        verify(repository).save(any(ABTestAssignment.class));

        AssignVariantResponse response = responseCaptor.getValue();
        assertEquals("user-1", response.getUserId());
        assertEquals("camp-1", response.getCampaignId());
        assertNotNull(response.getVariant());
    }

    @Test
    void assignVariant_ExistingAssignment_ReturnsExisting() {
        // Arrange
        AssignVariantRequest request = AssignVariantRequest.newBuilder()
                .setUserId("user-1")
                .setCampaignId("camp-1")
                .build();

        ABTestAssignment existing = new ABTestAssignment();
        existing.setId("old-id");
        existing.setUserId("user-1");
        existing.setCampaignId("camp-1");
        existing.setVariant("B");
        existing.setAssignedAt(LocalDateTime.now());

        when(repository.findByUserIdAndCampaignId("user-1", "camp-1")).thenReturn(Optional.of(existing));

        // Act
        service.assignVariant(request, assignVariantObserver);

        // Assert
        ArgumentCaptor<AssignVariantResponse> responseCaptor = ArgumentCaptor.forClass(AssignVariantResponse.class);
        verify(assignVariantObserver).onNext(responseCaptor.capture());
        verify(assignVariantObserver).onCompleted();
        verify(repository, never()).save(any(ABTestAssignment.class));

        AssignVariantResponse response = responseCaptor.getValue();
        assertEquals("B", response.getVariant());
    }

    @Test
    void getTestResults_Success() {
        // Arrange
        GetTestResultsRequest request = GetTestResultsRequest.newBuilder()
                .setCampaignId("camp-1")
                .build();

        when(repository.countByCampaignId("camp-1")).thenReturn(100);
        when(repository.countByCampaignIdAndVariant("camp-1", "A")).thenReturn(45);
        when(repository.countByCampaignIdAndVariant("camp-1", "B")).thenReturn(55);

        // Act
        service.getTestResults(request, getTestResultsObserver);

        // Assert
        ArgumentCaptor<GetTestResultsResponse> responseCaptor = ArgumentCaptor.forClass(GetTestResultsResponse.class);
        verify(getTestResultsObserver).onNext(responseCaptor.capture());
        verify(getTestResultsObserver).onCompleted();

        GetTestResultsResponse response = responseCaptor.getValue();
        assertEquals(100, response.getTotalParticipants());
        assertEquals(45, response.getVariantACount());
        assertEquals(55, response.getVariantBCount());
    }
}
