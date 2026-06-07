package com.example._ayelcommunitybe.controller;

import com.example._ayelcommunitybe.dto.ApiResponse;
import com.example._ayelcommunitybe.dto.user.*;
import com.example._ayelcommunitybe.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ApiResponse<SignupResponseDto> signup(
            @Valid @RequestBody SignupRequestDto request
    ) {

        int userId = userService.signup(request);

        return ApiResponse.success(
                "회원가입 성공",
                new SignupResponseDto(userId)
        );
    }

    // 회원 조회
    @GetMapping("/{userId}")
    public ApiResponse<UserResponseDto> getUser(
            @PathVariable int userId
    ) {

        return ApiResponse.success(
                "회원 조회 성공",
                userService.getUser(userId)
        );
    }

    // 회원 정보 수정
    @PatchMapping("/{userId}")
    public ApiResponse<Void> updateUser(
            @PathVariable int userId,
            @Valid @RequestBody UserUpdateRequestDto request,
            HttpServletRequest httpRequest
    ) {

        HttpSession session =
                httpRequest.getSession(false);

        int sessionUserId =
                (int) session.getAttribute("user_id");

        userService.updateUser(
                sessionUserId,
                userId,
                request
        );

        return ApiResponse.success(
                "회원 정보 수정 성공"
        );
    }

    // 비밀번호 수정
    @PutMapping("/{userId}/password")
    public ApiResponse<Void> updatePassword(
            @PathVariable int userId,
            @Valid @RequestBody PasswordUpdateRequestDto request,
            HttpServletRequest httpRequest
    ) {

        HttpSession session =
                httpRequest.getSession(false);

        int sessionUserId =
                (int) session.getAttribute("user_id");

        userService.updatePassword(
                sessionUserId,
                userId,
                request
        );

        return ApiResponse.success(
                "비밀번호 수정 성공"
        );
    }

    // 회원 탈퇴
    @DeleteMapping("/{userId}")
    public ApiResponse<Void> deleteUser(
            @PathVariable int userId,
            HttpServletRequest httpRequest
    ) {

        HttpSession session =
                httpRequest.getSession(false);

        int sessionUserId =
                (int) session.getAttribute("user_id");

        userService.deleteUser(
                sessionUserId,
                userId
        );

        return ApiResponse.success(
                "회원 탈퇴 성공"
        );
    }
}