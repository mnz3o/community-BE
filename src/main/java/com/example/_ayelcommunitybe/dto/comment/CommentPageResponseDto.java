package com.example._ayelcommunitybe.dto.comment;

import java.util.List;

public record CommentPageResponseDto(
        List<CommentResponseDto> comments,
        Integer nextCursor,
        boolean hasNext
) {
}