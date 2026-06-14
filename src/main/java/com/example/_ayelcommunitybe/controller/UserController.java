package com.example._ayelcommunitybe.controller;

import com.example._ayelcommunitybe.constant.SessionConst;
import com.example._ayelcommunitybe.dto.ApiResponse;
import com.example._ayelcommunitybe.dto.user.*;
import com.example._ayelcommunitybe.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping
    public ResponseEntity<ApiResponse<SignupResponseDto>> signup(
            @Valid @RequestBody SignupRequestDto request,
            HttpServletRequest httpRequest
    ) {

        int userId = userService.signup(request);

        HttpSession session = httpRequest.getSession();

        session.setAttribute(
                SessionConst.USER_ID,
                userId
        );

        return ResponseEntity
                .created(URI.create("/users/" + userId))
                .body(
                        ApiResponse.success(
                                "회원가입 성공",
                                new SignupResponseDto(userId)
                        )
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
            @RequestAttribute(SessionConst.USER_ID) int sessionUserId,
            @Valid @RequestBody UserUpdateRequestDto request
    ) {

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
            @RequestAttribute(SessionConst.USER_ID) int sessionUserId,
            @Valid @RequestBody PasswordUpdateRequestDto request
    ) {

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
            @RequestAttribute(SessionConst.USER_ID) int sessionUserId
    ) {

        userService.deleteUser(
                sessionUserId,
                userId
        );

        return ApiResponse.success(
                "회원 탈퇴 성공"
        );
    }
}