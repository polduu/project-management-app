package com.polduu.dto.user;

public record UserPatchRequestDto(
    String username,
    String fullName,
    String imageUrl
) {}