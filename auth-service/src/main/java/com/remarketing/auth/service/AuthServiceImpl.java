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
        if (userRepository.existsByUsername(request.getUsername())) {
            responseObserver.onNext(RegisterResponse.newBuilder()
                    .setMessage("Username already exists")
                    .build());
            responseObserver.onCompleted();
            return;
        }

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .build();

        userRepository.save(user);

        responseObserver.onNext(RegisterResponse.newBuilder()
                .setUserId(user.getId())
                .setMessage("User registered successfully")
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());
        
        if (userOpt.isPresent() && passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            String token = jwtUtil.generateToken(userOpt.get().getId(), userOpt.get().getUsername());
            responseObserver.onNext(LoginResponse.newBuilder()
                    .setToken(token)
                    .setMessage("Login successful")
                    .build());
        } else {
            responseObserver.onNext(LoginResponse.newBuilder()
                    .setMessage("Invalid credentials")
                    .build());
        }
        responseObserver.onCompleted();
    }
}
