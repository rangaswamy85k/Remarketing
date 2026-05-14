package com.remarketing.auth.service;

import com.remarketing.auth.entity.User;
import com.remarketing.auth.grpc.*;
import com.remarketing.auth.repository.UserRepository;
import com.remarketing.security.JwtUtil;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private StreamObserver<RegisterResponse> registerObserver;

    @Mock
    private StreamObserver<LoginResponse> loginObserver;

    @Mock
    private StreamObserver<DeleteAccountResponse> deleteAccountObserver;

    @InjectMocks
    private AuthServiceImpl service;

    @Test
    void register_Success() {
        // Arrange
        RegisterRequest request = RegisterRequest.newBuilder()
                .setUsername("testuser")
                .setPassword("password")
                .setEmail("test@example.com")
                .setRole("ADMIN")
                .build();

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);

        // Act
        service.register(request, registerObserver);

        // Assert
        ArgumentCaptor<RegisterResponse> responseCaptor = ArgumentCaptor.forClass(RegisterResponse.class);
        verify(registerObserver).onNext(responseCaptor.capture());
        verify(registerObserver).onCompleted();
        verify(userRepository).save(any(User.class));

        RegisterResponse response = responseCaptor.getValue();
        assertTrue(response.getMessage().contains("User registered successfully"));
    }

    @Test
    void register_DuplicateUsername_Fails() {
        // Arrange
        RegisterRequest request = RegisterRequest.newBuilder()
                .setUsername("existing")
                .build();

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        // Act
        service.register(request, registerObserver);

        // Assert
        ArgumentCaptor<RegisterResponse> responseCaptor = ArgumentCaptor.forClass(RegisterResponse.class);
        verify(registerObserver).onNext(responseCaptor.capture());
        verify(userRepository, never()).save(any(User.class));

        assertEquals("Username already registered. Please login instead.", responseCaptor.getValue().getMessage());
    }

    @Test
    void deleteAccount_Success() {
        // Arrange
        DeleteAccountRequest request = DeleteAccountRequest.newBuilder()
                .setUserId("user-123")
                .build();

        when(userRepository.findById("user-123")).thenReturn(Optional.of(new User()));

        // Act
        service.deleteAccount(request, deleteAccountObserver);

        // Assert
        verify(userRepository).deleteById("user-123");
        ArgumentCaptor<DeleteAccountResponse> responseCaptor = ArgumentCaptor.forClass(DeleteAccountResponse.class);
        verify(deleteAccountObserver).onNext(responseCaptor.capture());
        assertTrue(responseCaptor.getValue().getSuccess());
    }
}
