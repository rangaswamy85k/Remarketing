package com.remarketing.recommendation.service;

import com.remarketing.product.entity.ProductEntity;
import com.remarketing.product.entity.UserSearch;
import com.remarketing.product.repository.ProductRepository;
import com.remarketing.product.repository.UserSearchRepository;
import com.remarketing.recommendation.grpc.*;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserSearchRepository userSearchRepository;

    @Mock
    private StreamObserver<RecommendationResponse> responseObserver;

    @InjectMocks
    private RecommendationServiceImpl service;

    @Test
    void getRecommendations_WithHistory_ReturnsPersonalized() {
        // Arrange
        RecommendationRequest request = RecommendationRequest.newBuilder()
                .setUserId("user-1")
                .build();

        UserSearch search = new UserSearch();
        search.setUserId("user-1");
        search.setCategory("Books");

        ProductEntity product = new ProductEntity();
        product.setId("b1");
        product.setName("Clean Code");
        product.setCategory("Books");

        when(userSearchRepository.findByUserId("user-1")).thenReturn(Optional.of(search));
        when(productRepository.findByCategoryIgnoreCase("Books")).thenReturn(Collections.singletonList(product));

        // Act
        service.getRecommendations(request, responseObserver);

        // Assert
        ArgumentCaptor<RecommendationResponse> responseCaptor = ArgumentCaptor.forClass(RecommendationResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        RecommendationResponse response = responseCaptor.getValue();
        assertEquals(1, response.getRecommendedProductsCount());
        assertEquals("Clean Code", response.getRecommendedProducts(0).getName());
    }

    @Test
    void getRecommendations_NoHistory_ReturnsEmpty() {
        // Arrange
        RecommendationRequest request = RecommendationRequest.newBuilder()
                .setUserId("user-unknown")
                .build();

        when(userSearchRepository.findByUserId("user-unknown")).thenReturn(Optional.empty());

        // Act
        service.getRecommendations(request, responseObserver);

        // Assert
        ArgumentCaptor<RecommendationResponse> responseCaptor = ArgumentCaptor.forClass(RecommendationResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();

        RecommendationResponse response = responseCaptor.getValue();
        assertEquals(0, response.getRecommendedProductsCount());
    }
}
