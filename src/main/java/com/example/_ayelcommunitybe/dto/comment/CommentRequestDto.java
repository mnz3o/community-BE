package com.example._ayelcommunitybe.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequestDto(
        @NotBlank(message = "{comment.content.required}")
        @Size(max = 500, message = "{comment.content.max}")
        String content

) {
}

