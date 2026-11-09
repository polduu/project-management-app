package com.polduu.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.polduu.dto.user.*;
import com.polduu.model.AuthProvider;
import com.polduu.model.User;
import com.polduu.repository.UserRepository;

@Service 
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponseDto createUser(UserRegisterRequestDto user) {
        User newUser = DtoMapToModel(user);
        User savedUser = userRepository.save(newUser);
        return ModelMapToDto(savedUser);
    }

    public AuthResponseDto googleLogin(GoogleAuthRequestDto googleAuthRequest) {
        throw new UnsupportedOperationException("Google login is not implemented");
    }

    public AuthResponseDto githubLogin(GitHubAuthRequestDto githubAuthRequest) {
        throw new UnsupportedOperationException("GitHub login is not implemented");
    }

    public UserResponseDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuari no trobat"));
        
        return new UserResponseDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getImageUrl()
            
        );
    }

    private User DtoMapToModel(UserRegisterRequestDto request) {
        return User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.name())
                .authProvider(AuthProvider.LOCAL)
                .build();
    }

    private AuthResponseDto ModelMapToDto(User user) {
        return new AuthResponseDto(
            "JWT_TOKEN", // Placeholder for JWT token generation
            "REFRESH_TOKEN", // Placeholder for refresh token generation
            900L, // Ex: 900 (segons)
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getImageUrl()
        );
    }

    public AuthResponseDto logInUser(UserLoginRequestDto request) {
        
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Email not found"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        // Placeholder for JWT token generation
        String token = "JWT_TOKEN";

        return new AuthResponseDto(
            token,
            "REFRESH_TOKEN",
            900L,
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getImageUrl()
        );
    }  
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponseDto(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getImageUrl()
                ))
                .toList();
    }
    public UserResponseDto updateUser(UUID id, UserPatchRequestDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuari no trobat"));

        if (request.username() != null) {
            user.setUsername(request.username());
        }
        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }
        if (request.imageUrl() != null) {
            user.setImageUrl(request.imageUrl());
        }

        User updatedUser = userRepository.save(user);

        return new UserResponseDto(
            updatedUser.getId(),
            updatedUser.getUsername(),
            updatedUser.getEmail(),
            updatedUser.getFullName(),
            updatedUser.getImageUrl()
        );
    }



    



}
