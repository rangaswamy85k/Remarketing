package com.remarketing.behavior.service;

import com.remarketing.behavior.grpc.*;
import com.remarketing.behavior.entity.UserActivity;
import com.remarketing.behavior.repository.UserActivityRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@GrpcService
public class BehaviorServiceImpl extends BehaviorServiceGrpc.BehaviorServiceImplBase {

    private final UserActivityRepository userActivityRepository;

    @Autowired
    public BehaviorServiceImpl(UserActivityRepository userActivityRepository) {
        this.userActivityRepository = userActivityRepository;
    }

    @Override
    public void logActivity(LogActivityRequest request, StreamObserver<LogActivityResponse> responseObserver) {
        UserActivity activity = new UserActivity();
        activity.setUserId(request.getUserId());
        activity.setActivityType(request.getActivityType());
        activity.setDetails(request.getDetails());
        
        userActivityRepository.save(activity);
        System.out.println("Logged Activity for user " + request.getUserId() + ": " + request.getActivityType());
        
        responseObserver.onNext(LogActivityResponse.newBuilder().setSuccess(true).build());
        responseObserver.onCompleted();
    }

    @Override
    public void getUserHistory(GetUserHistoryRequest request, StreamObserver<GetUserHistoryResponse> responseObserver) {
        List<UserActivity> activities = userActivityRepository.findByUserIdOrderByTimestampDesc(request.getUserId());
        
        GetUserHistoryResponse.Builder responseBuilder = GetUserHistoryResponse.newBuilder();
        
        for (UserActivity act : activities) {
            responseBuilder.addActivities(Activity.newBuilder()
                    .setActivityType(act.getActivityType())
                    .setDetails(act.getDetails() != null ? act.getDetails() : "")
                    .setTimestamp(act.getTimestamp().toString())
                    .build());
        }
        
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }
}
