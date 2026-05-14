package com.remarketing.security;

import io.grpc.*;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

@GrpcGlobalServerInterceptor
public class GrpcSecurityInterceptor implements ServerInterceptor {

    private final JwtUtil jwtUtil;
    public static final Context.Key<String> USER_ID_CONTEXT_KEY = Context.key("userId");
    public static final Context.Key<String> ROLE_CONTEXT_KEY = Context.key("role");

    public GrpcSecurityInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String methodName = call.getMethodDescriptor().getFullMethodName();
        // Allow unauthenticated access to Auth endpoints
        if (methodName.startsWith("com.remarketing.auth.AuthService/")) {
            return next.startCall(call, headers);
        }

        String authHeader = headers.get(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER));
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                String userId = jwtUtil.getUserIdFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                
                if (methodName.startsWith("com.remarketing.campaign.CampaignService/")) {
                    if (methodName.endsWith("ClickNotification")) {
                        if (!"USER".equals(role)) {
                            call.close(Status.PERMISSION_DENIED.withDescription("Requires USER role"), new Metadata());
                            return new ServerCall.Listener<ReqT>() {};
                        }
                    } else {
                        if (!"ADMIN".equals(role)) {
                            call.close(Status.PERMISSION_DENIED.withDescription("Requires ADMIN role"), new Metadata());
                            return new ServerCall.Listener<ReqT>() {};
                        }
                    }
                }
                
                if (methodName.startsWith("com.remarketing.analytics.AnalyticsService/")) {
                    if (!"ANALYST".equals(role)) {
                        call.close(Status.PERMISSION_DENIED.withDescription("Requires ANALYST role"), new Metadata());
                        return new ServerCall.Listener<ReqT>() {};
                    }
                }

                if (methodName.startsWith("com.remarketing.fraud.FraudDetectionService/")) {
                    if (!"ADMIN".equals(role)) {
                        call.close(Status.PERMISSION_DENIED.withDescription("Requires ADMIN role"), new Metadata());
                        return new ServerCall.Listener<ReqT>() {};
                    }
                }

                Context ctx = Context.current()
                    .withValue(USER_ID_CONTEXT_KEY, userId)
                    .withValue(ROLE_CONTEXT_KEY, role);
                return Contexts.interceptCall(ctx, call, headers, next);
            }
        }

        call.close(Status.UNAUTHENTICATED.withDescription("Invalid or missing JWT token"), new Metadata());
        return new ServerCall.Listener<ReqT>() {};
    }
}
