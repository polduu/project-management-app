package com.polduu.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import io.jsonwebtoken.JwtException;

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
import com.polduu.exception.ConflictException;
import com.polduu.exception.GlobalExceptionHandler;
import com.polduu.exception.InvalidCredentialsException;
import com.polduu.exception.InvalidRefreshTokenException;
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

        private static final String EMAIL = "pol@test.com";
        private static final String USERNAME = "pol";
        private static final String FULLNAME = "Pol";
        private static final String REFRESH_TOKEN = "refresh";
        private static final String ACCESS_TOKEN = "access";

        // Login Tests

        @Test
        void login_withValidCredentials_returns200() throws Exception {
                var response = new AuthResponseDto(ACCESS_TOKEN, REFRESH_TOKEN, 900L,
                        new UserResponseDto(UUID.randomUUID(), USERNAME, EMAIL, FULLNAME, null));
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
        void login_withInvalidBody_returns400WithFieldErrors() throws Exception {
                mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.fieldErrors").exists());
        }

        @Test
        void login_withExpiredBearerToken_stillWorks() throws Exception {
                when(jwtService.extractUserId(any())).thenThrow(new JwtException("expired"));
                when(userService.logInUser(any())).thenReturn(new AuthResponseDto(ACCESS_TOKEN, REFRESH_TOKEN, 900L,
                new UserResponseDto(UUID.randomUUID(), USERNAME, EMAIL, FULLNAME, null)));

        mockMvc.perform(post("/api/auth/login")
                .header("Authorization", "Bearer expired.token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"pol@test.com","password":"secret123"}
                        """))
                .andExpect(status().isOk());
        }

        @Test
        void login_withMalformedJson_returns400() throws Exception {
                mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.message").value("Malformed request body"));
        }

        @Test
        void login_withWrongContentType_returns415() throws Exception {
                mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("hello"))
                        .andExpect(status().isUnsupportedMediaType());
        }

        /*------------------------------------------------------------------------------------------------*/

        // Register Tests
        @Test
        void register_withValidBody_returns201() throws Exception {
                var response = new AuthResponseDto(ACCESS_TOKEN, REFRESH_TOKEN, 900L, 
                        new UserResponseDto(UUID.randomUUID(), USERNAME, EMAIL, FULLNAME, null));
                when(userService.createUser(any())).thenReturn(response);

                mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"username":"pol","email":"pol@test.com","password":"secret123", "name":"Pol"}
                        """))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.accessToken").value("access"))
                        .andExpect(jsonPath("$.refreshToken").value("refresh"));
        }

        @Test
        void register_withInvalidBody_returns400WithFieldErrors() throws Exception {
                mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists());
        }

        @Test 
        void register_withExistingEmail_returns409() throws Exception {
                when(userService.createUser(any()))
                        .thenThrow(new ConflictException("Email already exists"));

                mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"username":"pol","email":"pol@test.com","password":"secret123", "name":"Pol"}
                        """))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.message").value("Email already exists"));
        }

        @Test
        void register_withInvalidEmail_returns400() throws Exception {
                mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"pol","email":"not-an-email","password":"secret123","name":"Pol"}
                                """))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.fieldErrors.email").exists());
        }

        @Test
        void register_withShortPassword_returns400() throws Exception {
                mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"pol","email":"pol@test.com","password":"123","name":"Pol"}
                                """))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.fieldErrors.password").exists());
        }

        /*------------------------------------------------------------------------------------------------*/

        // Refresh Token Tests

        @Test 
        void refreshToken_withValidBody_returns200() throws Exception {
                var response = new AuthResponseDto(ACCESS_TOKEN, REFRESH_TOKEN, 900L, 
                        new UserResponseDto(UUID.randomUUID(), USERNAME, EMAIL, FULLNAME, null));
                when(userService.refreshToken(any())).thenReturn(response);

                mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"refreshToken":"refresh"}
                        """))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.accessToken").value("access"))
                        .andExpect(jsonPath("$.refreshToken").value("refresh"));
        }

        @Test 
        void refreshToken_withInvalidBody_returns400WithFieldErrors() throws Exception {
                mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.fieldErrors").exists());
        }

        @Test
        void refreshToken_withInvalidToken_returns401() throws Exception {
                when(userService.refreshToken(any()))
                        .thenThrow(new InvalidRefreshTokenException("Invalid refresh token"));

                mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"refreshToken":"invalid"}
                        """))
                        .andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.message").value("Invalid refresh token"));
        }

        /*------------------------------------------------------------------------------------------------*/

        // Logout Tests

        @Test 
        void logoutUser_withValidBody_returns204() throws Exception {
                mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"refreshToken":"refresh"}
                        """))
                        .andExpect(status().isNoContent());
                
                verify(userService).logoutUser(any());
        }

        @Test 
        void logoutUser_withInvalidBody_returns400WithFieldErrors() throws Exception {
                mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.fieldErrors").exists());
        }

}
