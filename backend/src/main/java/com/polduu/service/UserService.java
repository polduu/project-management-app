package com.polduu.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.polduu.dto.user.*;
import com.polduu.exception.ConflictException;
import com.polduu.exception.InvalidRefreshTokenException;
import com.polduu.exception.ResourceNotFoundException;
import com.polduu.model.AuthProvider;
import com.polduu.model.RefreshToken;
import com.polduu.model.User;
import com.polduu.repository.UserRepository;
import com.polduu.security.JwtService;

import org.springframework.transaction.annotation.Transactional;


@Service 
@Transactional 
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService
            , RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public AuthResponseDto createUser(UserRegisterRequestDto user) {

        if (userRepository.existsByEmail(user.email())) {
            throw new ConflictException("Email already exists");
        }
        if (userRepository.existsByUsername(user.username())) {
            throw new ConflictException("Username already exists");
        }
        User newUser = toUserModel(user);
        User savedUser = userRepository.save(newUser);
        String token = jwtService.generateToken(savedUser.getId());
        RefreshToken refreshToken = refreshTokenService.renewRefreshToken(savedUser);
        
        return toAuthResponseDto(savedUser, token, refreshToken.getToken());
    }

    public AuthResponseDto googleLogin(GoogleAuthRequestDto googleAuthRequest) {
        throw new UnsupportedOperationException("Google login is not implemented");
    }

    public AuthResponseDto githubLogin(GitHubAuthRequestDto githubAuthRequest) {
        throw new UnsupportedOperationException("GitHub login is not implemented");
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return toUserResponseDto(user);
    }

    private User toUserModel(UserRegisterRequestDto request) {
        return User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.name())
                .authProvider(AuthProvider.LOCAL)
                .build();
    }

    public AuthResponseDto logInUser(UserLoginRequestDto request) {
        
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ConflictException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ConflictException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getId());
        RefreshToken refreshToken = refreshTokenService.renewRefreshToken(user);
        
        return toAuthResponseDto(user, token, refreshToken.getToken());
    }  
    @Transactional (readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> toUserResponseDto(user)
                )
                .toList();
    }
    public UserResponseDto updateUserById(UUID id, UserPatchRequestDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (request.username() != null && !request.username().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.username())) {
                throw new ConflictException("Username already exists"); 
            }
            user.setUsername(request.username());
        }
        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }
        if (request.imageUrl() != null) {
            user.setImageUrl(request.imageUrl());
        }

        User updatedUser = userRepository.save(user);

        return toUserResponseDto(updatedUser);
    }

    @Transactional (noRollbackFor = InvalidRefreshTokenException.class)
    public AuthResponseDto refreshToken(RefreshTokenRequestDto refreshTokenRequest) {
        
        String requestRefreshToken = refreshTokenRequest.refreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(refreshToken -> {
                    String token = jwtService.generateToken(refreshToken.getUser().getId());
                    return toAuthResponseDto(refreshToken.getUser(), token, refreshToken.getToken());
                })
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token is not in database!"));
    }

    private UserResponseDto toUserResponseDto(User user) {
        return new UserResponseDto(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getImageUrl()
        );
    }

    private AuthResponseDto toAuthResponseDto(User user, String token, String refreshToken) {
        Long expiresIn = jwtService.getJwtExpirationTime(); // Get expiration time in seconds
        return new AuthResponseDto(
            token,
            refreshToken,
            expiresIn,
            toUserResponseDto(user)
        );
    }

    public PublicUserResponseDto getPublicProfileById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new PublicUserResponseDto(
            user.getEmail(),
            user.getUsername(),
            user.getFullName(),
            user.getImageUrl()
        );
    }

    public void logoutUser(RefreshTokenRequestDto request) {
        refreshTokenService.deleteByToken(request.refreshToken());
    }



    



}
