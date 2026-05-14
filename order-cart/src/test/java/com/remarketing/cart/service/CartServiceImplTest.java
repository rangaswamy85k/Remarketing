package com.remarketing.cart.service;

import com.remarketing.cart.entity.CartItem;
import com.remarketing.cart.entity.CheckoutOrder;
import com.remarketing.cart.grpc.*;
import com.remarketing.cart.repository.CartRepository;
import com.remarketing.cart.repository.CheckoutOrderRepository;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CheckoutOrderRepository checkoutOrderRepository;

    @Mock
    private StreamObserver<AddToCartResponse> addToCartObserver;

    @Mock
    private StreamObserver<CheckoutResponse> checkoutObserver;

    @InjectMocks
    private CartServiceImpl service;

    @Test
    void addToCart_NewItem_Success() {
        // Arrange
        AddToCartRequest request = AddToCartRequest.newBuilder()
                .setUserId("user-1")
                .setProductId("prod-1")
                .setQuantity(2)
                .build();

        when(cartRepository.findByUserIdAndProductId("user-1", "prod-1")).thenReturn(Optional.empty());

        // Act
        service.addToCart(request, addToCartObserver);

        // Assert
        verify(cartRepository).save(any(CartItem.class));
        ArgumentCaptor<AddToCartResponse> responseCaptor = ArgumentCaptor.forClass(AddToCartResponse.class);
        verify(addToCartObserver).onNext(responseCaptor.capture());
        verify(addToCartObserver).onCompleted();
        assertTrue(responseCaptor.getValue().getSuccess());
    }

    @Test
    void checkout_WithItems_Success() {
        // Arrange
        CheckoutRequest request = CheckoutRequest.newBuilder()
                .setUserId("user-1")
                .build();

        CartItem item = new CartItem("user-1", "prod-1", 2);
        List<CartItem> cartItems = new ArrayList<>();
        cartItems.add(item);

        when(cartRepository.findByUserId("user-1")).thenReturn(cartItems);
        when(checkoutOrderRepository.save(any(CheckoutOrder.class))).thenAnswer(invocation -> {
            CheckoutOrder order = invocation.getArgument(0);
            order.setId("order-123");
            return order;
        });

        // Act
        service.checkout(request, checkoutObserver);

        // Assert
        verify(checkoutOrderRepository).save(any(CheckoutOrder.class));
        verify(cartRepository).deleteAll(cartItems);

        ArgumentCaptor<CheckoutResponse> responseCaptor = ArgumentCaptor.forClass(CheckoutResponse.class);
        verify(checkoutObserver).onNext(responseCaptor.capture());
        verify(checkoutObserver).onCompleted();

        CheckoutResponse response = responseCaptor.getValue();
        assertTrue(response.getSuccess());
        assertNotNull(response.getOrderId());
    }

    @Test
    void checkout_EmptyCart_Fails() {
        // Arrange
        CheckoutRequest request = CheckoutRequest.newBuilder()
                .setUserId("user-1")
                .build();

        when(cartRepository.findByUserId("user-1")).thenReturn(Collections.emptyList());

        // Act
        service.checkout(request, checkoutObserver);

        // Assert
        verify(checkoutOrderRepository, never()).save(any(CheckoutOrder.class));
        verify(cartRepository, never()).deleteAll(anyList());

        ArgumentCaptor<CheckoutResponse> responseCaptor = ArgumentCaptor.forClass(CheckoutResponse.class);
        verify(checkoutObserver).onNext(responseCaptor.capture());
        verify(checkoutObserver).onCompleted();

        CheckoutResponse response = responseCaptor.getValue();
        assertFalse(response.getSuccess());
        assertEquals("Cart is empty", response.getMessage());
    }
}
