package com.polduu.dto.user;

import java.util.UUID;

public record AuthResponseDto(
    String accessToken,
    String refreshToken,
    Long expiresIn,     // Ex: 900 (segons)
    UUID id,
    String username,
    String email,
    String imageUrl
) {}