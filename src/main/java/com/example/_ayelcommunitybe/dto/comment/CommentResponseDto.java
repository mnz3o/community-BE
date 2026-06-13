package com.example._ayelcommunitybe.dto.comment;

import java.time.LocalDateTime;

public record CommentResponseDto(
        int commentId,
        int userId,
        String content,
        String nickname,
        String profileFileUrl,
        LocalDateTime createdAt
) {
}