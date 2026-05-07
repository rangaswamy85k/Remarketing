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
    private final com.remarketing.product.repository.ProductRepository productRepository;

    public ProductServiceImpl(ApplicationEventPublisher eventPublisher, com.remarketing.product.repository.ProductRepository productRepository) {
        this.eventPublisher = eventPublisher;
        this.productRepository = productRepository;
    }

    @Override
    public void searchProducts(SearchRequest request, StreamObserver<SearchResponse> responseObserver) {
        // Publish an internal event for user-behavior tracking
        if (request.getUserId() != null && !request.getUserId().isEmpty()) {
            eventPublisher.publishEvent(new UserSearchEvent(this, request.getUserId(), request.getQuery()));
        }

        // Fetch products from database
        List<com.remarketing.product.entity.ProductEntity> entities = productRepository.findByNameContainingIgnoreCase(request.getQuery());

        List<Product> products = entities.stream()
                .map(entity -> Product.newBuilder()
                        .setId(entity.getId())
                        .setName(entity.getName())
                        .setPrice(entity.getPrice())
                        .setCategory(entity.getCategory())
                        .build())
                .toList();

        responseObserver.onNext(SearchResponse.newBuilder()
                .addAllProducts(products)
                .build());
        responseObserver.onCompleted();
    }
}
