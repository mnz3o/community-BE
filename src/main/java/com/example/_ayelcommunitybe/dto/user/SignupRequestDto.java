package com.example._ayelcommunitybe.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequestDto(

        @NotBlank(message = "{user.email.required}")
        @Email(message = "{user.email.invalid}")
        String email,

        @NotBlank(message = "{user.password.required}")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).{8,20}$",
                message = "{user.password.invalid}")
        String password,

        @NotBlank(message = "{user.password.confirm.required}")
        String passwordConfirm,

        @NotBlank(message = "{user.nickname.required}")
        @Size(max = 10, message = "{user.nickname.max}")
        String nickname
) {
}