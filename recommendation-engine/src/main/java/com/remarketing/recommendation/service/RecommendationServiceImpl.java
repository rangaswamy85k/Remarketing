package com.remarketing.recommendation.service;

import com.remarketing.product.entity.ProductEntity;
import com.remarketing.product.entity.UserSearch;
import com.remarketing.product.grpc.Product;
import com.remarketing.product.repository.ProductRepository;
import com.remarketing.product.repository.UserSearchRepository;
import com.remarketing.recommendation.grpc.*;
import com.remarketing.recommendation.grpc.RecommendationServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@GrpcService
public class RecommendationServiceImpl extends RecommendationServiceGrpc.RecommendationServiceImplBase {

    private final ProductRepository productRepository;
    private final UserSearchRepository userSearchRepository;

    @Autowired
    public RecommendationServiceImpl(ProductRepository productRepository, UserSearchRepository userSearchRepository) {
        this.productRepository = productRepository;
        this.userSearchRepository = userSearchRepository;
    }

    @Override
    public void getRecommendations(RecommendationRequest request, StreamObserver<RecommendationResponse> responseObserver) {
        System.out.println("Fetching true recommendations for User: " + request.getUserId());
        
        Optional<UserSearch> searchOpt = userSearchRepository.findByUserId(request.getUserId());
        RecommendationResponse.Builder responseBuilder = RecommendationResponse.newBuilder();
        
        if (searchOpt.isPresent()) {
            String category = searchOpt.get().getCategory();
            List<ProductEntity> categoryProducts = productRepository.findByCategoryIgnoreCase(category);
            
            for (ProductEntity entity : categoryProducts) {
                responseBuilder.addRecommendedProducts(Product.newBuilder()
                        .setId(entity.getId())
                        .setName(entity.getName())
                        .setPrice(entity.getPrice())
                        .setCategory(entity.getCategory())
                        .build());
            }
            System.out.println("Found " + categoryProducts.size() + " personalized recommendations for category: " + category);
        } else {
            System.out.println("No search history found. Returning no recommendations.");
        }

        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}
