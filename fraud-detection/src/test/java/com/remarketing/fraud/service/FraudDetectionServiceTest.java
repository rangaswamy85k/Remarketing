package com.remarketing.fraud.service;

import com.remarketing.fraud.model.FraudAlert;
import com.remarketing.fraud.repository.FraudAlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudDetectionServiceTest {

    @Mock
    private FraudAlertRepository repository;

    @InjectMocks
    private FraudDetectionService service;

    private FraudAlert sampleAlert;

    @BeforeEach
    void setUp() {
        sampleAlert = new FraudAlert();
        sampleAlert.setId("test-id");
        sampleAlert.setCampaignId("camp-1");
        sampleAlert.setAlertType("BOT");
        sampleAlert.setDescription("Test description");
    }

    @Test
    void createAlert_ShouldSaveAndReturnAlert() {
        // Arrange
        when(repository.save(any(FraudAlert.class))).thenReturn(sampleAlert);

        // Act
        FraudAlert result = service.createAlert("camp-1", "BOT", "Test description");

        // Assert
        assertNotNull(result);
        assertEquals("camp-1", result.getCampaignId());
        assertEquals("BOT", result.getAlertType());
        verify(repository, times(1)).save(any(FraudAlert.class));
    }

    @Test
    void listAlerts_ShouldReturnListOfAlerts() {
        // Arrange
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();
        List<FraudAlert> alerts = Arrays.asList(sampleAlert);
        when(repository.findByCampaignIdAndCreatedAtBetween(eq("camp-1"), eq(from), eq(to)))
                .thenReturn(alerts);

        // Act
        List<FraudAlert> result = service.listAlerts("camp-1", from, to);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("camp-1", result.get(0).getCampaignId());
        verify(repository, times(1)).findByCampaignIdAndCreatedAtBetween(eq("camp-1"), eq(from), eq(to));
    }
}
