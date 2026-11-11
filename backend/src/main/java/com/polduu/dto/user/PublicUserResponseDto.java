package com.polduu.dto.user;

public record PublicUserResponseDto(
    String username,
    String fullName,
    String imageUrl
) {}