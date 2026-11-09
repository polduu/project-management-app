package com.polduu.dto.user;



public record UserRegisterRequestDto(
    String username,
    String email,
    String password,
    String name
) {}
