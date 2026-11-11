package com.polduu.dto.user;

import jakarta.validation.constraints.NotBlank;

public record GitHubAuthRequestDto(

    @NotBlank (message = "GitHub authorization code is required")
    String code // The authorization code received from the client after GitHub authentication
) {
}
