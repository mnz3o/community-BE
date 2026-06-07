package com.example._ayelcommunitybe.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostUpdateRequestDto(

        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 26, message = "제목은 26자 이하입니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        String content

) {
}