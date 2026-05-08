package com.remarketing.product.service;

import com.remarketing.product.grpc.*;
import com.remarketing.product.grpc.ProductServiceGrpc;
import com.remarketing.product.entity.UserSearch;
import com.remarketing.product.repository.UserSearchRepository;
import com.remarketing.core.event.UserSearchEvent;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

@GrpcService
public class ProductServiceImpl extends ProductServiceGrpc.ProductServiceImplBase {

    private final ApplicationEventPublisher eventPublisher;
    private final com.remarketing.product.repository.ProductRepository productRepository;
    private final UserSearchRepository userSearchRepository;

    public ProductServiceImpl(ApplicationEventPublisher eventPublisher, 
                              com.remarketing.product.repository.ProductRepository productRepository,
                              UserSearchRepository userSearchRepository) {
        this.eventPublisher = eventPublisher;
        this.productRepository = productRepository;
        this.userSearchRepository = userSearchRepository;
    }

    @Override
    public void searchProducts(SearchRequest request, StreamObserver<SearchResponse> responseObserver) {
        String category = request.getQuery(); // query field is used as category
        String userId = request.getUserId();

        // Publish an internal event for user-behavior tracking
        if (userId != null && !userId.isEmpty()) {
            eventPublisher.publishEvent(new UserSearchEvent(this, userId, category));
            
            // Save/update user's latest category search (one record per user)
            Optional<UserSearch> existingSearch = userSearchRepository.findByUserId(userId);
            if (existingSearch.isPresent()) {
                UserSearch search = existingSearch.get();
                search.setCategory(category);
                userSearchRepository.save(search);
            } else {
                UserSearch search = new UserSearch();
                search.setUserId(userId);
                search.setCategory(category);
                userSearchRepository.save(search);
            }
            System.out.println("User " + userId + " searched for category: " + category);
        }

        // Fetch products by category from database
        List<com.remarketing.product.entity.ProductEntity> entities = productRepository.findByCategoryIgnoreCase(category);

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

    @Override
    public void addProduct(AddProductRequest request, StreamObserver<AddProductResponse> responseObserver) {
        com.remarketing.product.entity.ProductEntity product = new com.remarketing.product.entity.ProductEntity();
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setCategory(request.getCategory());
        
        productRepository.save(product);
        
        System.out.println("Admin added product: " + product.getName() + " in category: " + product.getCategory());
        
        responseObserver.onNext(AddProductResponse.newBuilder()
                .setSuccess(true)
                .setProductId(product.getId())
                .setMessage("Product added successfully")
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void removeProduct(RemoveProductRequest request, StreamObserver<RemoveProductResponse> responseObserver) {
        Optional<com.remarketing.product.entity.ProductEntity> productOpt = productRepository.findById(request.getProductId());
        
        if (productOpt.isPresent()) {
            productRepository.deleteById(request.getProductId());
            System.out.println("Admin removed product ID: " + request.getProductId());
            
            responseObserver.onNext(RemoveProductResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Product removed successfully")
                    .build());
        } else {
            responseObserver.onNext(RemoveProductResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Product not found")
                    .build());
        }
        responseObserver.onCompleted();
    }
}
