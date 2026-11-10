package com.polduu.dto.user;



public record AuthResponseDto(
    String accessToken,
    String refreshToken,
    Long expiresIn,     // Ex: 900 (segons)
    UserResponseDto user
) {}