package com.example._ayelcommunitybe.dto.post;

import java.time.LocalDateTime;

public record PostResponseDto(
        int postId,
        String title,
        String content,
        String nickname,
        int viewCount,
        int likeCount,
        int commentCount,
        LocalDateTime createdAt
) {
}
