package com.example._ayelcommunitybe.dto.post;

import java.util.List;

public record PostPageResponseDto(
        List<PostListResponseDto> posts,
        PostCursorDto nextCursor,
        boolean hasNext
) {
}