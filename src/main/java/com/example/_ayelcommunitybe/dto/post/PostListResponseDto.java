package com.example._ayelcommunitybe.dto.post;

import java.time.LocalDateTime;

public record PostListResponseDto(
        int postId,
        String title,
        String nickname,
        int viewCount,
        int likeCount,
        int commentCount,
        String profileFileUrl,
        LocalDateTime createdAt
) {
}