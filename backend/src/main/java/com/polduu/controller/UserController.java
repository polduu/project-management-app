package com.polduu.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.polduu.dto.user.*;
import com.polduu.service.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j 
@Tag (name = "User Controller", description = "Controller for user-related operations")
@RequestMapping ("/api/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping ("/register")
    public AuthResponseDto createUser(@Valid @RequestBody UserRegisterRequestDto user) {
        log.info("Creating new user: {}", user);
        return userService.createUser(user);
    }

    @PostMapping ("/google")
    public AuthResponseDto googleLogin(@Valid @RequestBody GoogleAuthRequestDto googleAuthRequest) {
        log.info("Google login endpoint hit: {}", googleAuthRequest);
        return userService.googleLogin(googleAuthRequest);
    }

    @PostMapping ("/github")
    public AuthResponseDto githubLogin(@Valid @RequestBody GitHubAuthRequestDto githubAuthRequest) {
        log.info("GitHub login endpoint hit: {}", githubAuthRequest);
        return userService.githubLogin(githubAuthRequest);
    }

    @PostMapping ("/login")
    public AuthResponseDto loginUser(@Valid @RequestBody UserLoginRequestDto userLoginRequestDto) {
        log.info("Login endpoint hit");
        return userService.logInUser(userLoginRequestDto);
    }

    @GetMapping ("{id}")
    public ResponseEntity<UserResponseDto> getUserProfile(@PathVariable UUID id) {
        log.info("Fetching user profile for ID: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping ("/all")
    public List<UserResponseDto> getAllUsers() {
        log.info("Fetching all users");
        return userService.getAllUsers();
    }

    @PatchMapping ("/profile/{id}")
    public ResponseEntity<UserResponseDto> updateUserProfile(@PathVariable UUID id, 
            @Valid @RequestBody UserPatchRequestDto userPatchRequestDto
    ) {
        log.info("Updating user profile for ID: {}", id);
        return ResponseEntity.ok(userService.updateUser(id, userPatchRequestDto));
    }


}
