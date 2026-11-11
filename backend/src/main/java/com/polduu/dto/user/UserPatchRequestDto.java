package com.polduu.dto.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserPatchRequestDto(
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    String username,
    @Pattern(regexp = "\\S.*")
    @Size(max = 100, min = 1, message = "Full name must be between 1 and 100 characters")
    String fullName,
    @Size(max = 500, message = "Image URL must be less than 500 characters")
    String imageUrl
) {}