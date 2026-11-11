package com.polduu.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.polduu.dto.user.PublicUserResponseDto;
import com.polduu.dto.user.UserPatchRequestDto;
import com.polduu.dto.user.UserResponseDto;
import com.polduu.exception.ConflictException;
import com.polduu.exception.GlobalExceptionHandler;
import com.polduu.exception.ResourceNotFoundException;
import com.polduu.security.JwtService;
import com.polduu.security.SecurityConfig;
import com.polduu.service.UserService;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class UserControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean JwtService jwtService;
    @MockBean UserService userService;

    // Authenticates the request with a UUID principal, like JwtAuthenticationFilter does
    private static RequestPostProcessor authenticatedAs(UUID userId) {
        return authentication(new UsernamePasswordAuthenticationToken(userId, null, List.of()));
    }

    /*------------------------------------------------------------------------------------------------- */

    // Tests for GET /api/users/{id}

    @Test
    void getUserProfile_withValidId_returnsPublicProfileWithoutEmail() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.getPublicProfileById(id))
                .thenReturn(new PublicUserResponseDto("pol", "Pol", null));

        mockMvc.perform(get("/api/users/{id}", id)
                .with(authenticatedAs(UUID.randomUUID())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("pol"))
                .andExpect(jsonPath("$.fullName").value("Pol"))
                .andExpect(jsonPath("$.email").doesNotExist());

        verify(userService).getPublicProfileById(id);
    }

    @Test
    void getUserProfile_withUnknownId_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.getPublicProfileById(id))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/{id}", id)
                .with(authenticatedAs(UUID.randomUUID())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void getUserProfile_withMalformedUuid_returns400() throws Exception {
        mockMvc.perform(get("/api/users/{id}", "not-a-uuid")
                .with(authenticatedAs(UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserProfile_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/users/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    /*------------------------------------------------------------------------------------------------- */

    // Tests for GET /api/users/me

    @Test
    void getCurrentUserProfile_returnsProfileOfAuthenticatedUser() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.getUserById(userId))
                .thenReturn(new UserResponseDto(userId, "pol", "pol@test.com", "Pol", null));

        mockMvc.perform(get("/api/users/me")
                .with(authenticatedAs(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("pol@test.com"))
                .andExpect(jsonPath("$.username").value("pol"))
                .andExpect(jsonPath("$.fullName").value("Pol"));

        verify(userService).getUserById(userId);
    }

    @Test
    void getCurrentUserProfile_withNonExistentUser_returns404() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.getUserById(userId))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/users/me")
                .with(authenticatedAs(userId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCurrentUserProfile_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    /*------------------------------------------------------------------------------------------------- */

    // Tests for PATCH /api/users/me

    @Test
    void updateUserProfile_withValidData_returnsUpdatedProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        when(userService.updateUserById(eq(userId), any(UserPatchRequestDto.class)))
                .thenReturn(new UserResponseDto(userId, "newusername", "pol@test.com", "New Full Name", null));

        mockMvc.perform(patch("/api/users/me")
                .with(authenticatedAs(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"newusername",
                    "fullName":"New Full Name"}
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newusername"));

        verify(userService).updateUserById(eq(userId), any(UserPatchRequestDto.class));
    }

    @Test
    void updateUserProfile_withTooShortUsername_returns400() throws Exception {
        mockMvc.perform(patch("/api/users/me")
                .with(authenticatedAs(UUID.randomUUID()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"ab"}
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.username").exists());

        verify(userService, never()).updateUserById(any(), any());
    }

    @Test
    void updateUserProfile_withExistingUsername_returns409() throws Exception {
        when(userService.updateUserById(any(), any(UserPatchRequestDto.class)))
                .thenThrow(new ConflictException("Username already exists"));

        mockMvc.perform(patch("/api/users/me")
                .with(authenticatedAs(UUID.randomUUID()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username":"existingusername"}
                    """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    void updateUserProfile_withInvalidJson_returns400() throws Exception {
        mockMvc.perform(patch("/api/users/me")
                .with(authenticatedAs(UUID.randomUUID()))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed request body"));
    }

    @Test
    void updateUserProfile_withoutToken_returns401() throws Exception {
        mockMvc.perform(patch("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    /*------------------------------------------------------------------------------------------------- */
    // Tests for GET /api/users/all
    // NOTE: any authenticated user can list every email. Remove these tests together with
    // the endpoint, or change them to expect 403 once roles exist.

    @Test
    void getAllUsers_returnsListOfUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(
                new UserResponseDto(UUID.randomUUID(), "pol", "pol@test.com", "Pol", null),
                new UserResponseDto(UUID.randomUUID(), "lusix", "lucia@test.com", "Lucia", null)));

        mockMvc.perform(get("/api/users/all")
                .with(authenticatedAs(UUID.randomUUID())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("pol"))
                .andExpect(jsonPath("$[1].username").value("lusix"));
    }
}