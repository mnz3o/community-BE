package com.example._ayelcommunitybe.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordUpdateRequestDto(

        @NotBlank(message = "{user.password.current.required}")
        String currentPassword,

        @NotBlank(message = "{user.password.new.required}")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,20}$",
                message = "{user.password.invalid}")
        String newPassword

) {
}