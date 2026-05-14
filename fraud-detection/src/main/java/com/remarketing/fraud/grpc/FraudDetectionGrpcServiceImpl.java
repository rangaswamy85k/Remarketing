package com.remarketing.fraud.grpc;

import com.remarketing.fraud.proto.*;
import com.remarketing.fraud.proto.FraudDetectionServiceGrpc.FraudDetectionServiceImplBase;
import com.remarketing.fraud.service.FraudDetectionService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.util.List;

@GrpcService
public class FraudDetectionGrpcServiceImpl extends FraudDetectionServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(FraudDetectionGrpcServiceImpl.class);

    @Autowired
    private FraudDetectionService service;

    @Override
    public void createAlert(CreateAlertRequest request, StreamObserver<CreateAlertResponse> responseObserver) {
        var alert = service.createAlert(
                request.getCampaignId(),
                request.getAlertType(),
                request.getDescription()
        );
        var response = CreateAlertResponse.newBuilder()
                .setAlertId(alert.getId())
                .setMessage("Alert recorded")
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void listAlerts(ListAlertsRequest request, StreamObserver<ListAlertsResponse> responseObserver) {
        Instant from = request.getFromTs() == 0 ? Instant.EPOCH : Instant.ofEpochMilli(request.getFromTs());
        Instant to = request.getToTs() == 0 ? Instant.now() : Instant.ofEpochMilli(request.getToTs());
        List<com.remarketing.fraud.model.FraudAlert> alerts = service.listAlerts(request.getCampaignId(), from, to);
        var builder = ListAlertsResponse.newBuilder();
        for (var a : alerts) {
            builder.addAlerts(Alert.newBuilder()
                    .setAlertId(a.getId())
                    .setCampaignId(a.getCampaignId())
                    .setAlertType(a.getAlertType())
                    .setDescription(a.getDescription())
                    .setTimestamp(a.getCreatedAt().toEpochMilli())
                    .build());
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void streamAlerts(StreamAlertsRequest request, StreamObserver<Alert> responseObserver) {
        // Placeholder: in a real system this would push live alerts via a reactive source.
        responseObserver.onCompleted();
    }
}
