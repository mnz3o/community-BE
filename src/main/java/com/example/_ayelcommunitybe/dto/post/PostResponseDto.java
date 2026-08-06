package com.example._ayelcommunitybe.dto.post;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponseDto(
        int postId,
        int userId,
        String title,
        String content,
        String nickname,
        int viewCount,
        int likeCount,
        int commentCount,
        List<String> fileUrls,
        String profileFileUrl,
        LocalDateTime createdAt,
        boolean isLiked
) {
}