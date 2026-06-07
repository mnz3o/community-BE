package com.example._ayelcommunitybe.dto.comment;

import java.time.LocalDateTime;

public record CommentResponseDto(
        int commentId,
        String content,
        String nickname,
        LocalDateTime createdAt
) {
}
