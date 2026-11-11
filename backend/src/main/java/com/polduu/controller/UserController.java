package com.polduu.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.polduu.dto.user.PublicUserResponseDto;
import com.polduu.dto.user.UserPatchRequestDto;
import com.polduu.dto.user.UserResponseDto;
import com.polduu.service.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j 
@Tag (name = "User Controller", description = "Controller for user-related operations")
@RequestMapping ("/api/users")
public class UserController {
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping ("{id}")
    public ResponseEntity<PublicUserResponseDto> getUserProfile(@PathVariable UUID id) {
        log.info("Fetching user profile for ID: {}", id);
        return ResponseEntity.ok(userService.getPublicProfileById(id));
    }

    @PatchMapping ("/me")
    public ResponseEntity<UserResponseDto> updateUserProfile(@AuthenticationPrincipal UUID userId,
        @Valid @RequestBody UserPatchRequestDto userPatchRequestDto
    ) {
        log.info("Updating user profile for ID: {}", userId);
        return ResponseEntity.ok(userService.updateUserById(userId, userPatchRequestDto));
    }

    @GetMapping ("/me")
    public ResponseEntity<UserResponseDto> getCurrentUserProfile(@AuthenticationPrincipal UUID userId) {
        log.info("Fetching current user profile");
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping ("/all") // TODO: Admin only endpoint, add security later
    public List<UserResponseDto> getAllUsers() {
        log.info("Fetching all users");
        return userService.getAllUsers();
    }
}
