package com.example._ayelcommunitybe.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostCreateRequestDto(

        @NotBlank(message = "{post.title.required}")
        @Size(max = 26, message = "{post.title.max}")
        String title,

        @NotBlank(message = "{post.content.required}")
        @Size(max = 1500, message = "{post.content.max}")
        String content,

        List<String> fileUrls

) {
}