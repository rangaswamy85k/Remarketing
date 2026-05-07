package com.remarketing.cart.service;

import com.remarketing.cart.entity.CartItem;
import com.remarketing.cart.repository.CartRepository;
import com.remarketing.cart.grpc.*;
import com.remarketing.cart.grpc.CartServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.context.ApplicationEventPublisher;
import java.util.Optional;
import java.util.UUID;

@GrpcService
public class CartServiceImpl extends CartServiceGrpc.CartServiceImplBase {

    private final ApplicationEventPublisher eventPublisher;
    private final CartRepository cartRepository;

    public CartServiceImpl(ApplicationEventPublisher eventPublisher, CartRepository cartRepository) {
        this.eventPublisher = eventPublisher;
        this.cartRepository = cartRepository;
    }

    @Override
    public void addToCart(AddToCartRequest request, StreamObserver<AddToCartResponse> responseObserver) {
        System.out.println("Added product " + request.getProductId() + " to cart for user " + request.getUserId());
        
        Optional<CartItem> existingItem = cartRepository.findByUserIdAndProductId(request.getUserId(), request.getProductId());
        
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartRepository.save(item);
        } else {
            CartItem newItem = new CartItem(request.getUserId(), request.getProductId(), request.getQuantity());
            cartRepository.save(newItem);
        }

        responseObserver.onNext(AddToCartResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Added to cart successfully")
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void checkout(CheckoutRequest request, StreamObserver<CheckoutResponse> responseObserver) {
        System.out.println("Checkout for user " + request.getUserId());
        
        // Future DB integration: Move items from Cart table to Order table, and DELETE from Cart table

        responseObserver.onNext(CheckoutResponse.newBuilder()
                .setSuccess(true)
                .setOrderId(UUID.randomUUID().toString())
                .setMessage("Checkout successful")
                .build());
        responseObserver.onCompleted();
    }
}
