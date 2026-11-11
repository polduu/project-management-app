package com.polduu.controller;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.polduu.dto.user.*;
import com.polduu.service.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;


@RestController
@Slf4j 
@Tag (name = "Auth Controller", description = "Controller for user authentication operations")
@RequestMapping ("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping ("/register")
    @ResponseStatus (code = HttpStatus.CREATED)
    public AuthResponseDto createUser(@Valid @RequestBody UserRegisterRequestDto user) {
        log.info("Creating new user: {}", user.email());
        return userService.createUser(user);
    }

    @PostMapping ("/google")
    public AuthResponseDto googleLogin(@Valid @RequestBody GoogleAuthRequestDto googleAuthRequest) {
        log.info("Google login endpoint hit: {}");
        return userService.googleLogin(googleAuthRequest);
    }

    @PostMapping ("/github")
    public AuthResponseDto githubLogin(@Valid @RequestBody GitHubAuthRequestDto githubAuthRequest) {
        log.info("GitHub login endpoint hit: {}");
        return userService.githubLogin(githubAuthRequest);
    }

    @PostMapping ("/login")
    public AuthResponseDto loginUser(@Valid @RequestBody UserLoginRequestDto userLoginRequestDto) {
        log.info("Login endpoint hit");
        return userService.logInUser(userLoginRequestDto);
    }

    @PostMapping ("/refresh")
    public AuthResponseDto refreshToken(@Valid @RequestBody RefreshTokenRequestDto refreshTokenRequest) {
        log.info("Refreshing token");
        return userService.refreshToken(refreshTokenRequest);
    }

    @PostMapping ("/logout")
    @ResponseStatus (code = HttpStatus.NO_CONTENT)
    public void logoutUser(@Valid @RequestBody RefreshTokenRequestDto refreshTokenRequest) {
        log.info("Logout endpoint hit");
        userService.logoutUser(refreshTokenRequest);
    }

}
