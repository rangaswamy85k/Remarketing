package com.remarketing.cart.service;

import com.remarketing.cart.entity.CartItem;
import com.remarketing.cart.entity.CheckoutOrder;
import com.remarketing.cart.entity.CheckoutItem;
import com.remarketing.cart.repository.CartRepository;
import com.remarketing.cart.repository.CheckoutOrderRepository;
import com.remarketing.cart.grpc.*;
import com.remarketing.cart.grpc.CartServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@GrpcService
public class CartServiceImpl extends CartServiceGrpc.CartServiceImplBase {

    private final ApplicationEventPublisher eventPublisher;
    private final CartRepository cartRepository;
    private final CheckoutOrderRepository checkoutOrderRepository;

    public CartServiceImpl(ApplicationEventPublisher eventPublisher, CartRepository cartRepository, CheckoutOrderRepository checkoutOrderRepository) {
        this.eventPublisher = eventPublisher;
        this.cartRepository = cartRepository;
        this.checkoutOrderRepository = checkoutOrderRepository;
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
    @Transactional
    public void checkout(CheckoutRequest request, StreamObserver<CheckoutResponse> responseObserver) {
        System.out.println("Checkout for user " + request.getUserId());
        
        List<CartItem> cartItems = cartRepository.findByUserId(request.getUserId());
        
        if (cartItems.isEmpty()) {
            responseObserver.onNext(CheckoutResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Cart is empty")
                    .build());
            responseObserver.onCompleted();
            return;
        }

        CheckoutOrder order = new CheckoutOrder();
        order.setUserId(request.getUserId());
        
        for (CartItem cartItem : cartItems) {
            CheckoutItem item = new CheckoutItem();
            item.setProductId(cartItem.getProductId());
            item.setQuantity(cartItem.getQuantity());
            order.addItem(item);
        }
        
        checkoutOrderRepository.save(order);
        cartRepository.deleteAll(cartItems);

        responseObserver.onNext(CheckoutResponse.newBuilder()
                .setSuccess(true)
                .setOrderId(order.getId())
                .setMessage("Checkout successful. Order placed.")
                .build());
        responseObserver.onCompleted();
    }
}
