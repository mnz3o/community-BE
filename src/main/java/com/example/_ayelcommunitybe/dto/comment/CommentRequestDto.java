package com.example._ayelcommunitybe.dto.comment;

import jakarta.validation.constraints.NotBlank;

public record CommentRequestDto(
        @NotBlank(message = "{comment.content.required}")
        String content

) {
}

