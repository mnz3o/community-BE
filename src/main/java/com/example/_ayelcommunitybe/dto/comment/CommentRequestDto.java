package com.example._ayelcommunitybe.dto.comment;

import jakarta.validation.constraints.NotBlank;

public record CommentRequestDto(
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        String content

) {
}

