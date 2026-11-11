package com.polduu.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterRequestDto(
    @NotBlank 
    String username,
    @NotBlank 
    @Email
    String email,
    @NotBlank
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters long")
    String password,
    @NotBlank
    String name
) {}
