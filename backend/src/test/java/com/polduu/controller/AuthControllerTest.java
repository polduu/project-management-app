package com.polduu.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.polduu.dto.user.AuthResponseDto;
import com.polduu.dto.user.UserResponseDto;
import com.polduu.exception.GlobalExceptionHandler;
import com.polduu.exception.InvalidCredentialsException;
import com.polduu.security.JwtService;
import com.polduu.security.SecurityConfig;
import com.polduu.service.UserService;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean UserService userService;
    @MockBean JwtService jwtService; // lo necesita JwtAuthenticationFilter

    @Test
    void login_withValidCredentials_returns200() throws Exception {
        var response = new AuthResponseDto("access", "refresh", 900L,
                new UserResponseDto(UUID.randomUUID(), "pol", "pol@test.com", "Pol", null));
        when(userService.logInUser(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"pol@test.com","password":"secret123"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").value("access"))
            .andExpect(jsonPath("$.refreshToken").value("refresh"));
    }

    @Test
    void login_withInvalidCredentials_returns401() throws Exception {
        when(userService.logInUser(any()))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"pol@test.com","password":"wrong"}
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void register_withInvalidBody_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors").exists());
    }
}