package com.example._ayelcommunitybe.dto.post;

import java.util.List;

public record PostPageResponseDto(
        List<PostResponseDto> posts,
        Integer nextCursor,
        boolean hasNext
) {
}