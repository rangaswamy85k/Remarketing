package com.remarketing.behavior.service;

import com.remarketing.behavior.entity.UserActivity;
import com.remarketing.behavior.grpc.*;
import com.remarketing.behavior.repository.UserActivityRepository;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BehaviorServiceImplTest {

    @Mock
    private UserActivityRepository userActivityRepository;

    @Mock
    private StreamObserver<LogActivityResponse> logActivityObserver;

    @Mock
    private StreamObserver<GetUserHistoryResponse> historyObserver;

    @InjectMocks
    private BehaviorServiceImpl service;

    @Test
    void logActivity_Success() {
        // Arrange
        LogActivityRequest request = LogActivityRequest.newBuilder()
                .setUserId("user-1")
                .setActivityType("SEARCH")
                .setDetails("Searched for laptops")
                .build();

        // Act
        service.logActivity(request, logActivityObserver);

        // Assert
        verify(userActivityRepository).save(any(UserActivity.class));
        ArgumentCaptor<LogActivityResponse> responseCaptor = ArgumentCaptor.forClass(LogActivityResponse.class);
        verify(logActivityObserver).onNext(responseCaptor.capture());
        verify(logActivityObserver).onCompleted();

        assertTrue(responseCaptor.getValue().getSuccess());
    }

    @Test
    void getUserHistory_Success() {
        // Arrange
        GetUserHistoryRequest request = GetUserHistoryRequest.newBuilder()
                .setUserId("user-1")
                .build();

        UserActivity activity = new UserActivity();
        activity.setUserId("user-1");
        activity.setActivityType("SEARCH");
        activity.setDetails("Books");
        activity.setTimestamp(LocalDateTime.now());

        when(userActivityRepository.findByUserIdOrderByTimestampDesc("user-1"))
                .thenReturn(Collections.singletonList(activity));

        // Act
        service.getUserHistory(request, historyObserver);

        // Assert
        ArgumentCaptor<GetUserHistoryResponse> responseCaptor = ArgumentCaptor.forClass(GetUserHistoryResponse.class);
        verify(historyObserver).onNext(responseCaptor.capture());
        verify(historyObserver).onCompleted();

        GetUserHistoryResponse response = responseCaptor.getValue();
        assertEquals(1, response.getActivitiesCount());
        assertEquals("SEARCH", response.getActivities(0).getActivityType());
        assertEquals("Books", response.getActivities(0).getDetails());
    }
}
