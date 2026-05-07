package com.remarketing.product.service;

import com.remarketing.product.grpc.*;
import com.remarketing.product.grpc.ProductServiceGrpc;
import com.remarketing.core.event.UserSearchEvent;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.UUID;

@GrpcService
public class ProductServiceImpl extends ProductServiceGrpc.ProductServiceImplBase {

    private final ApplicationEventPublisher eventPublisher;

    public ProductServiceImpl(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void searchProducts(SearchRequest request, StreamObserver<SearchResponse> responseObserver) {
        // Publish an internal event for user-behavior tracking
        if (request.getUserId() != null && !request.getUserId().isEmpty()) {
            eventPublisher.publishEvent(new UserSearchEvent(this, request.getUserId(), request.getQuery()));
        }

        // Mocking product search
        Product p1 = Product.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setName(request.getQuery() + " Pro")
                .setPrice(99.99)
                .setCategory("Electronics")
                .build();

        Product p2 = Product.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setName(request.getQuery() + " Basic")
                .setPrice(49.99)
                .setCategory("Electronics")
                .build();

        responseObserver.onNext(SearchResponse.newBuilder()
                .addAllProducts(List.of(p1, p2))
                .build());
        responseObserver.onCompleted();
    }
}
