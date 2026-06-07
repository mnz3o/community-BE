package com.example._ayelcommunitybe.dto.user;

public record AuthCheckResponseDto(
        int userId,
        String email,
        String nickname
) {
}
