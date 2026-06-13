package com.example._ayelcommunitybe.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostUpdateRequestDto(

        @NotBlank(message = "{post.title.required}")
        @Size(max = 26, message = "{post.title.max}")
        String title,

        @NotBlank(message = "{post.content.required}")
        String content

) {
}