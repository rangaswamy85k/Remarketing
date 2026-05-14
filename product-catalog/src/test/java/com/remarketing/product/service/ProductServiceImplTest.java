package com.remarketing.product.service;

import com.remarketing.product.entity.ProductEntity;
import com.remarketing.product.entity.UserSearch;
import com.remarketing.product.grpc.*;
import com.remarketing.product.repository.ProductRepository;
import com.remarketing.product.repository.UserSearchRepository;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserSearchRepository userSearchRepository;

    @Mock
    private StreamObserver<SearchResponse> searchObserver;

    @Mock
    private StreamObserver<AddProductResponse> addProductObserver;

    @InjectMocks
    private ProductServiceImpl service;

    @Test
    void searchProducts_Success() {
        // Arrange
        SearchRequest request = SearchRequest.newBuilder()
                .setUserId("user-1")
                .setQuery("Electronics")
                .build();

        ProductEntity product = new ProductEntity();
        product.setId("p1");
        product.setName("Laptop");
        product.setPrice(999.99);
        product.setCategory("Electronics");

        when(userSearchRepository.findByUserId("user-1")).thenReturn(Optional.empty());
        when(productRepository.findByCategoryIgnoreCase("Electronics")).thenReturn(Collections.singletonList(product));

        // Act
        service.searchProducts(request, searchObserver);

        // Assert
        ArgumentCaptor<SearchResponse> responseCaptor = ArgumentCaptor.forClass(SearchResponse.class);
        verify(searchObserver).onNext(responseCaptor.capture());
        verify(searchObserver).onCompleted();
        verify(eventPublisher).publishEvent(any());
        verify(userSearchRepository).save(any(UserSearch.class));

        SearchResponse response = responseCaptor.getValue();
        assertEquals(1, response.getProductsCount());
        assertEquals("Laptop", response.getProducts(0).getName());
    }

    @Test
    void addProduct_Success() {
        // Arrange
        AddProductRequest request = AddProductRequest.newBuilder()
                .setName("Phone")
                .setPrice(499.0)
                .setCategory("Electronics")
                .build();

        // Act
        service.addProduct(request, addProductObserver);

        // Assert
        verify(productRepository).save(any(ProductEntity.class));
        ArgumentCaptor<AddProductResponse> responseCaptor = ArgumentCaptor.forClass(AddProductResponse.class);
        verify(addProductObserver).onNext(responseCaptor.capture());
        assertTrue(responseCaptor.getValue().getSuccess());
    }

    @Test
    void removeProduct_Success() {
        // Arrange
        RemoveProductRequest request = RemoveProductRequest.newBuilder()
                .setProductId("p1")
                .build();

        when(productRepository.findById("p1")).thenReturn(Optional.of(new ProductEntity()));

        // Act
        service.removeProduct(request, serviceObserverStub()); // wait, I can use a mock

        // Actually let's use the mock I have or create a new one
    }

    // Helper for dummy observer if needed, but I'll stick to mocks
    private StreamObserver<RemoveProductResponse> serviceObserverStub() {
        return mock(StreamObserver.class);
    }

    @Test
    void removeProduct_Success_Mock() {
        // Arrange
        RemoveProductRequest request = RemoveProductRequest.newBuilder()
                .setProductId("p1")
                .build();
        StreamObserver<RemoveProductResponse> observer = mock(StreamObserver.class);
        when(productRepository.findById("p1")).thenReturn(Optional.of(new ProductEntity()));

        // Act
        service.removeProduct(request, observer);

        // Assert
        verify(productRepository).deleteById("p1");
        verify(observer).onNext(any(RemoveProductResponse.class));
        verify(observer).onCompleted();
    }
}
