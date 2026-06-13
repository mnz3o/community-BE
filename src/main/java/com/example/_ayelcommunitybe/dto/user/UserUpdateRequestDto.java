package com.example._ayelcommunitybe.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequestDto(

        @NotBlank(message = "{user.nickname.required}")
        @Size(max = 10, message = "{user.nickname.max}")
        String nickname
) {
}
