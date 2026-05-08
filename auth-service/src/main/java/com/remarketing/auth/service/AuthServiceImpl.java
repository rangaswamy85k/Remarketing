package com.remarketing.auth.service;

import com.remarketing.auth.entity.User;
import com.remarketing.auth.grpc.AuthServiceGrpc;
import com.remarketing.auth.grpc.*;
import com.remarketing.auth.repository.UserRepository;
import com.remarketing.security.JwtUtil;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

@GrpcService
public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthServiceImpl(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
        String roleFromRequest = request.getRole();
        String userRole;
        if (roleFromRequest == null || roleFromRequest.trim().isEmpty()) {
            userRole = "USER";
        } else {
            userRole = roleFromRequest.trim().toUpperCase();
        }

        // Block re-registration by username
        if (userRepository.existsByUsername(request.getUsername())) {
            responseObserver.onNext(RegisterResponse.newBuilder()
                    .setMessage("Username already registered. Please login instead.")
                    .build());
            responseObserver.onCompleted();
            return;
        }

        // Block duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            responseObserver.onNext(RegisterResponse.newBuilder()
                    .setMessage("Email already registered by another user. Please use a different email.")
                    .build());
            responseObserver.onCompleted();
            return;
        }
        System.out.println("=== REGISTRATION DEBUG ===");
        System.out.println("Username: " + request.getUsername());
        System.out.println("Email: " + request.getEmail());
        System.out.println("Role assigned: " + userRole);

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(userRole)
                .build();

        userRepository.save(user);

        responseObserver.onNext(RegisterResponse.newBuilder()
                .setUserId(user.getId())
                .setMessage("User registered successfully as " + userRole)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        
        if (userOpt.isPresent() && passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            String role = userOpt.get().getRole() != null ? userOpt.get().getRole() : "USER";
            String token = jwtUtil.generateToken(userOpt.get().getId(), userOpt.get().getUsername(), role);
            responseObserver.onNext(LoginResponse.newBuilder()
                    .setToken(token)
                    .setMessage("Login successful")
                    .setRole(role)
                    .build());
        } else {
            responseObserver.onNext(LoginResponse.newBuilder()
                    .setMessage("Invalid credentials")
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void logout(LogoutRequest request, StreamObserver<LogoutResponse> responseObserver) {
        // In a stateless JWT implementation, logout is mostly handled client-side by deleting the token.
        // For a more secure implementation, we could blacklist the token here.
        System.out.println("User requested logout. Token to be dropped by client.");
        
        responseObserver.onNext(LogoutResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Logged out successfully. Please discard the token on the client side.")
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteAccount(DeleteAccountRequest request, StreamObserver<DeleteAccountResponse> responseObserver) {
        String userId = request.getUserId();
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (userOpt.isPresent()) {
            userRepository.deleteById(userId);
            System.out.println("Account deleted for user ID: " + userId);
            
            responseObserver.onNext(DeleteAccountResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Account deleted successfully")
                    .build());
        } else {
            responseObserver.onNext(DeleteAccountResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Account not found")
                    .build());
        }
        responseObserver.onCompleted();
    }
}
