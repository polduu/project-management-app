package com.polduu.dto.user;

public record PublicUserResponseDto(
    String email,
    String username,
    String fullName,
    String imageUrl
) {}