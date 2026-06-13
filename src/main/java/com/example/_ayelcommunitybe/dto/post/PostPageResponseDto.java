package com.example._ayelcommunitybe.dto.post;

import java.util.List;

public record PostPageResponseDto(
        List<PostListResponseDto> posts,
        Integer nextCursor,
        boolean hasNext
) {
}