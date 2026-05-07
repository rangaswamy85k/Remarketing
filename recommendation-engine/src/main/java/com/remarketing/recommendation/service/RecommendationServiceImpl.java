package com.remarketing.recommendation.service;

import com.remarketing.product.grpc.Product;
import com.remarketing.recommendation.grpc.*;
import com.remarketing.recommendation.grpc.RecommendationServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import java.util.List;
import java.util.UUID;

@GrpcService
public class RecommendationServiceImpl extends RecommendationServiceGrpc.RecommendationServiceImplBase {

    @Override
    public void getRecommendations(RecommendationRequest request, StreamObserver<RecommendationResponse> responseObserver) {
        System.out.println("Fetching recommendations for User: " + request.getUserId());
        
        // Future MySQL integration: Query top 5 searches and top 5 cart additions.
        
        Product p1 = Product.newBuilder()
                .setId(UUID.randomUUID().toString())
                .setName("Suggested Item Based on Recent Search")
                .setPrice(199.99)
                .setCategory("Personalized")
                .build();

        responseObserver.onNext(RecommendationResponse.newBuilder()
                .addRecommendedProducts(p1)
                .build());
        responseObserver.onCompleted();
    }
}
